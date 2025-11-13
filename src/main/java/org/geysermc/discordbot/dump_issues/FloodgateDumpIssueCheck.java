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
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FloodgateDumpIssueCheck extends AbstractDumpIssueCheck {

    @Override
    public boolean compatiblePlatform(String platform) {
        return !platform.equals("STANDALONE");
    }

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        JSONObject configRemote = dump.getJSONObject("config").getJSONObject("java");

        List<String> problems = new ArrayList<>();

        // Check plugins
        if (bootstrapInfo.has("plugins")) {
            boolean needsFloodgate = configRemote.getString("auth-type").equalsIgnoreCase("floodgate");
            boolean needsFloodgateAuthType = false;

            JSONArray plugins = bootstrapInfo.getJSONArray("plugins");
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);

                // Check if floodgate is installed
                if (plugin.getString("name").toLowerCase().contains("floodgate")) {
                    // Check if its enabled
                    if (plugin.getBoolean("enabled")) {
                        needsFloodgate = false;
                        needsFloodgateAuthType = true;
                    }

                    // Check we aren't on an old version of 1.8
                    if (bootstrapInfo.has("platformAPIVersion") && bootstrapInfo.getString("platformAPIVersion").startsWith("1.8-R0.1")) {
                        problems.add("- You run on an outdated and unsupported version of 1.8, you can download the latest Paper build (1.8.8) [here](https://papermc.io/api/v1/paper/1.8.8/443/download).");
                    }
                    break;
                }
            }

            // Add any problem messages relates to floodgate
            if (needsFloodgate) {
                problems.add("- `auth-type` is set to `floodgate`, but you don't have Floodgate installed! Download it [here](https://geysermc.org/download#floodgate).");
            } else if (needsFloodgateAuthType && !configRemote.getString("auth-type").equalsIgnoreCase("floodgate")) {
                problems.add("- You have Floodgate installed, but `auth-type` is set to `" + configRemote.getString("auth-type") + "`! Set it to `floodgate` if you want to use Floodgate.");
            }
        }

        return problems;
    }
}
