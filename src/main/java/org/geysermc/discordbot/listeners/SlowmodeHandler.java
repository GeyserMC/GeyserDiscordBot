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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.geysermc.discordbot.util.BotColors;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlowmodeHandler extends ListenerAdapter {
    private final long channelId;
    private int seconds;
    private final Cache<OffsetDateTime, User> messageCache;

    public SlowmodeHandler(long channelId, int seconds) {
        this.channelId = channelId;
        this.seconds = seconds;

        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(seconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Make sure we are in a guild
        if (!event.isFromGuild()) return;

        if (this.channelId == event.getMessage().getChannel().getIdLong()) {
            if (canBypassSlowMode(event.getMember())) {
                return;
            }

            OffsetDateTime dateTime = getLastPosted(event.getAuthor());
            if (dateTime != null) {
                event.getMessage().delete().queue(unused ->
                        event.getAuthor().openPrivateChannel().queue(channel ->
                                channel.sendMessageEmbeds(new EmbedBuilder()
                                        .setTitle("Message deleted")
                                        .appendDescription("Your message has been removed because there is still a delay before you can type!")
                                        .addField("Channel", event.getMessage().getChannel().getAsMention(), false)
                                        .addField("When you can post again", TimeFormat.RELATIVE.format(dateTime.plusSeconds(seconds).toInstant()), false)
                                        .setTimestamp(Instant.now())
                                        .setColor(BotColors.FAILURE.getColor())
                                        .build()).queue()));
            } else {
                // Don't add to the cache if a message is deleted - that way the timer doesn't reset.
                this.messageCache.put(event.getMessage().getTimeCreated(), event.getAuthor());
            }
        }
    }

    private boolean canBypassSlowMode(Member member) {
        return member.getUser().isBot() || member.hasPermission(Permission.MESSAGE_MANAGE);
    }

    private OffsetDateTime getLastPosted(User user) {
        for (Map.Entry<OffsetDateTime, User> entry : messageCache.asMap().entrySet()) {
            if (entry.getValue().getIdLong() == user.getIdLong()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
