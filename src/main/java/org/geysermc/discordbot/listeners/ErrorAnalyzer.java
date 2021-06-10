package org.geysermc.discordbot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorAnalyzer extends ListenerAdapter {
    private final Map<String, String> configExceptionFixes;
    private final Map<Pattern, String> configExceptionChecks;

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

    }
}