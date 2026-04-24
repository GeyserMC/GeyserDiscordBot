/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ModerationHelper {
    private ModerationHelper() {}

    public static void quarantineMember(Member user, Guild guild, String reason, boolean automatic, @Nullable Member staffMember) {
        if (staffMember == null) staffMember = guild.getSelfMember();

        String title;

        if (automatic) {
            title = "You have been automatically quarantined from " + guild.getName() + "!";
        } else {
            title = "You have been quarantined from " + guild.getName() + "!";
        }

        user.getUser().openPrivateChannel().queue((channel) -> {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(title)
                    .addField("Reason", reason, false)
                    .addField("Recommended Actions", "Change your Discord password, enable 2FA, and scan your computer for malware. See [Discord's article](https://support.discord.com/hc/en-us/articles/24160905919511-My-Discord-Account-was-Hacked-or-Compromised) for more info.", false)
                    .addField("Information", "If you believe this quarantine was an accident or a false flag, please reach out to a member of staff in order to get this sorted.", false)
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.WARNING.getColor())
                    .build();

            channel.sendMessageEmbeds(embed).queue();
        });

        Duration duration = Duration.ofSeconds(60 * 60 * 24 * 28); // 28 days
        user.timeoutFor(duration).queue();

        // Send a message in the mod chat with buttons to take action
        ActionRow row1 = ActionRow.of(
                Button.success("quarantine-unquarantine", "Unquarantine"),
                Button.primary("quarantine-misuse", "Honey pot misuse"),
                Button.danger("quarantine-compromise", "Compromised account"),
                Button.primary("quarantine-timeout", "Timeout (1 week)"),
                Button.secondary("quarantine-kick", "Kick")
        );

        ActionRow row2 = ActionRow.of(
                Button.danger("quarantine-ban", "Ban (1 week)")
        );

        MessageEmbed modChatEmbed = new EmbedBuilder()
                .setTitle("Quarantined user")
                .setDescription(user.getAsMention() + " has been quarantined. Select an action below to take. Quarantine expires in <t:%d:R>.".formatted(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 28))
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();

        ServerSettings.getModChannel(guild).sendMessage(
                new MessageCreateBuilder()
                        .setContent(user.getAsMention())
                        .setEmbeds(modChatEmbed)
                        .build()
        ).addComponents(row1, row2).queue(message -> {
            Role moderationRole = ServerSettings.getModRole(guild);
            if (moderationRole != null) {
                message.reply(moderationRole.getAsMention())
                        .setAllowedMentions(null) // Allows the ping, null means all confusingly
                        .queue();
            }
        });

        // Now log it!
        int id = GeyserBot.storageManager.addLog(staffMember, "quarantine", user, reason);

        MessageEmbed quarantinedEmbed = new EmbedBuilder()
                .setTitle("Quarantined user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", staffMember.getAsMention(), false)
                .addField("Reason", reason, false)
                .setFooter("ID: " + id)
                .setTimestamp(Instant.now())
                .setColor(BotColors.WARNING.getColor())
                .build();

        ServerSettings.getLogChannel(guild).sendMessageEmbeds(quarantinedEmbed).queue();
    }

    public static MessageEmbed timeoutUser(Member member, Member moderator, Guild guild, Duration duration, boolean silent, String reason) {
        if (moderator == null) moderator = guild.getSelfMember();

        // Check the user exists
        if (member == null) {
            return new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        // Check we can target the user
        if (!BotHelpers.canTarget(moderator, member)) {
            return new EmbedBuilder()
                    .setTitle("Higher role")
                    .setDescription("Either the bot or you cannot target that user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        User user = member.getUser();

        // Let the user know they're timed out if we are not being silent
        if (!silent) {
            user.openPrivateChannel().queue((channel) -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("You have been timed out from " + guild.getName() + "!")
                        .addField("Reason", reason, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor());

                String punishmentMessage = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "punishment-message");
                if (punishmentMessage != null && !punishmentMessage.isEmpty()) {
                    embedBuilder.addField("Additional Info", punishmentMessage, false);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                    // Timeout user
                    guild.timeoutFor(user, duration).reason(reason).queue();
                }, throwable -> {
                    // Timeout user
                    guild.timeoutFor(user, duration).reason(reason).queue();
                });
            }, throwable -> {
                // Timeout user
                guild.timeoutFor(user, duration).reason(reason).queue();
            });
        } else {
            // Timeout user
            guild.timeoutFor(user, duration).reason(reason).queue();
        }

        // Log the change
        int id = GeyserBot.storageManager.addLog(moderator, "timeout", user, reason);

        MessageEmbed timedOutEmbed = new EmbedBuilder()
                .setTitle("Timed out user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", moderator.getAsMention(), false)
                .addField("Reason", reason, false)
                .setFooter("ID: " + id)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(guild).sendMessageEmbeds(timedOutEmbed).queue();
        return timedOutEmbed;
    }

    public static MessageEmbed kickUser(Member member, Member moderator, Guild guild, boolean silent, String reason) {
        if (moderator == null) moderator = guild.getSelfMember();

        // Check the user exists
        if (member == null) {
            return new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        // Check we can target the user
        if (!BotHelpers.canTarget(moderator, member)) {
            return new EmbedBuilder()
                    .setTitle("Higher role")
                    .setDescription("Either the bot or you cannot target that user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        User user = member.getUser();

        // Let the user know they're kicked if we are not being silent
        if (!silent) {
            user.openPrivateChannel().queue((channel) -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("You have been kicked from " + guild.getName() + "!")
                        .addField("Reason", reason, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor());

                String punishmentMessage = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "punishment-message");
                if (punishmentMessage != null && !punishmentMessage.isEmpty()) {
                    embedBuilder.addField("Additional Info", punishmentMessage, false);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                    // Kick user
                    guild.kick(user).reason(reason).queue();
                }, throwable -> {
                    // Kick user
                    guild.kick(user).reason(reason).queue();
                });
            }, throwable -> {
                // Kick user
                guild.kick(user).reason(reason).queue();
            });
        } else {
            // Kick user
            guild.kick(user).reason(reason).queue();
        }

        // Log the change
        int id = GeyserBot.storageManager.addLog(moderator, "kick", user, reason);

        MessageEmbed kickedEmbed = new EmbedBuilder()
                .setTitle("Kicked user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", moderator.getAsMention(), false)
                .addField("Reason", reason, false)
                .setFooter("ID: " + id)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(guild).sendMessageEmbeds(kickedEmbed).queue();
        ServerSettings.getModChannel(guild).sendMessageEmbeds(kickedEmbed).queue();
        return kickedEmbed;
    }

    public static MessageEmbed banUser(Member member, Member moderator, Guild guild, int days, boolean silent, String reason) {
        if (moderator == null) moderator = guild.getSelfMember();

        // Check the user exists
        if (member == null) {
            return new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        // Check we can target the user
        if (!BotHelpers.canTarget(moderator, member)) {
            return new EmbedBuilder()
                    .setTitle("Higher role")
                    .setDescription("Either the bot or you cannot target that user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        User user = member.getUser();

        // Let the user know they're banned if we are not being silent
        if (!silent) {
            user.openPrivateChannel().queue((channel) -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("You have been banned from " + guild.getName() + "!")
                        .addField("Reason", reason, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor());

                String punishmentMessage = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "punishment-message");
                if (punishmentMessage != null && !punishmentMessage.isEmpty()) {
                    embedBuilder.addField("Additional Info", punishmentMessage, false);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                    // Ban user
                    guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();
                }, throwable -> {
                    // Ban user
                    guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();
                });
            }, throwable -> {
                // Ban user
                guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();
            });
        } else {
            // Ban user
            guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();
        }

        // Log the change
        int id = GeyserBot.storageManager.addLog(moderator, "ban", user, reason);

        MessageEmbed bannedEmbed = new EmbedBuilder()
                .setTitle("Banned user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", moderator.getAsMention(), false)
                .addField("Reason", reason, false)
                .setFooter("ID: " + id)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(guild).sendMessageEmbeds(bannedEmbed).queue();
        ServerSettings.getModChannel(guild).sendMessageEmbeds(bannedEmbed).queue();
        return bannedEmbed;
    }
}
