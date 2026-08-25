/*
 * Copyright (c) 2025-2026 GeyserMC. http://geysermc.org
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OfflineIssueCheck extends AbstractDumpIssueCheck {

    private static final Set<String> POPULAR_AUTH_PLUGINS_MODS = Set.of(
        "easyauth",
        "authmc",
        "sessionguard",
        "authme",
        "authmereloaded",
        "nlogin",
        "loginsecurity",
        "oauthenticator",
        "ultimatelogin",
        "locklogin",
        "fastlogin"
);

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) throws JSONException {
        List<String> problems = new ArrayList<>();
        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        boolean isOffline = !bootstrapInfo.getBoolean("onlineMode");

        if (isOffline) {
            problems.add("- We do not support offline mode servers, please see `!!offline`.");
        } else {
                    String foundAuthAddon = null;
                    if (bootstrapInfo.has("mods")) {
                        foundAuthAddon = findAuthAddon(bootstrapInfo.getJSONArray("mods"));
                    } else if (bootstrapInfo.has("plugins")) {
                        foundAuthAddon = findAuthAddon(bootstrapInfo.getJSONArray("plugins"));
                    }

                    if (foundAuthAddon != null) {
                        problems.add("- Server is in online mode, but authentication plugin/mod `" + foundAuthAddon + "` was found. This may interfere with authentication.");
                    }
        }

        return problems;
    }

    private String findAuthAddon(JSONArray addons) {
        for (int i = 0; i < addons.length(); i++) {
            JSONObject addon = addons.getJSONObject(i);
            if (addon.optBoolean("enabled", true) && addon.has("name")) {
                String name = addon.getString("name");
                if (POPULAR_AUTH_PLUGINS_MODS.contains(name.toLowerCase())) {
                    return name;
                }
            }
        }
        return null;
    }
}
