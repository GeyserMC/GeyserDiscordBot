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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.BotColors;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HelpHandler extends ListenerAdapter {

    private final Cache<User, Integer> messageCache;

    public HelpHandler() {
        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        // Check if member has joined within 10 days from current date.
        Member member = event.getMember();
        assert member != null;

        if (member.getTimeJoined().toLocalDate().plusDays(12).isBefore(LocalDate.now())) {
            return;
        }
        // We only send help embed once every 30 minutes to specific member.
        Integer count = messageCache.getIfPresent(event.getAuthor());

        if (count == null) {
            messageCache.put(event.getAuthor(), 1);
        }

        if (count != null && count >= 1) {
            return;
        }

        String discordMessage = event.getMessage().getContentRaw().toLowerCase();
        // Split discord message so we can compare keywords.
        String[] discordMessageSplit = discordMessage.split(" ");
        String[] keywordsToCheck = {"pls", "please", "help", "me", "need", "how", "install", "setup", "to", "can"};
        String[] tagKeywords = {"help", "floodgate", "geyser", "update", "reload", "portforward", "1.8"};
        List<String> commonKeywords = new ArrayList<>();
        // Check for keywords in the discord message.
        for (String s : discordMessageSplit) {
            for (String word : keywordsToCheck) {
                if (s.equals(word)) {
                    // Keywords found and added to the commonWords list
                    commonKeywords.add(s);
                    break;
                }
            }
        }
        // If list contains 2 common keywords and has least 1 tag keyword send an embed + help tag.
        if (commonKeywords.size() > 1 && CollectionUtils.containsAny(Arrays.asList(discordMessageSplit), tagKeywords)) {
            List<String> listKeyWords = List.of(tagKeywords);
            int index = 0;
            for (int i = 0; i <= tagKeywords.length - 1; i++) {
                if (discordMessage.contains(tagKeywords[i])) {
                    index = i;
                }
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Help Provider");
            embedBuilder.setDescription(TagsManager.getSelfHelp().get(listKeyWords.get(index)));
            embedBuilder.addField("GeyserWiki", "https://wiki.geysermc.org", false);
            embedBuilder.addField("Geyser Discord Bot Usage and Tools", "https://wiki.geysermc.org/other/discord-bot-usage/", false);
            embedBuilder.setColor(BotColors.SUCCESS.getColor());

            event.getMessage().replyEmbeds(embedBuilder.build()).queue();
        }
    }
}