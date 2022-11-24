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

package org.geysermc.discordbot.updates;

import org.geysermc.discordbot.GeyserBot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.List;

public class MinecraftUpdateCheck extends AbstractUpdateCheck {

    private static final String CHECK_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final List<String> KNOWN_VERSIONS = new ArrayList<>();

    @Override
    public void populate() throws JSONException {
        JSONObject versionsData = RestClient.simpleGetJsonObject(CHECK_URL);
        JSONArray versions = versionsData.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            KNOWN_VERSIONS.add(versions.getJSONObject(i).getString("id"));
        }

        GeyserBot.LOGGER.info("Loaded " + KNOWN_VERSIONS.size() + " initial Java Minecraft versions");
    }

    @Override
    public void check() {
        JSONObject versionsData = RestClient.simpleGetJsonObject(CHECK_URL);
        if (versionsData.has("error")) {
            GeyserBot.LOGGER.warn("Error while checking '" + CHECK_URL + "': " + versionsData.getString("error"));
            return;
        }
        JSONArray versions = versionsData.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject version = versions.getJSONObject(i);
            if (!KNOWN_VERSIONS.contains(version.getString("id"))) {
                KNOWN_VERSIONS.add(version.getString("id"));
                UpdateManager.sendMessage("A new " + version.getString("type") + " version of Java Minecraft was just released! : " + version.getString("id"));
            }
        }
    }
}
