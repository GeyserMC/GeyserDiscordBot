package org.geysermc.discordbot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ForumPostAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import javax.annotation.Nonnull;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreviewHandler extends ListenerAdapter {
    private static final Pattern GH_PR_PATTERN = Pattern.compile("https://github\\.com/GeyserMC/(.+)/pull/(\\d+)");

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        NewsChannel previewFeedsChannel = ServerSettings.getPreviewFeedsChannel(event.getGuild());
        if (previewFeedsChannel == null) return;
        if (event.getChannel().getIdLong() != previewFeedsChannel.getIdLong()) return;

        // Create a post in the preview forum channel with this content
        ForumChannel previewChannel = ServerSettings.getPreviewChannel(event.getGuild());
        if (previewChannel == null) return;

        // Check if the message contains a GitHub PR link and extract the repo and PR number via matcher
        String content = event.getMessage().getContentRaw();
        String repo, pr;
        Matcher matcher = GH_PR_PATTERN.matcher(content);
        if (matcher.find()) {
            repo = matcher.group(1);
            pr = matcher.group(2);
        } else {
            return;
        }
        
        RestClient.RestResponse<JSONObject> restResponse =
            RestClient.getJsonObject("https://api.github.com/repos/GeyserMC/" + repo + "/pulls/" + pr);
        JSONObject serverResponse = restResponse.body();
        
        if (serverResponse.has("message")) {
            // The linked PR does not exist
            return;
        }

        ForumPostAction post = previewChannel.createForumPost(
            serverResponse.getString("title"), 
            MessageCreateData.fromEmbeds((new EmbedBuilder()
                .setTitle(serverResponse.getString("title"))
                .setColor(BotColors.SUCCESS.getColor())
                .setDescription(event.getMessage().getContentRaw())
                .setAuthor(event.getAuthor().getEffectiveName(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setImage("https://opengraph.githubassets.com/1/GeyserMC/" + repo + "/pull/" + pr)
                .setTimestamp(Instant.now())
                .build())))
            .addActionRow(
                Button.link(serverResponse.getString("html_url"), "Discuss on GitHub").withEmoji(Emoji.fromFormatted("💬")),
                Button.link(serverResponse.getString("html_url") + "/files", "View Changes").withEmoji(Emoji.fromFormatted("📝")),
                Button.link(serverResponse.getString("html_url") + "/checks", "Download Artifacts").withEmoji(Emoji.fromFormatted("📦"))
            ); 
        post.queue( forumPost -> {
            // Reply to the original message with the link to the forum post
            event.getMessage().replyEmbeds(new EmbedBuilder()
                .setColor(BotColors.SUCCESS.getColor())
                .setDescription("The above preview can be discussed in:\n### <#" + forumPost.getMessage().getId() + ">")
                .setTimestamp(Instant.now())
                .build()).queue();            
        });
        
        // Remove embeds from the original message
        event.getMessage().suppressEmbeds(true).queue();

        // Publish message
        event.getMessage().crosspost().queue();
    }
}
