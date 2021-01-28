/*
 * Copyright (c) 2020-2021 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.awt.Color;
import java.time.Instant;

public class LogHandler extends ListenerAdapter {

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        Guild.Ban ban = event.getGuild().retrieveBan(event.getUser()).complete();

        // Get the ban from the audit log to get the user that created it
        AuditLogEntry banLog = event.getGuild().retrieveAuditLogs().type(ActionType.BAN).stream().filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == ban.getUser().getIdLong()).findFirst().orElse(null);

        // Don't log bans by the bot (they are handled separately)
        if (banLog.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            // Log the change
            GeyserBot.storageManager.addLog(event.getGuild().getMember(banLog.getUser()), "ban", event.getUser(), ban.getReason());

            // Send the embed as a reply and to the log
            ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                    .setTitle("Banned user")
                    .addField("User", event.getUser().getAsMention(), false)
                    .addField("Staff member", banLog.getUser().getAsMention(), false)
                    .addField("Reason", ban.getReason(), false)
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build()).queue();
        }
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        // Get the unban from the audit log to get the user that created it
        AuditLogEntry banLog = event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).stream().filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == event.getUser().getIdLong()).findFirst().orElse(null);

        // Don't log bans by the bot (they are handled separately)
        if (banLog.getUser().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            // Log the change
            GeyserBot.storageManager.addLog(event.getGuild().getMember(banLog.getUser()), "unban", event.getUser(), "");

            // Send the embed as a reply and to the log
            ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                    .setTitle("Unbanned user")
                    .addField("User", event.getUser().getAsMention(), false)
                    .addField("Staff member", banLog.getUser().getAsMention(), false)
                    .addField("Reason", "", false)
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build()).queue();
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        PrettyTime t = new PrettyTime(Instant.now());
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                .setAuthor("Member Joined", null, event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
                .addField("Account Age", t.format(t.calculatePreciseDuration(event.getUser().getTimeCreated().toInstant())).replace(" ago", ""), false)
                .setThumbnail(event.getUser().getAvatarUrl())
                .setFooter("ID: " + event.getUser().getId())
                .setTimestamp(Instant.now())
                .setColor(Color.green)
                .build()).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                .setAuthor("Member Left", null, event.getUser().getAvatarUrl())
                .setDescription(event.getUser().getAsMention() + " " + event.getUser().getAsTag())
                .setFooter("ID: " + event.getUser().getId())
                .setTimestamp(Instant.now())
                .setColor(Color.green)
                .build()).queue();
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                .setDescription("**Message edited in **" + event.getChannel().getAsMention() + " [Jump to Message](" + event.getMessage().getJumpUrl() + ")")
                //.addField("Before", event.getMessage().getContentRaw(), false)
                .addField("After", event.getMessage().getContentRaw(), false)
                .setFooter("User ID: " + event.getAuthor().getId())
                .setTimestamp(Instant.now())
                .setColor(Color.green)
                .build()).queue();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        for (String inviteCode : event.getMessage().getInvites()) {
            Invite invite = Invite.resolve(event.getJDA(), inviteCode, true).complete();

            ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                    .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                    .setDescription("**Invite posted for " + invite.getGuild().getName() + "** " + event.getChannel().getAsMention() + "\n" + invite.getUrl())
                    .addField("Inviter", invite.getInviter().getAsTag(), true)
                    .addField("Channel", invite.getChannel().getName(), true)
                    .addField("Members", invite.getGuild().getOnlineCount() + "/" + invite.getGuild().getMemberCount(), true)
                    .setFooter("ID: " + event.getAuthor().getId())
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build()).queue();
        }
    }
}
