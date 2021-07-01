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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<String, String> configExceptionFixes;
    private final Map<Pattern, String> configExceptionChecks;

    private static final Pattern BRANCH_PATTERN = Pattern.compile("Geyser .* \\(git-[0-9a-zA-Z]+-([0-9a-zA-Z]{7})\\)");

    public ErrorAnalyzer(Map<String, String> exceptionFixes) {
        configExceptionFixes = new HashMap<>(exceptionFixes);
        configExceptionChecks = new HashMap<>();

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
                    String exceptionDesc = "Unknown fix!\nClass: `" + line.getJavaClass() + "`\nMethod: `" + line.getMethod() + "`\nLine: `" + line.getLine() + "`\nLink: " + (!lineUrl.isEmpty() ? "[" + line.getSource() + "#L" + line.getLine() + "](" + lineUrl + ")" : "Unknown");

                    // Make sure we dont already have that field
                    if (embedBuilder.getFields().stream().noneMatch(field -> exceptionTitle.equals(field.getName()) && exceptionDesc.equals(field.getValue()))) {
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