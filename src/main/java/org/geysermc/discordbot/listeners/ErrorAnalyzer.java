package org.geysermc.discordbot.listeners;

import com.rtm516.stackparser.Parser;
import com.rtm516.stackparser.StackException;
import com.rtm516.stackparser.StackLine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.util.GithubFileFinder;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<String, String> configExceptionFixes;
    private final Map<Pattern, String> configExceptionChecks;

    private static Pattern BRANCH_PATTERN = Pattern.compile("Geyser .* \\(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\\)");

    public ErrorAnalyzer() {
        configExceptionFixes = new HashMap<>();
        configExceptionChecks = new HashMap<>();

        // Seed
        configExceptionChecks.put(Pattern.compile("gist\\.github\\.com\\/([0-9a-zA-Z]+\\/)([0-9a-zA-Z]+)", Pattern.CASE_INSENSITIVE), "https://gist.githubusercontent.com/%1$s/%2$s/raw/");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String contents = event.getMessage().getContentRaw();
        String url = null;
        for (Pattern regex : configExceptionChecks.keySet()) {
            Matcher matcher = regex.matcher(contents);

            if (!matcher.find()) {
                return;
            }

            url = String.format(configExceptionChecks.get(regex), matcher.group(1), matcher.group(2));
        }

        String pasteBody = RestClient.get(url);

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
            String exceptionDesc;

            for (StackLine line : exception.getLines()) {
                if (line.getStackPackage() != null && line.getStackPackage().startsWith("org.geysermc") && !line.getStackPackage().contains("shaded")) {
                    // Get the file url
                    String lineUrl = fileFinder.getFileUrl(line.getSource(), Integer.parseInt(line.getLine()));

                    // Build the description
                    exceptionDesc = "Unknown fix!\nClass: `" + line.getJavaClass() + "`\nMethod: `" + line.getMethod() + "`\nLine: `" + line.getLine() + "`\nLink: " + (lineUrl != "" ? "[" + line.getSource() + "#L" + line.getLine() + "](" + lineUrl + ")" : "Unknown");

                    // Make sure we dont already have that field
                    String finalExceptionDesc = exceptionDesc;
                    if (embedBuilder.getFields().stream().noneMatch(field -> exceptionTitle.equals(field.getName()) && finalExceptionDesc.equals(field.getValue()))) {
                        embedBuilder.addField(exceptionTitle, exceptionDesc, false);
                    }

                    break;
                }
            }
        }

        // If we have no fields set the description acordingly
        if (embedBuilder.getFields().isEmpty()) {
            embedBuilder.setDescription("We don't currently have automated responses for the detected errors!");
        }

        event.getMessage().reply(embedBuilder.build()).queue();
    }
}