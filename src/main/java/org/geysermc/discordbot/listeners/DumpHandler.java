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

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.net.util.SubnetUtils;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.dump_issues.AbstractDumpIssueCheck;
import org.geysermc.discordbot.util.PropertiesManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DumpHandler extends ListenerAdapter {

    private static final Pattern DUMP_URL = Pattern.compile("dump\\.geysermc\\.org/([0-9a-z]{32})", Pattern.CASE_INSENSITIVE);
    private static final SubnetUtils.SubnetInfo[] INTERNAL_IP_RANGES;
    public static final List<AbstractDumpIssueCheck> ISSUE_CHECKS;

    static {
        // Gathers all checks from "dumpissues" package.
        List<AbstractDumpIssueCheck> checks = new ArrayList<>();
        try {
            Reflections reflections = new Reflections("org.geysermc.discordbot.dump_issues");
            Set<Class<? extends AbstractDumpIssueCheck>> subTypes = reflections.getSubTypesOf(AbstractDumpIssueCheck.class);

            for (Class<? extends AbstractDumpIssueCheck> theClass : subTypes) {
                checks.add(theClass.getDeclaredConstructor().newInstance());
                LoggerFactory.getLogger(theClass).debug("Loaded Successfully!");
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            GeyserBot.LOGGER.error("Unable to load commands", e);
        }
        ISSUE_CHECKS = checks;

        // We cache the infos here since its expensive to create them every time we need them
        INTERNAL_IP_RANGES = new SubnetUtils.SubnetInfo[] {
                new SubnetUtils("0.0.0.0/8").getInfo(),
                new SubnetUtils("10.0.0.0/8").getInfo(),
                new SubnetUtils("100.64.0.0/10").getInfo(),
                new SubnetUtils("127.0.0.0/8").getInfo(),
                new SubnetUtils("169.254.0.0/16").getInfo(),
                new SubnetUtils("172.16.0.0/12").getInfo(),
                new SubnetUtils("192.0.0.0/24").getInfo(),
                new SubnetUtils("192.0.2.0/24").getInfo(),
                new SubnetUtils("192.88.99.0/24").getInfo(),
                new SubnetUtils("192.168.0.0/16").getInfo(),
                new SubnetUtils("198.18.0.0/15").getInfo(),
                new SubnetUtils("198.51.100.0/24").getInfo(),
                new SubnetUtils("203.0.113.0/24").getInfo(),
                new SubnetUtils("224.0.0.0/4").getInfo(),
                new SubnetUtils("240.0.0.0/4").getInfo(),
                new SubnetUtils("255.255.255.255/32").getInfo()
        };
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Matcher matcher = DUMP_URL.matcher(event.getMessage().getContentRaw());

        if (!matcher.find()) {
            return;
        }

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

        String platform = bootstrapInfo.getString("platform");
        List<String> problems = new ArrayList<>();

        // Check plugins and stuff for potential issues
        for (AbstractDumpIssueCheck issueCheck : ISSUE_CHECKS) {
            if (issueCheck.compatablePlatform(platform)) {
                problems.addAll(issueCheck.checkIssues(dump));
            }
        }

        StringBuilder gitData = new StringBuilder();
        String gitUrl = gitInfo.getString("git.remote.origin.url").replaceAll("\\.git$", "");

        // TODO: Migrate this over to the github-api lib we have

        // Get the commit hash
        String latestCommit = new JSONArray(RestClient.get("https://api.github.com/repos/GeyserMC/Geyser/commits?per_page=1")).getJSONObject(0).getString("sha");

        // Compare latest and current
        JSONObject compare = new JSONObject(RestClient.get("https://api.github.com/repos/GeyserMC/Geyser/compare/" + latestCommit + "..." + gitInfo.getString("git.commit.id")));

        // Set the latest info based on the returned comparison
        if (compare.getInt("behind_by") != 0 || compare.getInt("ahead_by") != 0) {
            gitData.append("**Latest:** No\n");
            problems.add("- You aren't on the latest Geyser version! Please [download](https://ci.opencollab.dev/job/GeyserMC/job/Geyser/job/master/) the latest version.");
        } else {
            gitData.append("**Latest:** Yes\n");
        }

        // Check if they are using a custom fork
        if (!gitUrl.startsWith("https://github.com/GeyserMC/Geyser")) {
            gitData.append("**Is fork:** Yes ([").append(gitUrl.replace("https://github.com/", "")).append("](").append(gitUrl).append("))\n");
        }

        gitData.append("**Commit:** [`").append(gitInfo.getString("git.commit.id.abbrev")).append("`](").append(gitUrl).append("/commit/").append(gitInfo.getString("git.commit.id")).append(")\n");
        gitData.append("**Branch:** [`").append(gitInfo.getString("git.branch")).append("`](").append(gitUrl).append("/tree/").append(gitInfo.getString("git.branch")).append(")\n");

        if (compare.getInt("ahead_by") != 0) {
            gitData.append("Ahead by ").append(compare.getInt("ahead_by")).append(" commit").append(compare.getInt("ahead_by") == 1 ? "" : "s").append("\n");
        }

        if (compare.getInt("behind_by") != 0) {
            gitData.append("Behind by ").append(compare.getInt("behind_by")).append(" commit").append(compare.getInt("behind_by") == 1 ? "" : "s").append("\n");
        }

        String versionString = "Unknown";

        // Ping java server
        String javaAddrText = getJavaServerText(configRemote.getString("address"), configRemote.getInt("port"));

        // Ping bedrock server
        String bedrockAddrText = getBedrockServerText(configBedrock.getString("address"), configBedrock.getInt("port"));

        // Get the version string from the dump if it exists
        if (bootstrapInfo.has("platformVersion")) {
            versionString = bootstrapInfo.getString("platformVersion");
        }

        // Get the platform name and format it to title case (Xxxxxx)
        String platformNamePretty = platform.substring(0, 1).toUpperCase() +
                platform.substring(1).toLowerCase();

        // TODO: Change the emote to not be hardcoded
        // Not sure how to do that the best as searching for it everytime seems pointless and expensive
        event.getMessage().reply(new EmbedBuilder()
                .setTitle("<:geyser:723981877773598771> Geyser " + platformNamePretty, cleanURL)
                .setDescription(problems.size() != 0 ? "**Possible problems:**\n" + problems.stream().map(Object::toString).collect(Collectors.joining("\n")) : "")
                .addField("Git info", gitData.toString(), false)
                .addField("Platform", platformNamePretty, true)
                .addField("Remote address", javaAddrText, true)
                .addField("Listen address", bedrockAddrText, true)
                .addField("Auth type", configRemote.getString("auth-type"), true)
                .addField("Server version", versionString, true)
                .addField("Cache chunks?", (platform.equals("SPIGOT") || config.getBoolean("cache-chunks")) ? "Yes" : "No", true)
                .setTimestamp(Instant.now())
                .setColor(PropertiesManager.getDefaultColor())
                .build()).queue();
    }

    /**
     * Check if an IP is internal/reserved as defined by the IETF and IANA
     * https://en.wikipedia.org/wiki/Reserved_IP_addresses
     *
     * @param address IP address to check
     * @return True if the IP is internal/reserved
     */
    private boolean isInternalIP(String address) {
        try {
            for (SubnetUtils.SubnetInfo subnetInfo : INTERNAL_IP_RANGES) {
                if (subnetInfo.isInRange(address) || subnetInfo.getAddress().equals(address)) {
                    return true;
                }
            }
        } catch (IllegalArgumentException ignored) { } // If we get this then its likely a domain

        return false;
    }

    /**
     * Ping a Java server and return the rich address text
     *
     * @param address Target IP address
     * @param port Target port
     * @return Rich address text
     */
    private String getJavaServerText(String address, int port) {
        String addrText = address + ":" + port;

        // Censored dump
        if (address.equals("***")) {
            addrText = "\\*\\*\\*:" + port; // Discord formatting
        } else if (isInternalIP(address)) { // Check if the server is listening on an internal ip and ping it if not
            addrText += " (internal IP)";
        } else {
            try {
                // Ping the server with a timeout of 1.5s
                MCPingOptions options = MCPingOptions.builder()
                        .hostname(address)
                        .port(port)
                        .timeout(1500)
                        .build();

                MCPingResponse data = MCPing.getPing(options);

                // Mark the server as pinged and add the status to the address field
                addrText += " [(server online)](https://mcsrvstat.us/server/" + address + ":" + port + ")";

                // TODO: Implement version from ping
            } catch (IOException ignored) {
                addrText += " [(server offline)](https://mcsrvstat.us/server/" + address + ":" + port + ")";
            }
        }

        return addrText;
    }

    /**
     * Ping a Bedrock server and return the rich address text
     *
     * @param address Target IP address
     * @param port Target port
     * @return Rich address text
     */
    private String getBedrockServerText(String address, int port) {
        String addrText = address + ":" + port;

        // Censored dump
        if (address.equals("***")) {
            addrText = "\\*\\*\\*:" + port; // Discord formatting
        } else if (isInternalIP(address)) { // Check if the server is listening on an internal ip and ping it if not
            addrText += " (internal IP)";
        } else {
            try {
                // Ping the server with a timeout of 1.5s
                InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", (int) (10000 + Math.round(Math.random() * 1000)));
                BedrockClient client = new BedrockClient(bindAddress);

                client.bind().join();

                InetSocketAddress addressToPing = new InetSocketAddress(address, port);
                BedrockPong pong = client.ping(addressToPing,1500, TimeUnit.MILLISECONDS).get();

                // Mark the server as pinged and add the status to the address field
                addrText += " [(server online)](https://mcsrvstat.us/server/" + address + ":" + port + ")";
            } catch (InterruptedException | ExecutionException ignored) {
                addrText += " [(server offline)](https://mcsrvstat.us/server/" + address + ":" + port + ")";
            }
        }

        return addrText;
    }
}
