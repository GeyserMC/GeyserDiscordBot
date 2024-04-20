/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.kohsuke.github.GHPullRequest;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreviewHandler extends ListenerAdapter {
    private static final Pattern GH_PR_PATTERN = Pattern.compile("https://github\\.com/GeyserMC/(.+)/pull/(\\d+)");

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        NewsChannel previewFeedsChannel = ServerSettings.getPreviewFeedsChannel(event.getGuild());
        if (previewFeedsChannel == null) return;
        if (event.getChannel().getIdLong() != previewFeedsChannel.getIdLong()) return;

        // Create a post in the preview forum channel with this content
        ForumChannel previewChannel = ServerSettings.getPreviewChannel(event.getGuild());
        if (previewChannel == null) return;

        // Check if the message contains a GitHub PR link and extract the repo and PR
        // number via matcher
        String content = event.getMessage().getContentRaw();

        Matcher matcher = GH_PR_PATTERN.matcher(content);
        if (!matcher.find()) {
            return;
        }

        String repo = matcher.group(1);
        String pr = matcher.group(2);

        GHPullRequest pullRequest;
        try {
            pullRequest = GeyserBot.getGithub().getRepository("GeyserMC/" + repo).getPullRequest(Integer.parseInt(pr));
        } catch (IOException e) {
            // The linked PR does not exist
            return;
        }

        previewChannel.createForumPost(
                pullRequest.getTitle(),
                MessageCreateData.fromEmbeds(new EmbedBuilder()
                        .setTitle(pullRequest.getTitle())
                        .setColor(BotColors.SUCCESS.getColor())
                        .setDescription(event.getMessage().getContentRaw())
                        .setAuthor(event.getAuthor().getEffectiveName(), null,
                                event.getAuthor().getEffectiveAvatarUrl())
                        .setImage("https://opengraph.githubassets.com/1/GeyserMC/" + repo + "/pull/" + pr)
                        .setTimestamp(Instant.now())
                        .build()))
                .addActionRow(
                        Button.link(String.valueOf(pullRequest.getHtmlUrl()), "Discuss on GitHub")
                                .withEmoji(Emoji.fromUnicode("\ud83d\udcac")),
                        Button.link(pullRequest.getHtmlUrl() + "/files", "View Changes")
                                .withEmoji(Emoji.fromUnicode("\ud83d\udcdd")),
                        Button.link(pullRequest.getHtmlUrl() + "/checks", "Download Artifacts")
                                .withEmoji(Emoji.fromUnicode("\ud83d\udce6")))
                .queue(forumPost -> {
                    // Reply to the original message with the link to the forum post
                    event.getMessage().replyEmbeds(new EmbedBuilder()
                            .setColor(BotColors.SUCCESS.getColor())
                            .setDescription("The above preview can be discussed in:\n### <#"
                                    + forumPost.getMessage().getId() + ">")
                            .setTimestamp(Instant.now())
                            .build()).queue();
                });

        // Remove embeds from the original message
        event.getMessage().suppressEmbeds(true).queue();

        // Publish message
        event.getMessage().crosspost().queue();
    }
}
