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

package org.geysermc.discordbot.commands.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;

import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BanCommand extends SlashCommand {

    public BanCommand() {
        this.name = "ban";
        this.hidden = true;
        this.help = "Ban a user";

        this.userPermissions = new Permission[] { Permission.BAN_MEMBERS };

        this.options = Arrays.asList(
                new OptionData(OptionType.USER, "member", "The member to ban", true),
                new OptionData(OptionType.INTEGER, "days", "How many days worth of messages should we purge"),
                new OptionData(OptionType.BOOLEAN, "silent", "Toggle notifying the user upon banning"),
                new OptionData(OptionType.STRING, "reason", "Specify a reason for banning")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Fetch members
        Member member = event.getOption("member").getAsMember();
        Member moderator = event.getMember();

        // Fetch ban args
        int days = event.getOption("days", 0, OptionMapping::getAsInt);
        boolean silent = event.optBoolean("silent", false);
        String reason = event.optString("reason", "*None*");

        event.replyEmbeds(handle(member, moderator, event.getGuild(), days, silent, reason)).queue();
    }

    private MessageEmbed handle(Member member, Member moderator, Guild guild, int days, boolean silent, String reason) {
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
                        .setTitle("You have been banned from GeyserMC!")
                        .addField("Reason", reason, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor());

                String punishmentMessage = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "punishment-message");
                if (punishmentMessage != null && !punishmentMessage.isEmpty()) {
                    embedBuilder.addField("Additional Info", punishmentMessage, false);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {}, throwable -> {});
            }, throwable -> {});
        }

        // Ban user
        guild.ban(user, days, TimeUnit.DAYS).reason(reason).queue();

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
        return bannedEmbed;
    }
}
