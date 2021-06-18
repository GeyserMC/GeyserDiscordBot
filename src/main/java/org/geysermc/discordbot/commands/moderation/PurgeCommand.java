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
import net.dv8tion.jda.api.entities.*;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.LogHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.MessageHelper;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PurgeCommand extends Command {

    public PurgeCommand() {
        this.name = "purge";
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Get the count and validate it
        int count;
        try {
            count = Integer.parseInt(args.get(0));
        } catch (NumberFormatException e) {
            MessageHelper.errorResponse(event, "Invalid count", "Please specify a positive integer for the number of messages to delete!");
            return;
        }

        // Make sure we dont have a invalid number
        if (count <= 0) {
            MessageHelper.errorResponse(event, "Invalid count", "Please specify a positive integer for the number of messages to delete!");
            return;
        }

        // Fetch the user
        User user = null;
        if (args.size() >= 2) {
            user = BotHelpers.getUser(args.get(1));

            // Check user is valid
            if (user == null) {
                MessageHelper.errorResponse(event, "Invalid user", "The user ID specified doesn't link with any valid user in this server.");
                return;
            }
        }

        LogHandler.PURGED_MESSAGES.add(event.getMessage().getId());
        event.getMessage().delete().queue();

        int totalMessages = 0;
        MessageHistory history = event.getChannel().getHistory();
        while (totalMessages < count) {
            List<Message> messagesToDelete = new ArrayList<>();

            // Pull the last 100 messages
            for (Message message : history.retrievePast(100).complete()) {
                if (user != null && message.getAuthor() != user) {
                    continue;
                }

                if (message.getIdLong() == event.getMessage().getIdLong()) {
                    continue;
                }

                messagesToDelete.add(message);
                totalMessages++;

                if (totalMessages >= count) {
                    break;
                }
            }

            // Remove the message(s)
            List<String> messagesToDeleteIds = messagesToDelete.stream().map(message -> message.getId()).collect(Collectors.toList());
            LogHandler.PURGED_MESSAGES.addAll(messagesToDeleteIds);
            if (messagesToDelete.size() > 1) {
                event.getTextChannel().deleteMessagesByIds(messagesToDeleteIds).queue();
            } else if (messagesToDelete.size() == 1) {
                messagesToDelete.get(0).delete().queue();
                break;
            }
        }

        // TODO: Store the contents of removed messages and upload along side the log

        // Log the change
        MessageEmbed bannedEmbed = new EmbedBuilder()
                .setTitle("Purged channel")
                .addField("Channel", event.getTextChannel().getAsMention(), false)
                .addField("Target", user != null ? user.getAsMention() : "None", false)
                .addField("Staff member", event.getAuthor().getAsMention(), false)
                .addField("Count", totalMessages + "/" + count, false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(bannedEmbed).queue();
    }
}
