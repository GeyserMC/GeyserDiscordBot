/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/GeyserDiscordBot
 */

package org.geysermc.discordbot.dump_issues;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlatformVersionDumpIssueCheck extends AbstractDumpIssueCheck {

    @Override
    public boolean compatiblePlatform(String platform) {
        return !platform.equals("STANDALONE");
    }

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) throws JSONException {
        List<String> problems = new ArrayList<>();

        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        String platformName = bootstrapInfo.getString("platformName");
        String platformVersion = bootstrapInfo.getString("platformVersion");
        String softwareName = platformName;

        boolean isLatest = switch (platformName) {
            case "Paper" -> {
                String[] versionParts = platformVersion.split("-");
                yield isLatestFillBuild("paper", versionParts[0], Integer.parseInt(versionParts[1]));
            }
            case "Velocity" -> {
                String[] versionParts = platformVersion.split("-b");
                String build = versionParts[versionParts.length-1];

                yield isLatestFillBuild(
                        "velocity", platformVersion.split(" ")[0],
                        Integer.parseInt(build.replace(")", ""))
                );
            }
            case "fabric" -> {
                softwareName = "Fabric Loader";
                yield isLatestFabricLoader(platformVersion);
            }
            default -> true;
        };

        if (!isLatest) {
            problems.add("- Your version of %s is out of date, please consider updating your server software.".formatted(softwareName));
        }

        return problems;
    }

    private static boolean isLatestFillBuild(String project, String projectVersion, int build) {
        RestClient.Response response = RestClient.get("https://fill.papermc.io/v3/projects/%s/versions/%s".formatted(project, projectVersion));

        if (!response.success()) return true; // Assume all is fine

        JSONObject fillData = response.asJSONObject();

        JSONArray buildsArray = fillData.getJSONArray("builds");

        int latestBuild = (Integer) buildsArray.get(0);

        return latestBuild <= build;
    }

    private static boolean isLatestFabricLoader(String currentFabricLoader) {
        RestClient.Response response = RestClient.get("https://meta.fabricmc.net/v2/versions/loader");

        if (!response.success()) return true; // Assume all is fine

        JSONArray buildsArray = response.asJSONArray();

        JSONObject latestData = buildsArray.getJSONObject(0);

        String separator = latestData.getString("separator");
        if (!separator.equals(".")) return false;

        List<Integer> latestVersionParts = Arrays.stream(latestData.getString("version").split("\\."))
                .map(Integer::parseInt).toList();
        List<Integer> currentVersionParts = Arrays.stream(currentFabricLoader.split("\\."))
                .map(Integer::parseInt).toList();

        if (currentVersionParts.get(0) < latestVersionParts.get(0)) return false;
        if (currentVersionParts.get(1) < latestVersionParts.get(1)) return false;
        return currentVersionParts.get(2) >= latestVersionParts.get(2);
    }
}
