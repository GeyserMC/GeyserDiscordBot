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

package org.geysermc.discordbot.listeners;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import com.nukkitx.protocol.bedrock.BedrockClient;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.dump_issues.AbstractDumpIssueCheck;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.NetworkUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHRepository;
import org.reflections.Reflections;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    private static final Pattern DUMP_URL = Pattern.compile("dump\\.geysermc\\.org/(raw/)?([0-9a-z]{32})", Pattern.CASE_INSENSITIVE);
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
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Matcher matcher = DUMP_URL.matcher(event.getMessage().getContentRaw());

        if (!matcher.find()) {
            // Check attached files
            for (Message.Attachment attachment : event.getMessage().getAttachments()) {
                if (attachment.getFileName().equals("dump.json")) {
                    String contents = RestClient.simpleGetString(attachment.getUrl());

                    if (isDump(contents)) {
                        parseDump(event, null, contents);
                    }
                }
            }

            return;
        }

        String cleanURL = "https://dump.geysermc.org/" + matcher.group(2);
        String rawURL = "https://dump.geysermc.org/raw/" + matcher.group(2);

        parseDump(event, cleanURL, RestClient.simpleGetString(rawURL));
    }

    /**
     * Check if the given json matches the signature of a valid dump
     *
     * @param contents Json in text format to check
     * @return True if the dump matches the signature
     */
    private boolean isDump(String contents) {
        try {
            // Get json data from dump
            JSONObject dump = new JSONObject(contents);

            // Check the dump isn't empty or an error message
            if (dump.isEmpty() || (dump.length() == 1 && dump.has("message"))) {
                return false;
            }

            // Check for the dump parts
            JSONObject config = dump.getJSONObject("config");
            config.getJSONObject("bedrock");
            config.getJSONObject("remote");
            dump.getJSONObject("gitInfo");
            dump.getJSONObject("bootstrapInfo");

            return true;
        } catch (JSONException ignored) {
            return false;
        }
    }

    /**
     * Parse and handle the information for a dump
     *
     * @param event Message event that caused the check
     * @param cleanURL The url of the dump
     * @param contents Contents of the dump to check
     */
    private void parseDump(@NotNull MessageReceivedEvent event, String cleanURL, String contents) {
        JSONObject bootstrapInfo;
        JSONObject gitInfo;
        JSONObject dump;
        JSONObject configRemote;
        JSONObject configBedrock;
        JSONObject config;
        try {
            // Get json data from dump
            dump = new JSONObject(contents);

            // Check the dump isn't empty or an error message
            if (dump.isEmpty() || (dump.length() == 1 && dump.has("message"))) {
                MessageHelper.errorResponse(event, "Empty dump", "The dump you linked was empty, its either an invalid link or has expired. Please make a new one." + (!dump.isEmpty() ? " (" + dump.getString("message").trim() + ")" : ""));
                return;
            }

            // Setup some helper vars for quicker access
            config = dump.getJSONObject("config");
            configBedrock = config.getJSONObject("bedrock");
            configRemote = config.getJSONObject("remote");
            gitInfo = dump.getJSONObject("gitInfo");
            bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        } catch (JSONException ignored) {
            MessageHelper.errorResponse(event, "Invalid dump", "The dump you linked was invalid. Please make a new one.");
            return;
        }

        String platform = bootstrapInfo.getString("platform");
        List<String> problems = new ArrayList<>();

        // Check plugins and stuff for potential issues
        for (AbstractDumpIssueCheck issueCheck : ISSUE_CHECKS) {
            if (issueCheck.compatiblePlatform(platform)) {
                try {
                    problems.addAll(issueCheck.checkIssues(dump));
                } catch (JSONException ignored) { }
            }
        }

        StringBuilder gitData = new StringBuilder();
        String gitUrl = gitInfo.getString("git.remote.origin.url").replaceAll("\\.git$", "");

        GHRepository repo = null;
        try {
            repo = GeyserBot.getGithub().getRepository("GeyserMC/Geyser");
        } catch (IOException e) {
            MessageHelper.errorResponse(event, "Failed to get Geyser repository", "There was an issue trying to get the Geyser repository!\n" + e.getMessage());
        }

        // Get the commit hash
        GHCommit latestCommit = repo.listCommits()._iterator(1).next();

        // Compare latest and current
        GHCompare compare = null;
        try {
            compare = repo.getCompare(latestCommit, repo.getCommit(gitInfo.getString("git.commit.id")));
        } catch (IOException e) {
            MessageHelper.errorResponse(event, "Failed to get latest commit", "There was an issue trying to get the latest commit!\n" + e.getMessage());
        }

        // Can be null for unpublished commits
        if (compare != null) {
            // Set the latest info based on the returned comparison
            if (compare.getBehindBy() != 0 || compare.getAheadBy() != 0) {
                gitData.append("**Latest:** No\n");
                problems.add("- You aren't on the latest Geyser version! Please [download](https://ci.opencollab.dev/job/GeyserMC/job/Geyser/job/master/) the latest version.");
            } else {
                gitData.append("**Latest:** Yes\n");
            }
        }

        boolean isFork = false;
        // Check if they are using a custom fork
        if (!gitUrl.startsWith("https://github.com/GeyserMC/Geyser")) {
            gitData.append("**Is fork:** Yes ([").append(gitUrl.replace("https://github.com/", "")).append("](").append(gitUrl).append("))\n");
            isFork = true;
        }

        gitData.append("**Commit:** [`").append(gitInfo.getString("git.commit.id.abbrev")).append("`](").append(gitUrl).append("/commit/").append(gitInfo.getString("git.commit.id")).append(")\n");
        gitData.append("**Branch:** [`").append(gitInfo.getString("git.branch")).append("`](").append(gitUrl).append("/tree/").append(gitInfo.getString("git.branch")).append(")\n");

        if (compare != null && compare.getAheadBy() != 0) {
            gitData.append("Ahead by ").append(compare.getAheadBy()).append(" commit").append(compare.getAheadBy() == 1 ? "" : "s").append("\n");
        }

        boolean compareByBuildNumber = false;
        if (!isFork && gitInfo.has("git.build.number")) {
            try {
                // Attempt to see how far behind they are not based on commits but CI builds
                String buildXML = RestClient.simpleGetString("https://ci.opencollab.dev/job/GeyserMC/job/Geyser/job/" +
                        URLEncoder.encode(gitInfo.getString("git.branch"), StandardCharsets.UTF_8.toString()) + "/lastSuccessfulBuild/api/xml?xpath=//buildNumber");
                if (buildXML.startsWith("<buildNumber>")) {
                    int latestBuildNum = Integer.parseInt(buildXML.replaceAll("<(\\\\)?(/)?buildNumber>", "").trim());
                    int buildNum = Integer.parseInt(gitInfo.getString("git.build.number"));

                    int buildNumDiff = latestBuildNum - buildNum;
                    if (buildNumDiff > 0) {
                        compareByBuildNumber = true;
                        String compareUrl = gitUrl + "/compare/" + gitInfo.getString("git.commit.id.abbrev") + "..." + gitInfo.getString("git.branch");
                        gitData.append("Behind by [").append(buildNumDiff).append(" CI build").append(buildNumDiff == 1 ? "" : "s").append("](").append(compareUrl).append(")\n");
                    }
                }
            } catch (IOException | NumberFormatException ignored) {
            }
        }

        if (!compareByBuildNumber) {
            if (compare != null && compare.getBehindBy() != 0) {
                String compareUrl = gitUrl + "/compare/" + gitInfo.getString("git.commit.id.abbrev") + "..." + gitInfo.getString("git.branch");
                gitData.append("Behind by [").append(compare.getBehindBy()).append(" commit").append(compare.getBehindBy() == 1 ? "" : "s").append("](").append(compareUrl).append(")\n");
            }
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
        EmbedBuilder buildEmbed = new EmbedBuilder()
                .setTitle("<:geyser:723981877773598771> Geyser " + platformNamePretty, cleanURL)
                .setDescription(problems.size() != 0 ? "**Possible problems:**\n" + problems.stream().map(Object::toString).collect(Collectors.joining("\n")) : "")
                .addField("Git info", gitData.toString(), false)
                .addField("Platform", platformNamePretty, true)
                .addField("Listen address", bedrockAddrText, true)
                .addField("Remote address", javaAddrText, true)
                .addField("Auth type", configRemote.getString("auth-type"), true)
                .addField("Server version", versionString, true)
                .addField("Autoconfigured remote?", (config.getBoolean("autoconfiguredRemote")) ? "Yes" : "No", true)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor());

        if (!dump.isNull("logsInfo")) {
            try {
                String logs = dump.getJSONObject("logsInfo").getString("link");
                buildEmbed.addField("Logs", logs, true);
            } catch (JSONException ignored) { }
        }

        event.getMessage().replyEmbeds(buildEmbed.build()).queue();
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
        } else if (NetworkUtils.isInternalIP(address)) { // Check if the server is listening on an internal ip and ping it if not
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
                addrText += " [(server online, " + data.getVersion().getName() + ")](https://mcsrvstat.us/server/" + address + ":" + port + ")";
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
        } else if (NetworkUtils.isInternalIP(address)) { // Check if the server is listening on an internal ip and ping it if not
            addrText += " (internal IP)";
        } else {
            try {
                // Ping the server with a timeout of 1.5s
                InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", (int) (10000 + Math.round(Math.random() * 1000)));
                BedrockClient client = new BedrockClient(bindAddress);

                client.bind().join();

                InetSocketAddress addressToPing = new InetSocketAddress(address, port);
                client.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

                // Mark the server as pinged and add the status to the address field
                addrText += " [(server online)](https://mcsrvstat.us/server/" + address + ":" + port + ")";
            } catch (InterruptedException | ExecutionException ignored) {
                addrText += " [(server offline)](https://mcsrvstat.us/server/" + address + ":" + port + ")";
            }
        }

        return addrText;
    }
}
