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
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.listeners.LogHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PurgeCommand extends SlashCommand {

    public PurgeCommand() {
        this.name = "purge";
        this.aliases = new String[] { "prune" };
        this.hidden = true;
        this.help = "Delete specified number of messages";

        this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };
        this.botPermissions = new Permission[] { Permission.MESSAGE_MANAGE };

        this.options = Arrays.asList(
                new OptionData(OptionType.INTEGER, "count", "Number of messages to purge", true).setMinValue(1),
                new OptionData(OptionType.USER, "member", "Remove only this user's messages")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        User user = null;
        MessageHistory history = event.getChannel().getHistory();
        Member moderator = event.getMember();
        int count = event.getOption("count").getAsInt();

        // Make sure we don't have a invalid number
        if (count <= 0) {
            event.reply("Invalid count, Please specify a positive integer for the number of messages to delete!").setEphemeral(true).queue();
            return;
        }

        if (event.hasOption("user")) {
            user = event.getOption("member").getAsUser();

            if (user == null) {
                event.reply("Invalid user, The user ID specified doesn't link with any valid user in this server.").setEphemeral(true).queue();
                return;
            }
        }

        List<String> delList = handle(user, moderator, event.getGuild(), history, count);

        if (delList == null) {
            // Should only return null when it's a single entry
            event.reply("Purged 1/1 messages!").setEphemeral(true).queue();
            return;
        }

        // Remove the extra purge line at the end, so it doesn't interfere
        String purged = delList.get(delList.size()-1);
        delList.remove(delList.size()-1);

        event.getTextChannel().deleteMessagesByIds(delList).queue();
        event.reply("Purged " + purged + " messages!").setEphemeral(true).queue();

    }

    private List<String> handle(User user, Member mod, Guild guild, MessageHistory history, int count) {
        int totalMessages = 0;
        while (totalMessages < count) {
            List<Message> messagesToDelete = new ArrayList<>();

            // Pull the last 100 messages
            for (Message message : history.retrievePast(100).complete()) {
                if (user != null && message.getAuthor() != user) {
                    continue;
                }

                messagesToDelete.add(message);
                totalMessages++;

                if (totalMessages >= count) {
                    break;
                }
            }

            // Remove the message(s)
            List<String> messagesToDeleteIds = messagesToDelete.stream().map(ISnowflake::getId).collect(Collectors.toList());

            if (messagesToDelete.size() > 1) {
                // Tell the log handler to ignore the messages
                LogHandler.PURGED_MESSAGES.addAll(messagesToDeleteIds);

                // TODO: Store the contents of removed messages and upload along side the log
                // Log the change
                MessageEmbed bannedEmbed = new EmbedBuilder()
                        .setTitle("Purged channel")
                        .addField("Channel", history.getChannel().getAsMention(), false)
                        .addField("Target", user != null ? user.getAsMention() : "None", false)
                        .addField("Staff member", mod.getAsMention(), false)
                        .addField("Count", totalMessages + "/" + count, false)
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor())
                        .build();

                // Send the embed as a reply and to the log
                ServerSettings.getLogChannel(guild).sendMessageEmbeds(bannedEmbed).queue();

                if (true) {
                    // If slash command, add purge message count
                    messagesToDeleteIds.add(totalMessages+"/"+count);
                }

                // Return to the main event
                return messagesToDeleteIds;
            } else if (messagesToDelete.size() == 1) {
                messagesToDelete.get(0).delete().queue();
                break;
            }
        }
        return null;
    }
}
