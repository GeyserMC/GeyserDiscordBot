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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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

public class ReasonCommand extends Command {

    public ReasonCommand() {
        this.name = "reason";
        this.aliases = new String[] { "case" };
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the entry
        int logId = Integer.parseInt(args.remove(0));
        ModLog log = GeyserBot.storageManager.getLog(event.getGuild(), logId);

        // Check user is valid
        if (log == null) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid moderation log")
                    .setDescription("The moderation log ID specified doesn't exist in the database for this guild.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        EmbedBuilder logEmbedBuilder = new EmbedBuilder()
                .setTitle("Mod log: " + logId)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .addField("Action", log.action().substring(0, 1).toUpperCase() + log.action().substring(1), true)
                .addField("Target", log.target().getAsMention(), true)
                .addField("By", log.user().getAsMention(), true)
                .addField("Time", TimeFormat.DATE_TIME_LONG.format(OffsetDateTime.ofInstant(log.time(), ZoneOffset.UTC)), false);

        String newReason = String.join(" ", args);

        if (!newReason.trim().isEmpty()) {
            logEmbedBuilder
                    .setTitle("Updated mod log: " + logId)
                    .addField("Old Reason", log.reason(), false)
                    .addField("New Reason", newReason, false);

            GeyserBot.storageManager.updateLog(event.getGuild(), logId, newReason);
        } else {
            logEmbedBuilder.addField("Reason", log.reason(), false);
        }

        // Send the embed as a reply
        event.getMessage().replyEmbeds(logEmbedBuilder.build()).queue();
    }
}
