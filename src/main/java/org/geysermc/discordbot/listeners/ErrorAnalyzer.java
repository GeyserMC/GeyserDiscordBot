package org.geysermc.discordbot.listeners;

import com.rtm516.stackparser.Parser;
import com.rtm516.stackparser.StackException;
import com.rtm516.stackparser.StackLine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.GithubFileFinder;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<String, String> issueResponses;
    private final Map<Pattern, String> logUrlPatterns;

    private static final Pattern BRANCH_PATTERN = Pattern.compile("Geyser .* \\(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\\)");

    public ErrorAnalyzer() {
        issueResponses = TagsManager.getIssueResponses();
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
        embedBuilder.setColor(0xFF0000);


        List<StackException> exceptions = Parser.parse(pasteBody);

        if (exceptions.size() != 0) {
            // Get the github trees for fetching the file paths
            String branch = "master";
            Matcher branchMatcher = BRANCH_PATTERN.matcher(pasteBody);
            if (branchMatcher.find()) {
                branch = branchMatcher.group(1);
            }
            GithubFileFinder fileFinder = new GithubFileFinder(branch);

            for (StackException exception : exceptions) {
                // Can't have more than 12 embed fields as its longer than the 6k characters on average
                if (embedBuilder.getFields().size() >= 12) {
                    break;
                }

                String exceptionTitle = exception.getException() + ": " + exception.getDescription();

                // If there is a fix for the exception, add it.
                if (addFixIfPresent(exceptionTitle, embedBuilder)) {
                    continue;
                }

                for (StackLine line : exception.getLines()) {
                    if (line.getStackPackage() != null && line.getStackPackage().startsWith("org.geysermc") && !line.getStackPackage().contains("shaded")) {
                        // Get the file url
                        String lineUrl = fileFinder.getFileUrl(line.getSource(), Integer.parseInt(line.getLine()));

                        // Build the description
                        String exceptionDesc = "Unknown fix!\nClass: `" + line.getJavaClass() + "`\nMethod: `" + line.getMethod() + "`\nLine: `" + line.getLine() + "`\nLink: " + (!lineUrl.isEmpty() ? "[" + line.getSource() + "#L" + line.getLine() + "](" + lineUrl + ")" : "Unknown");

                        // Make sure we don't already have that field
                        if (embedBuilder.getFields().stream().noneMatch(field -> exceptionTitle.equals(field.getName()) && exceptionDesc.equals(field.getValue()))) {
                            embedBuilder.addField(exceptionTitle, exceptionDesc, false);
                        }

                        break;
                    }
                }
            }

        }

        // Add any errors that aren't from stack traces
        for (String issue : issueResponses.keySet()) {
            if (pasteBody.contains(issue)) {
                addFixIfPresent(issue, embedBuilder);
            }
        }






        // If we have no fields set the description accordingly
        if (embedBuilder.getFields().isEmpty()) {
            embedBuilder.setDescription("We don't currently have automated responses for the detected errors!");
        }

        event.getMessage().reply(embedBuilder.build()).queue();
    }

    /**
     * Add an issue and its fix to an EmbedBuilder if the fix exists, and the issue hasn't already been added
     * @param issue The issue to find the fix for
     * @param embedBuilder The embed builder to add to
     * @return true if a fix was added to the EmbedBuilder
     */
    private boolean addFixIfPresent(String issue, EmbedBuilder embedBuilder) {
        String fix = null;
        for (String key : issueResponses.keySet()) {
            if (key.contains(issue) || issue.contains(key)) {
                fix = issueResponses.get(key);
                break;
            }
        }
        if (fix != null && embedBuilder.getFields().stream().noneMatch(field -> issue.equals(field.getName()))) {
            embedBuilder.addField(issue, fix, false);
            return true;
        }
        return false;
    }
}