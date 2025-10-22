/*
 * Copyright (c) 2020-2025 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LogHandler extends ListenerAdapter {

    public static final List<String> PURGED_MESSAGES = new ArrayList<>();

    private final Map<Long, Cache<Long, Message>> messageCache = new HashMap<>();

    private Message getCacheMessage(Guild guild, Long messageId) {
        Cache<Long, Message> cache = messageCache.get(guild.getIdLong());
        // Insert the cache if there is none
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .maximumSize(1000)
                    .build();
            messageCache.put(guild.getIdLong(), cache);

            return null;
        }

        return cache.getIfPresent(messageId);
    }

    private void putCacheMessage(Guild guild, Message message) {
        Cache<Long, Message> cache = messageCache.get(guild.getIdLong());
        // Insert the cache if there is none
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(24, TimeUnit.HOURS)
                    .build();
            messageCache.put(guild.getIdLong(), cache);
        }

        cache.put(message.getIdLong(), message);
    }

    private void removeCacheMessage(Guild guild, Long messageId) {
        Cache<Long, Message> cache = messageCache.get(guild.getIdLong());
        if (cache == null) {
            return;
        }

        cache.invalidate(messageId);
    }

    @Override
    public void onGuildAuditLogEntryCreate(@NotNull GuildAuditLogEntryCreateEvent event) {
        if (event.getEntry().getUserIdLong() == event.getJDA().getSelfUser().getIdLong()) return;

        String action;
        String actionTitle;
        UserSnowflake staffUser = User.fromId(event.getEntry().getUserId());
        UserSnowflake targetUser = User.fromId(event.getEntry().getTargetId());
        String reason = event.getEntry().getReason();
        Color color;

        if (reason == null) {
            reason = "";
        }

        switch (event.getEntry().getType()) {
            case BAN -> {
                action = "ban";
                actionTitle = "Banned user";
                color = BotColors.FAILURE.getColor();
            }
            case UNBAN -> {
                action = "unban";
                actionTitle = "Unbanned user";
                color = BotColors.SUCCESS.getColor();
            }
            case KICK -> {
                action = "kick";
                actionTitle = "Kicked user";
                color = BotColors.SUCCESS.getColor();
            }
            case MEMBER_UPDATE -> {
                AuditLogChange timeoutChange = event.getEntry().getChangeByKey(AuditLogKey.MEMBER_TIME_OUT);
                if (timeoutChange != null && timeoutChange.getNewValue() != null) {
                    action = "timeout";
                    actionTitle = "Timed out user until " + TimeFormat.DATE_TIME_SHORT.atInstant(Instant.parse(timeoutChange.getNewValue()));
                    color = BotColors.SUCCESS.getColor();
                } else {
                    return;
                }
            }
            default -> {
                return;
            }
        }

        // Log the change
        int id = GeyserBot.storageManager.addLog(event.getGuild().getMember(staffUser), action, targetUser, reason);

        // Send the embed as a reply and to the log
        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setTitle(actionTitle)
                    .addField("User", targetUser.getAsMention(), false)
                    .addField("Staff member", staffUser.getAsMention(), false)
                    .addField("Reason", reason, false)
                    .setFooter("ID: " + id)
                    .setTimestamp(Instant.now())
                    .setColor(color)
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor("Member Joined", null, event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " " + event.getUser().getName())
                    .addField("Account Created", TimeFormat.RELATIVE.format(event.getUser().getTimeCreated().toInstant()), false)
                    .setThumbnail(event.getUser().getAvatarUrl())
                    .setFooter("ID: " + event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.SUCCESS.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor("Member Left", null, event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " " + event.getUser().getName())
                    .setFooter("ID: " + event.getUser().getId())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.WARNING.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        // Make sure we are in a guild
        if (!event.isFromGuild()) return;

        // Ignore non logged channels
        if (ServerSettings.shouldNotLogChannel(event.getChannel())) {
            return;
        }

        // Ignore bots
        if (event.getAuthor().isBot()) {
            putCacheMessage(event.getGuild(), event.getMessage());
            return;
        }

        Message cachedMessage = getCacheMessage(event.getGuild(), event.getMessage().getIdLong());

        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl())
                    .setDescription("**Message edited in **" + event.getChannel().getAsMention() + " [Jump to Message](" + event.getMessage().getJumpUrl() + ")")
                    .addField("Before", cachedMessage != null ? BotHelpers.trim(cachedMessage.getContentRaw(), 450) : "*Old message not cached*", false)
                    .addField("After", BotHelpers.trim(event.getMessage().getContentRaw(), 450), false)
                    .setFooter("User ID: " + event.getAuthor().getId())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.NEUTRAL.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }

        putCacheMessage(event.getGuild(), event.getMessage());

        filterInvites(event.getMessage());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Make sure we are in a guild
        if (!event.isFromGuild()) return;

        // Ignore non logged channels
        if (ServerSettings.shouldNotLogChannel(event.getChannel())) {
            return;
        }

        // Do this before the invite log just incase its removed
        putCacheMessage(event.getGuild(), event.getMessage());

        filterInvites(event.getMessage());
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        // Make sure we are in a guild
        if (!event.isFromGuild()) return;

        // Don't show purged messages or non-logged channels
        if (PURGED_MESSAGES.remove(event.getMessageId()) || ServerSettings.shouldNotLogChannel(event.getChannel())) {
            return;
        }

        Message cachedMessage = getCacheMessage(event.getGuild(), event.getMessageIdLong());

        String authorTag = "Unknown";
        String authorMention = "Unknown";
        String authorAvatar = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Question_mark_%28black%29.svg/200px-Question_mark_%28black%29.svg.png";
        String authorId = "000000000000000000";
        String message = "*Old message not cached*";

        if (cachedMessage != null) {
            // Don't show delete messages if the author was a bot
            if (cachedMessage.getAuthor().isBot()) {
                removeCacheMessage(event.getGuild(), event.getMessageIdLong());
                return;
            }

            authorTag = cachedMessage.getAuthor().getName();
            authorMention = cachedMessage.getAuthor().getAsMention();
            authorAvatar = cachedMessage.getAuthor().getAvatarUrl();
            authorId = cachedMessage.getAuthor().getId();
            message = cachedMessage.getContentRaw();
        }

        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor(authorTag, null, authorAvatar)
                    .setDescription("**Message sent by** " + authorMention + " **deleted in** " + event.getChannel().getAsMention() + "\n" + BotHelpers.trim(message, 900))
                    .setFooter("Author: " + authorId + " | Message ID: " + event.getMessageId())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.WARNING.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }

        removeCacheMessage(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        boolean isJoin = event.getOldValue() == null && event.getNewValue() != null;
        boolean isLeave = event.getOldValue() != null && event.getNewValue() == null;
        boolean isMove = !isJoin && !isLeave;


        String description = "";
        if (isJoin) {
            description = event.getMember().getAsMention() + " **joined voice channel " + event.getChannelJoined().getAsMention() + "**";
        } else if (isMove) {
            description = event.getMember().getAsMention() + " **switched voice channel " + event.getChannelLeft().getAsMention() + " -> " + event.getChannelJoined().getAsMention() + "**";
        } else if (isLeave) {
            description = event.getMember().getAsMention() + " **left voice channel " + event.getChannelLeft().getAsMention() + "**";
        }

        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor(event.getMember().getUser().getName(), null, event.getMember().getUser().getAvatarUrl())
                    .setDescription(description)
                    .setFooter("ID: " + event.getMember().getId())
                    .setTimestamp(Instant.now())
                    .setColor((isJoin || isMove) ? BotColors.SUCCESS.getColor() : BotColors.FAILURE.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }
    }

    /**
     * Filter invites from messages
     *
     * @param message Message to filter
     */
    private void filterInvites(Message message) {
        for (String inviteCode : message.getInvites()) {
            try {
                Invite invite = Invite.resolve(message.getJDA(), inviteCode, true).complete();

                try {
                    ServerSettings.getLogChannel(message.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                            .setAuthor(message.getAuthor().getName(), null, message.getAuthor().getAvatarUrl())
                            .setDescription("**Invite posted for " + invite.getGuild().getName() + "** " + message.getChannel().getAsMention() + "\n" + invite.getUrl())
                            .addField("Inviter", invite.getInviter() != null ? invite.getInviter().getName() : "Unknown", true)
                            .addField("Channel", invite.getChannel() != null ? invite.getChannel().getName() : "Group", true)
                            .addField("Members", invite.getGuild().getOnlineCount() + "/" + invite.getGuild().getMemberCount(), true)
                            .setFooter("ID: " + message.getAuthor().getId())
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.NEUTRAL.getColor())
                            .build()).queue();
                } catch (IllegalArgumentException ignored) { }

                // Bypass for users with MESSAGE_MANAGE permission
                if (message.getMember() != null && !message.getMember().hasPermission(Permission.MESSAGE_MANAGE) && !ServerSettings.getList(message.getGuild().getIdLong(), "allowed-invites").contains(invite.getGuild().getId())) {
                    message.delete().complete();
                }
            } catch (ErrorResponseException ignored) { }
        }
    }
}
