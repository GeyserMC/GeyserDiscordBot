/*
 * Copyright (c) 2020-2021 GeyserMC. http://geysermc.org
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionDumpIssueCheck extends AbstractDumpIssueCheck {

    @Override
    public boolean compatablePlatform(String platform) {
        return !platform.equals("STANDALONE");
    }

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        String platform = bootstrapInfo.getString("platform");
        String supportedMinecraft = dump.getJSONObject("versionInfo").getJSONObject("mcInfo").getString("javaVersion");

        boolean isOldVersion = false;

        // Check if we are running an old server version
        if (!(platform.equals("BUNGEECORD") || platform.equals("VELOCITY") || platform.equals("FABRIC") || platform.equals("ANDROID")) &&
                !bootstrapInfo.getString("platformVersion").contains(supportedMinecraft)) {
            isOldVersion = true;
        }

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
            return Collections.singletonList("- Your server needs to be on Minecraft " + supportedMinecraft + "! If you're on an old version you can use [ViaVersion](https://www.spigotmc.org/resources/viaversion.19254/).");
        }

        return new ArrayList<>();
    }
}
