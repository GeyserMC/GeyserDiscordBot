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
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
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
    public void onGuildBan(@NotNull GuildBanEvent event) {
        Guild.Ban ban = event.getGuild().retrieveBan(event.getUser()).complete();

        // Get the ban from the audit log to get the user that created it
        AuditLogEntry banLog = event.getGuild().retrieveAuditLogs().type(ActionType.BAN).stream().filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == ban.getUser().getIdLong()).findFirst().orElse(null);

        GeyserBot.getGeneralThreadPool().schedule(() -> {
            AuditLogEntry newBanLog;
            if (banLog == null) {
                newBanLog = event.getGuild().retrieveAuditLogs().type(ActionType.BAN).stream().filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == ban.getUser().getIdLong()).findFirst().orElse(null);

                // Still null, shouldn't happen but just quietly fail
                if (newBanLog == null) {
                    return;
                }
            } else {
                newBanLog = banLog;
            }

            // Don't log bans by the bot (they are handled separately)
            if (newBanLog.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
                // Log the change
                int id = GeyserBot.storageManager.addLog(event.getGuild().getMember(newBanLog.getUser()), "ban", event.getUser(), ban.getReason());

                // Send the embed as a reply and to the log
                try {
                    ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                            .setTitle("Banned user")
                            .addField("User", event.getUser().getAsMention(), false)
                            .addField("Staff member", newBanLog.getUser().getAsMention(), false)
                            .addField("Reason", ban.getReason(), false)
                            .setFooter("ID: " + id)
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.FAILURE.getColor())
                            .build()).queue();
                } catch (IllegalArgumentException ignored) { }
            }
        }, banLog == null ? 5 : 0, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        // Get the unban from the audit log to get the user that created it
        AuditLogEntry banLog = event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).stream().filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        // Don't log bans by the bot (they are handled separately)
        if (banLog.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            // Log the change
            int id = GeyserBot.storageManager.addLog(event.getGuild().getMember(banLog.getUser()), "unban", event.getUser(), "");

            // Send the embed as a reply and to the log
            try {
                ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Unbanned user")
                        .addField("User", event.getUser().getAsMention(), false)
                        .addField("Staff member", banLog.getUser().getAsMention(), false)
                        .addField("Reason", "", false)
                        .setFooter("ID: " + id)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.SUCCESS.getColor())
                        .build()).queue();
            } catch (IllegalArgumentException ignored) { }
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        try {
            ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                    .setAuthor("Member Joined", null, event.getUser().getAvatarUrl())
                    .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
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
                    .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
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
                    .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                    .setDescription("**Message edited in **" + event.getChannel().getAsMention() + " [Jump to Message](" + event.getMessage().getJumpUrl() + ")")
                    .addField("Before", cachedMessage != null ? BotHelpers.trim(cachedMessage.getContentRaw(), 450) : "*Old message not cached*", false)
                    .addField("After", BotHelpers.trim(event.getMessage().getContentRaw(), 450), false)
                    .setFooter("User ID: " + event.getAuthor().getId())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.NEUTRAL.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }

        putCacheMessage(event.getGuild(), event.getMessage());
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

        for (String inviteCode : event.getMessage().getInvites()) {
            try {
                Invite invite = Invite.resolve(event.getJDA(), inviteCode, true).complete();

                try {
                    ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                            .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                            .setDescription("**Invite posted for " + invite.getGuild().getName() + "** " + event.getChannel().getAsMention() + "\n" + invite.getUrl())
                            .addField("Inviter", invite.getInviter() != null ? invite.getInviter().getAsTag() : "Unknown", true)
                            .addField("Channel", invite.getChannel() != null ? invite.getChannel().getName() : "Group", true)
                            .addField("Members", invite.getGuild().getOnlineCount() + "/" + invite.getGuild().getMemberCount(), true)
                            .setFooter("ID: " + event.getAuthor().getId())
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.NEUTRAL.getColor())
                            .build()).queue();
                } catch (IllegalArgumentException ignored) { }

                // Bypass for users with MESSAGE_MANAGE permission
                if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE) && !ServerSettings.getList(event.getGuild().getIdLong(), "allowed-invites").contains(invite.getGuild().getId())) {
                    event.getMessage().delete().complete();
                }
            } catch (ErrorResponseException ignored) { }
        }
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

            authorTag = cachedMessage.getAuthor().getAsTag();
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
                    .setAuthor(event.getMember().getUser().getAsTag(), null, event.getMember().getUser().getAvatarUrl())
                    .setDescription(description)
                    .setFooter("ID: " + event.getMember().getId())
                    .setTimestamp(Instant.now())
                    .setColor((isJoin || isMove) ? BotColors.SUCCESS.getColor() : BotColors.FAILURE.getColor())
                    .build()).queue();
        } catch (IllegalArgumentException ignored) { }
    }
}
