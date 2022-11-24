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

public class JiraUpdateCheck extends AbstractUpdateCheck {

    private static final String CHECK_URL = "https://bugs.mojang.com/rest/api/latest/project/";
    private static final List<String> KNOWN_VERSIONS = new ArrayList<>();

    private final String project;
    private final String platform;

    public JiraUpdateCheck(String project, String platform) {
        this.project = project;
        this.platform = platform;
    }

    @Override
    public void populate() throws JSONException {
        JSONArray versions = RestClient.simpleGetJsonArray(CHECK_URL + project + "/versions");

        for (int i = 0; i < versions.length(); i++) {
            String name = versions.getJSONObject(i).getString("name");
            if (!name.toLowerCase().contains("future version")) {
                KNOWN_VERSIONS.add(name);
            }
        }

        GeyserBot.LOGGER.info("Loaded " + KNOWN_VERSIONS.size() + " initial " + platform + " jira versions");
    }

    @Override
    public void check() {
        String versionsText = RestClient.simpleGetString(CHECK_URL + project + "/versions");

        try {
            JSONArray versions = new JSONArray(versionsText);

            for (int i = 0; i < versions.length(); i++) {
                String name = versions.getJSONObject(i).getString("name");
                if (!KNOWN_VERSIONS.contains(name) && !name.toLowerCase().contains("future version")) {
                    KNOWN_VERSIONS.add(name);
                    UpdateManager.sendMessage("A new " + platform + " version (" + name + ") has been added to the Minecraft issue tracker!");
                }
            }
        } catch (JSONException e) {
            try {
                JSONObject obj = new JSONObject(versionsText);
                GeyserBot.LOGGER.warn("Error while checking Jira versions for '" + project + "': " + obj.getString("error"));
            } catch (JSONException e2) {
                throw new JSONException(e.getMessage() + "\n" + versionsText);
            }
        }

    }
}
