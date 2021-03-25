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
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.SlowmodeHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotHelpers;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlowmodeCommand extends Command {

    public SlowmodeCommand() {
        this.name = "slowmode";
        this.aliases = new String[] {"slow"};
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Check if a time string was specified
        if (args.size() >= 1 && args.get(0).trim().isEmpty()) {
            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("Invalid usage")
                    .setDescription("Please specify a time in the correct format `1h2m3s`.")
                    .setColor(Color.red)
                    .build()).queue();
            return;
        }

        MessageEmbed slowmodeEmbed;

        if (args.get(0).trim().equals("off")) {
            for (Object listener : event.getJDA().getEventManager().getRegisteredListeners()) {
                if (listener instanceof SlowmodeHandler) {
                    if (((SlowmodeHandler) listener).getChannelId() == event.getTextChannel().getIdLong()) {
                        event.getJDA().getEventManager().unregister(listener);
                        break;
                    }
                }
            }

            GeyserBot.storageManager.setSlowModeChannel(event.getTextChannel(), 0);

            slowmodeEmbed = new EmbedBuilder()
                    .setTitle("Slowmode")
                    .setDescription("Slowmode disabled for " + event.getTextChannel().getAsMention() + " by " + event.getAuthor().getAsMention())
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build();
        } else {
            // Get the time
            int delay = BotHelpers.parseTimeString(args.get(0));

            // Check if time is valid
            if (delay == 0) {
                event.getMessage().reply(new EmbedBuilder()
                        .setTitle("Invalid usage")
                        .setDescription("Please specify a time in the correct format `1h2m3s`.")
                        .setColor(Color.red)
                        .build()).queue();
                return;
            }

            slowmodeEmbed = new EmbedBuilder()
                    .setTitle("Slowmode")
                    .setDescription("Slowmode updated for " + event.getTextChannel().getAsMention() + " set to `" + args.get(0) + "` (" + delay + "s) by " + event.getAuthor().getAsMention())
                    .setTimestamp(Instant.now())
                    .setColor(Color.green)
                    .build();

            boolean found = false;
            for (Object listener : event.getJDA().getEventManager().getRegisteredListeners()) {
                if (listener instanceof SlowmodeHandler) {
                    if (((SlowmodeHandler) listener).getChannelId() == event.getTextChannel().getIdLong()) {
                        ((SlowmodeHandler) listener).setSeconds(delay);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                event.getJDA().addEventListener(new SlowmodeHandler(event.getTextChannel().getIdLong(), delay));
            }

            // Update the db
            GeyserBot.storageManager.setSlowModeChannel(event.getTextChannel(), delay);
        }

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(slowmodeEmbed).queue();
        event.getMessage().reply(slowmodeEmbed).queue();
    }
}
