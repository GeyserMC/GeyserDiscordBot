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

package org.geysermc.discordbot.commands.moderation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.ModLog;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotHelpers;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogCommand extends Command {

    public LogCommand() {
        this.name = "modlog";
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the user
        User user = BotHelpers.getUser(args.remove(0));

        // Check user is valid
        if (user == null) {
            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user.")
                    .setColor(Color.red)
                    .build()).queue();
            return;
        }

        EmbedBuilder logEmbedBuilder = new EmbedBuilder()
                .setTitle("Mod log for: " + user.getId())
                .setTimestamp(Instant.now())
                .setColor(Color.green);

        List<ModLog> logs = GeyserBot.storageManager.getLog(event.getGuild(), user);

        if (logs.isEmpty()) {
            logEmbedBuilder.setDescription("No logs for the selected user");
        } else {
            for (ModLog log : logs) {
                String title = log.getAction().substring(0, 1).toUpperCase() + log.getAction().substring(1);
                logEmbedBuilder = logEmbedBuilder.addField(title, "**Time:** " + OffsetDateTime.ofInstant(log.getTime(), ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n**By:** " + log.getUser().getAsMention() + "\n**Reason:** " + log.getReason(), false);
            }
        }

        // Send the embed as a reply
        event.getMessage().reply(logEmbedBuilder.build()).queue();
    }
}
