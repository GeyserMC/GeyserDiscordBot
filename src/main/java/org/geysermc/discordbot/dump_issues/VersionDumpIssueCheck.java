/*
 * Copyright (c) 2020-2022 GeyserMC. http://geysermc.org
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
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

public class VersionDumpIssueCheck extends AbstractDumpIssueCheck {

    @Override
    public boolean compatiblePlatform(String platform) {
        return !platform.equals("STANDALONE");
    }

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        String platform = bootstrapInfo.getString("platform");
        JSONObject jsonSupportedMinecraft = dump.getJSONObject("versionInfo").getJSONObject("mcInfo");
        List<String> supportedMinecraft;
        if (jsonSupportedMinecraft.get("javaVersions") instanceof JSONArray array) {
            supportedMinecraft = StreamSupport.stream(array.spliterator(), false).map((object) -> (String) object).toList();
        } else {
            supportedMinecraft = Collections.singletonList(jsonSupportedMinecraft.getString("javaVersion"));
        }

        boolean isOldVersion = false;
        if (!(platform.equals("BUNGEECORD") || platform.equals("VELOCITY") || platform.equals("FABRIC") || platform.equals("ANDROID"))) {
            for (String version : supportedMinecraft) {
                isOldVersion = !bootstrapInfo.getString("platformVersion").contains(version);
                if (!isOldVersion) {
                    break;
                }
            }
        }

        // Check if we are running an old server version

        // Check plugins
        if (bootstrapInfo.has("plugins")) {
            JSONArray plugins = bootstrapInfo.getJSONArray("plugins");
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);

                // Check if VV is installed
                if (plugin.getString("name").equals("ViaVersion") && plugin.getBoolean("enabled")) {
                    isOldVersion = false;
                    break;
                }
            }
        }

        if (isOldVersion) {
            if (supportedMinecraft.size() > 1) {
                return Collections.singletonList("- You're server is not on one the following Minecraft versions: " + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).");
            } else {
                return Collections.singletonList("- You're server is not on Minecraft " + supportedMinecraft.get(0) + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).");
            }
        }

        return Collections.emptyList();
    }
}
