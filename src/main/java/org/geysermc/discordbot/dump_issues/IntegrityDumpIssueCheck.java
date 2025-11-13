/*
 * Copyright (c) 2020-2025 GeyserMC. http://geysermc.org
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
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegrityDumpIssueCheck extends AbstractDumpIssueCheck {
    private static Pattern VERSION_PATTERN = Pattern.compile("^(\\d+\\.\\d+\\.\\d+)-b(\\d+) \\(.*\\)$");

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        List<String> issues = new ArrayList<>();

        String gitBuildNumber = dump.getJSONObject("gitInfo").getString("buildNumber");

        // Make sure this has a build number
        if (gitBuildNumber.equals("-1")) {
            return issues;
        }

        String versionString = dump.getJSONObject("versionInfo").getString("version");

        // Make sure the version string is valid
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            return issues;
        }

        // Get the version and build numbers
        String versionNumber = matcher.group(1);
        String buildNumber = matcher.group(2);

        String sha256Hash = dump.getString("hash");

        // https://download.geysermc.org/v2/projects/geyser/versions/2.9.0/builds/979

        JSONObject response = RestClient.get("https://download.geysermc.org/v2/projects/geyser/versions/" + versionNumber + "/builds/" + buildNumber).asJSONObject();

        // Couldnt find the build
        if (response.has("error")) {
            return issues;
        }

        for (String downloadKey : response.getJSONObject("downloads").keySet()) {
            JSONObject download = response.getJSONObject("downloads").getJSONObject(downloadKey);

            if (download.getString("sha256").equals(sha256Hash)) {
                return issues; // All good
            }
        }

        // We didnt match any hashes
        issues.add("- Your Geyser jar is corrupt or has been tampered with. Please re-download it [from the website](https://geysermc.org/download/).");

        return issues;
    }
}
