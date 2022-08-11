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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuteCommand extends SlashCommand {

    public MuteCommand() {
        this.name = "mute";
        this.hidden = true;
        this.help = "Mute a user.";

        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
        this.botPermissions = new Permission[] {Permission.KICK_MEMBERS};
        this.guildOnly = true;

        this.options = Arrays.asList(
                new OptionData(OptionType.USER, "member", "The member to mute").setRequired(true),
                new OptionData(OptionType.BOOLEAN, "silent", "Toggle notifying the user on mute").setRequired(false),
                new OptionData(OptionType.STRING, "reason", "Specify a reason for the mute").setRequired(false)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Fetch users
        Member member = event.getOption("member").getAsMember();
        Member moderator = event.getMember();

        //Fetch kick args
        boolean silent = event.optBoolean("silent", false);
        String reason = event.optString("reason", "*None*");

        if (member == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        // Check we can target the user
        if (!event.getMember().canInteract(member) || member.getIdLong() == GeyserBot.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Higher role")
                    .setDescription("Either the bot or you cannot target that user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        event.replyEmbeds(handle(member, moderator, event.getGuild(), silent, reason)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the user
        Member member = BotHelpers.getMember(event.getGuild(), args.remove(0));

        //Fetch the user that issued the command
        Member moderator = event.getMember();

        // Check user is valid
        if (member == null) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        // Check we can target the user
        if (!event.getSelfMember().canInteract(member) || !moderator.canInteract(member)) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setTitle("Higher role")
                    .setDescription("Either the bot or you cannot target that user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        boolean silent = false;

        // Handle all the option args
        // We clone the args here to prevent a CME
        for (String arg : args.toArray(new String[0])) {
            if (!arg.startsWith("-") || arg.length() < 2) {
                break;
            }

            // Check for silent flag
            if (arg.toCharArray()[1] == 's') {
                silent = true;
            } else {
                event.getMessage().replyEmbeds(new EmbedBuilder()
                        .setTitle("Invalid option")
                        .setDescription("The option `" + arg + "` is invalid")
                        .setColor(BotColors.FAILURE.getColor())
                        .build()).queue();
            }

            args.remove(0);
        }

        // Get the reason or use None
        String reasonParts = String.join(" ", args);
        String reason;
        if (reasonParts.trim().isEmpty()) {
            reason = "*None*";
        } else {
            reason = reasonParts;
        }

        event.getMessage().replyEmbeds(handle(member, moderator, event.getGuild(), silent, reason)).queue();
    }

    private MessageEmbed handle(Member member, Member mod, Guild guild, boolean silent, String reason) {
        // Get the user from the member
        User user = member.getUser();

        // Let the user know they're muted if we are not being silent
        if (!silent) {
            user.openPrivateChannel().queue((channel) -> {
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setTitle("You have been muted from GeyserMC!")
                        .addField("Reason", reason, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor());

                String punishmentMessage = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "punishment-message");
                if (!punishmentMessage.isEmpty()) {
                    embedBuilder.addField("Additional Info", punishmentMessage, false);
                }

                channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {}, throwable -> {});
            }, throwable -> {});
        }

        // Find and add the 'muted' role
        Role muteRole = guild.getRolesByName("muted", true).get(0);
        guild.addRoleToMember(member, muteRole).queue();

        // Persist the role
        GeyserBot.storageManager.addPersistentRole(member, muteRole);

        // Log the change
        int id = GeyserBot.storageManager.addLog(member, "mute", user, reason);

        MessageEmbed mutedEmbed = new EmbedBuilder()
                .setTitle("Muted user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", mod.getAsMention(), false)
                .addField("Reason", reason, false)
                .setFooter("ID: " + id)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(guild).sendMessageEmbeds(mutedEmbed).queue();
        return mutedEmbed;
    }
}
