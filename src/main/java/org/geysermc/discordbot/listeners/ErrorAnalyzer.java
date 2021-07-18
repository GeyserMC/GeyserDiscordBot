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

import com.rtm516.stackparser.Parser;
import com.rtm516.stackparser.StackException;
import com.rtm516.stackparser.StackLine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.GithubFileFinder;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<String, String> configExceptionFixes;
    private final Map<Pattern, String> configExceptionChecks;

    private static final Pattern BRANCH_PATTERN = Pattern.compile("Geyser .* \\(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\\)");

    public ErrorAnalyzer() {
        configExceptionFixes = new HashMap<>();
        configExceptionChecks = new HashMap<>();

        // TODO: Move some of these to the database instead of hard-coding?

        // Known exceptions
        configExceptionFixes.put("java.net.BindException: Address already in use", "This means something (likely another instance of Geyser) is running on the port you have specified in the config. Please make sure you close all applications running on this port. If you don't recall opening anything, usually restarting your computer fixes this.");
        configExceptionFixes.put("java.net.BindException: Cannot assign requested address: bind", "This means the IP your server is trying to use is unavailable or disallowed by the system or firewall.");
        configExceptionFixes.put("java.lang.AssertionError: Expected AES to be available", "Update your Java at [AdoptOpenJDK.net](https://adoptopenjdk.net/).");
        configExceptionFixes.put("AnnotatedConnectException: Connection timed out", "The Geyser instance cannot connect to your Java server.");

        // Log url patterns
        configExceptionChecks.put(Pattern.compile("hastebin\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://hastebin.com/raw/%s  ");
        configExceptionChecks.put(Pattern.compile("hasteb\\.in/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://hasteb.in/raw/%s");
        configExceptionChecks.put(Pattern.compile("mclo\\.gs/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://api.mclo.gs/1/raw/%s");
        configExceptionChecks.put(Pattern.compile("pastebin\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://pastebin.com/raw/%s");
        configExceptionChecks.put(Pattern.compile("gist\\.github\\.com/([0-9a-zA-Z]+/)([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://gist.githubusercontent.com/%1$s/%2$s/raw/");
        configExceptionChecks.put(Pattern.compile("paste\\.shockbyte\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://paste.shockbyte.com/raw/%s");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String contents = event.getMessage().getContentRaw();
        String url = null;
        for (Pattern regex : configExceptionChecks.keySet()) {
            Matcher matcher = regex.matcher(contents);

            if (!matcher.find()) {
                continue;
            }

            String[] groups = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                groups[i] = matcher.group(i + 1);
            }

            url = String.format(configExceptionChecks.get(regex), groups);
            break;
        }

        String pasteBody;
        if (url != null) {
            pasteBody = RestClient.get(url);
        } else {
            pasteBody = contents;
        }

        String branch = "master";
        Matcher branchMatcher = BRANCH_PATTERN.matcher(pasteBody);
        if (branchMatcher.find()) {
            branch = branchMatcher.group(1);
        }

        List<StackException> exceptions = Parser.parse(pasteBody);

        if (exceptions.size() == 0) {
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        // Set the base title and description of the embed
        embedBuilder.setTitle("Found errors in the log!");
        embedBuilder.setDescription("See below for details and possible fixes");
        embedBuilder.setColor(0xff0000);

        // Get the github trees for fetching the file paths
        GithubFileFinder fileFinder = new GithubFileFinder(branch);

        for (StackException exception : exceptions) {
            // Can't have more than 12 embed fields as its longer than the 6k characters on average
            if (embedBuilder.getFields().size() >= 12) {
                break;
            }

            String exceptionTitle = exception.getException() + ": " + exception.getDescription();

            // Check if we have a known fix
            Optional<Map.Entry<String, String>> foundFix = configExceptionFixes.entrySet().stream().filter(entry -> entry.getKey().startsWith(exceptionTitle) || exceptionTitle.startsWith(entry.getKey())).findFirst();
            if (foundFix.isPresent() && embedBuilder.getFields().stream().noneMatch(field -> exceptionTitle.equals(field.getName()))) {
                embedBuilder.addField(exceptionTitle, foundFix.get().getValue(), false);
                continue;
            }

            for (StackLine line : exception.getLines()) {
                if (line.getStackPackage() != null && line.getStackPackage().startsWith("org.geysermc") && !line.getStackPackage().contains("shaded")) {
                    // Get the file url
                    String lineUrl = fileFinder.getFileUrl(line.getSource(), Integer.parseInt(line.getLine()));

                    // Build the description
                    String exceptionDesc = "Unknown fix!\nClass: `" + line.getJavaClass() + "`\nMethod: `" + line.getMethod() + "`\nLine: `" + line.getLine() + "`\nLink: " + (lineUrl != "" ? "[" + line.getSource() + "#L" + line.getLine() + "](" + lineUrl + ")" : "Unknown");

                    // Make sure we dont already have that field
                    if (embedBuilder.getFields().stream().noneMatch(field -> exceptionTitle.equals(field.getName()) && exceptionDesc.equals(field.getValue()))) {
                        embedBuilder.addField(BotHelpers.trim(exceptionTitle, 256), exceptionDesc, false);
                    }

                    break;
                }
            }
        }

        // If we have no fields set the description accordingly
        if (embedBuilder.getFields().isEmpty()) {
            embedBuilder.setDescription("We don't currently have automated responses for the detected errors!");
        }

        event.getMessage().replyEmbeds(embedBuilder.build()).queue();
    }
}