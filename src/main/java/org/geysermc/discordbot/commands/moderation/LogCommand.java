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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ModLog;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Add pagination
 */
public class LogCommand extends SlashCommand {

    public LogCommand() {
        this.name = "modlog";
        this.hidden = true;
        this.help = "Fetch a user's moderation logs.";

        this.userPermissions = new Permission[]{Permission.KICK_MEMBERS};
        this.botPermissions = new Permission[]{Permission.KICK_MEMBERS};

        this.guildOnly = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "member", "Member to fetch").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Defer to wait for us to handle the command
        InteractionHook interactionHook = event.deferReply().complete();

        User user = event.getOption("member").getAsUser();
        if (user == null) {
            interactionHook.editOriginalEmbeds(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        interactionHook.editOriginalEmbeds(handle(user, event.getGuild())).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the user
        User user = BotHelpers.getUser(args.remove(0));

        // Check user is valid
        if (user == null) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        // Send the embed as a reply
        event.getMessage().replyEmbeds(handle(user, event.getGuild())).queue();
    }

    private MessageEmbed handle(User user, Guild guild) {
        EmbedBuilder logEmbedBuilder = new EmbedBuilder()
                .setTitle("Mod log for: " + user.getId())
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor());

        List<ModLog> logs = GeyserBot.storageManager.getLogs(guild, user);

        if (logs.isEmpty()) {
            logEmbedBuilder.setDescription("No logs for the selected user");
        } else {
            for (ModLog log : logs) {
                String title = log.action().substring(0, 1).toUpperCase() + log.action().substring(1) + " (" + log.id() + ")";
                logEmbedBuilder.addField(title, "**Time:** " + TimeFormat.DATE_TIME_LONG.format(OffsetDateTime.ofInstant(log.time(), ZoneOffset.UTC)) + "\n**By:** " + log.user().getAsMention() + "\n**Reason:** " + log.reason(), false);
            }
        }

        return logEmbedBuilder.build();
    }
}
