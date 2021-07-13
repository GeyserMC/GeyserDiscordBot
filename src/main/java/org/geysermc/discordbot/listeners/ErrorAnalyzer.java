package org.geysermc.discordbot.listeners;

import com.rtm516.stackparser.Parser;
import com.rtm516.stackparser.StackException;
import com.rtm516.stackparser.StackLine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.GithubFileFinder;
import org.geysermc.discordbot.util.MessageHelper;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<Pattern, String> logUrlPatterns;

    private static final Pattern BRANCH_PATTERN = Pattern.compile("Geyser .* \\(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\\)");

    public ErrorAnalyzer() {
        logUrlPatterns = new HashMap<>();

        // Log url patterns
        logUrlPatterns.put(Pattern.compile("hastebin\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://hastebin.com/raw/%s  ");
        logUrlPatterns.put(Pattern.compile("hasteb\\.in/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://hasteb.in/raw/%s");
        logUrlPatterns.put(Pattern.compile("mclo\\.gs/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://api.mclo.gs/1/raw/%s");
        logUrlPatterns.put(Pattern.compile("pastebin\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://pastebin.com/raw/%s");
        logUrlPatterns.put(Pattern.compile("gist\\.github\\.com/([0-9a-zA-Z]+/)([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://gist.githubusercontent.com/%1$s/%2$s/raw/");
        logUrlPatterns.put(Pattern.compile("paste\\.shockbyte\\.com/([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://paste.shockbyte.com/raw/%s");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String contents = event.getMessage().getContentRaw();
        String url = null;
        for (Pattern regex : logUrlPatterns.keySet()) {
            Matcher matcher = regex.matcher(contents);

            if (!matcher.find()) {
                continue;
            }

            String[] groups = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                groups[i] = matcher.group(i + 1);
            }

            url = String.format(logUrlPatterns.get(regex), groups);
            break;
        }

        String pasteBody;
        if (url != null) {
            pasteBody = RestClient.get(url);
        } else {
            pasteBody = contents;
        }

        // Create the embed and format it
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Found errors in the log!");
        embedBuilder.setDescription("See below for details and possible fixes");
        embedBuilder.setColor(BotColors.FAILURE.getColor());

        List<StackException> exceptions = Parser.parse(pasteBody);

        int embedLength = embedBuilder.length();
        int fieldCount = 0;

        // Add any errors that aren't from stack traces first
        for (String issue : TagsManager.getIssueResponses().keySet()) {
            if (pasteBody.contains(issue)) {
                if (embedLength >= MessageEmbed.EMBED_MAX_LENGTH_BOT || fieldCount >= 25) {
                    // cannot have more than 25 embed fields
                    break;
                }
                int exitCode = addFixIfPresent(issue, embedBuilder);
                if (exitCode > 0) {
                    // Increase our length tally if the Embed was added to
                    embedLength += exitCode;
                    fieldCount++;
                }
            }
        }

        // Add any errors from stacktraces
        if (exceptions.size() != 0) {
            // Get the github trees for fetching the file paths
            String branch = "master";
            Matcher branchMatcher = BRANCH_PATTERN.matcher(pasteBody);
            if (branchMatcher.find()) {
                branch = branchMatcher.group(1);
            }
            GithubFileFinder fileFinder = new GithubFileFinder(branch);

            for (StackException exception : exceptions) {
                if (embedLength >= MessageEmbed.EMBED_MAX_LENGTH_BOT || fieldCount >= 25) {
                    break;
                }

                String exceptionTitle = exception.getException() + ": " + exception.getDescription();

                // If there is a fix for the exception, add it.
                int exitCode = addFixIfPresent(exceptionTitle, embedBuilder);
                if (exitCode > 0) {
                    embedLength += exitCode;
                    fieldCount++;
                } else if (exitCode != -1) {
                    // Only continue if no response exists
                    return;
                }

                // If no fix exists then add some info about the error
                for (StackLine line : exception.getLines()) {
                    if (line.getStackPackage() != null && line.getStackPackage().startsWith("org.geysermc") && !line.getStackPackage().contains("shaded")) {
                        // Get the file url
                        String lineUrl = fileFinder.getFileUrl(line.getSource(), Integer.parseInt(line.getLine()));

                        // Build the description
                        String exceptionDesc = "Unknown fix!\nClass: `" + line.getJavaClass() + "`\nMethod: `" + line.getMethod() + "`\nLine: `" + line.getLine() + "`\nLink: " + (!lineUrl.isEmpty() ? "[" + line.getSource() + "#L" + line.getLine() + "](" + lineUrl + ")" : "Unknown");

                        embedBuilder.addField(exceptionTitle, exceptionDesc, false);
                        embedLength += exceptionTitle.length() + exceptionDesc.length();
                        fieldCount++;

                        break;
                    }
                }
            }
        }

        // If we have any info then send the message
        if (!embedBuilder.getFields().isEmpty()) {

            MessageHelper.truncateFields(embedBuilder);
            event.getMessage().reply(embedBuilder.build()).queue();
        }
    }

    /**
     * Add an issue and its fix to an {@link EmbedBuilder} if the fix exists, and the issue hasn't already been added
     *
     * @param issue The issue to find the fix for
     * @param embedBuilder The embed builder to add to
     * @return A positive integer equal to the size of the {@link MessageEmbed.Field} added to the {@link EmbedBuilder} if the issue wasn't already listed and a fix exists.
     * Will return -2 if the issue was already listed. Will return -1 if no fix was found.
     */
    private int addFixIfPresent(String issue, EmbedBuilder embedBuilder) {
        if (MessageHelper.similarFieldExists(embedBuilder.getFields(), issue)) {
            return -2;
        }

        String fix = null;
        for (String key : TagsManager.getIssueResponses().keySet()) {
            if (key.contains(issue) || issue.contains(key)) {
                fix = TagsManager.getIssueResponses().get(key);
                break;
            }
        }

        if (fix == null) {
            return -1;
        } else {
            String title = issue.trim();
            if (title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
                GeyserBot.LOGGER.warn("Issue response with Field name: '" + title + "' has a Field name greater than " + MessageEmbed.TITLE_MAX_LENGTH + " characters! Truncating field name.");
                title = title.substring(0, MessageEmbed.TITLE_MAX_LENGTH);
            }

            String response = fix.trim();
            if (response.length() > MessageEmbed.VALUE_MAX_LENGTH) {
                GeyserBot.LOGGER.warn("Issue response with Field name: '" + title + "' has a Field value greater than " + MessageEmbed.VALUE_MAX_LENGTH + " characters! Truncating Field value.");
                response = response.substring(0, MessageEmbed.VALUE_MAX_LENGTH);
            }

            embedBuilder.addField(title, response, false);
            return fix.length();
        }
    }
}