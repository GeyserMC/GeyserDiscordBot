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

package org.geysermc.discordbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumpHandler extends ListenerAdapter {

    private static final Pattern DUMP_URL = Pattern.compile("dump\\.geysermc\\.org/([0-9a-z]{32})", Pattern.CASE_INSENSITIVE);
    private static final String[] INTERNAL_IP_RANGES = new String[] {"0.0.0.0/8", "10.0.0.0/8", "100.64.0.0/10", "127.0.0.0/8", "169.254.0.0/16", "172.16.0.0/12", "192.0.0.0/24", "192.0.2.0/24", "192.88.99.0/24", "192.168.0.0/16", "198.18.0.0/15", "198.51.100.0/24", "203.0.113.0/24", "224.0.0.0/4", "240.0.0.0/4", "255.255.255.255/32"};

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Matcher matcher = DUMP_URL.matcher(event.getMessage().getContentRaw());

        if (matcher.find()) {
            String cleanURL = "https://dump.geysermc.org/" + matcher.group(1);
            String rawURL = "https://dump.geysermc.org/raw/" + matcher.group(1);

            // Get json data from dump
            JSONObject dump = new JSONObject(RestClient.get(rawURL));

            // Setup some helper vars for quicker access
            JSONObject config = dump.getJSONObject("config");
            JSONObject configBedrock = config.getJSONObject("bedrock");
            JSONObject configRemote = config.getJSONObject("remote");
            JSONObject gitInfo = dump.getJSONObject("gitInfo");
            JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");

            // TODO: Check plugins and stuff for issues

            StringBuilder gitData = new StringBuilder();
            String gitUrl = gitInfo.getString("git.remote.origin.url").replaceAll("\\.git$", "");

            // Get the commit hash
            String latestCommit = new JSONArray(RestClient.get("https://api.github.com/repos/GeyserMC/Geyser/commits?per_page=1")).getJSONObject(0).getString("sha");

            // Compare latest and current
            JSONObject compare = new JSONObject(RestClient.get("https://api.github.com/repos/GeyserMC/Geyser/compare/" + latestCommit + "..." + gitInfo.getString("git.commit.id")));

            // Set the latest info based on the returned comparison
            if (compare.getInt("behind_by") != 0 && compare.getInt("ahead_by") != 0) {
                gitData.append("**Latest:** No\n");
            } else {
                gitData.append("**Latest:** Yes\n");
            }

            // Check if they are using a custom fork
            if (!gitUrl.startsWith("https://github.com/GeyserMC/Geyser")) {
                gitData.append("**Is fork:** Yes ([").append(gitUrl.replace("https://github.com/", "")).append("](").append(gitUrl).append("))\n");
            }

            gitData.append("**Commit:** [`").append(gitInfo.getString("git.commit.id.abbrev")).append("`](").append(gitUrl).append("/commit/").append(gitInfo.getString("git.commit.id")).append(")\n");

            if (compare.getInt("ahead_by") != 0) {
                gitData.append("Ahead by ").append(compare.getInt("ahead_by")).append(" commit").append(compare.getInt("ahead_by") == 1 ? "" : "s").append("\n");
            }

            if (compare.getInt("behind_by") != 0) {
                gitData.append("Behind by ").append(compare.getInt("behind_by")).append(" commit").append(compare.getInt("behind_by") == 1 ? "" : "s").append("\n");
            }

            String versionString = "Unknown";
            String addr = configRemote.getString("address") + ':' + configRemote.getInt("port");
            String addrText = addr;

            // TODO: Ping server
            // Should have 1 func for bedrock pings and 1 for java pings

            // If Bedrock address is censored, account for its formatting
            String bedrockAddrText = configBedrock.getString("address");
            if (bedrockAddrText.equals("***")) {
                bedrockAddrText = "\\*\\*\\*";
            }

            // Get the version string from the dump if it exists
            if (bootstrapInfo.has("platformVersion")) {
                versionString = bootstrapInfo.getString("platformVersion");
            }

            // Get the platform name and format it to title case (Xxxxxx)
            String platformNamePretty = bootstrapInfo.getString("platform").substring(0, 1).toUpperCase() +
                    bootstrapInfo.getString("platform").substring(1).toLowerCase();

            // TODO: Change the emote to not be hardcoded
            // Not sure how to do that the best as searching for it everytime seems pointless and expensive
            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("<:geyser:723981877773598771> Geyser", cleanURL)
                    .addField("Git info", gitData.toString(), false)
                    .addField("Platform", platformNamePretty, true)
                    .addField("Remote address", addrText, true)
                    .addField("Listen address", bedrockAddrText + ":" + configBedrock.getInt("port"), true)
                    .addField("Auth type", configRemote.getString("auth-type"), true)
                    .addField("Server version", versionString, true)
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build()).queue();
        }
    }
}
