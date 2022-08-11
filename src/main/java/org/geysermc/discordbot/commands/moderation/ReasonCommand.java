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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ModLog;
import org.geysermc.discordbot.util.BotColors;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReasonCommand extends SlashCommand {

    public ReasonCommand() {
        this.name = "reason";
        this.aliases = new String[] { "case" };
        this.help = "Get info on a moderation event";
        this.hidden = true;

        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
        this.botPermissions = new Permission[] { Permission.KICK_MEMBERS };

        this.guildOnly = false;
        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER, "id", "The event ID to fetch").setRequired(true),
                new OptionData(OptionType.STRING, "reason", "Set the reason").setRequired(false)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Defer to wait for us to handle the command
        InteractionHook interactionHook = event.deferReply().complete();

        int logID = event.getOption("id").getAsInt();
        String updatedReason;

        // Get reason if specified
        if (event.hasOption("reason")) {
            updatedReason = event.getOption("reason").getAsString();
        } else {
            updatedReason = null;
        }

        interactionHook.editOriginalEmbeds(handle(logID, updatedReason, event.getGuild())).queue();

    }

    @Override
    protected void execute(CommandEvent event) {
        MessageEmbed embed;
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the entry
        int logId = Integer.parseInt(args.remove(0));
        String newReason = String.join(" ", args);

        if (newReason.trim().isEmpty()) {
            embed = handle(logId, null, event.getGuild());
        } else {
            embed = handle(logId, newReason, event.getGuild());
        }

        event.getMessage().replyEmbeds(embed).queue();
    }

    private MessageEmbed handle(int logId, String updatedReason, Guild guild) {
        // Fetch log
        ModLog log = GeyserBot.storageManager.getLog(guild, logId);

        // Check log is valid
        if (log == null) {
            return new EmbedBuilder()
                    .setTitle("Invalid moderation log")
                    .setDescription("The moderation log ID specified doesn't exist in the database for this guild.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        EmbedBuilder logEmbedBuilder = new EmbedBuilder()
                .setTitle("Mod log: " + logId)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .addField("Action", log.action().substring(0, 1).toUpperCase() + log.action().substring(1), true)
                .addField("Target", log.target().getAsMention(), true)
                .addField("By", log.user().getAsMention(), true)
                .addField("Time", TimeFormat.DATE_TIME_LONG.format(OffsetDateTime.ofInstant(log.time(), ZoneOffset.UTC)), false);

        if (updatedReason != null) {
            logEmbedBuilder
                    .setTitle("Updated mod log: " + logId)
                    .addField("Old Reason", log.reason(), false)
                    .addField("New Reason", updatedReason, false);

            GeyserBot.storageManager.updateLog(guild, logId, updatedReason);
        } else {
            logEmbedBuilder.addField("Reason", log.reason(), false);
        }

        // Send the embed as a reply
        return logEmbedBuilder.build();
    }
}
