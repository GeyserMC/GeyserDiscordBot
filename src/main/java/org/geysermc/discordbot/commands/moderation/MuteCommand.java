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
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.ServerSettings;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MuteCommand extends Command {

    public MuteCommand() {
        this.name = "mute";
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Clean input
        String userTag = args.remove(0);
        if (userTag.startsWith("<@!") && userTag.endsWith(">")) {
            userTag = userTag.substring(3, userTag.length() - 1);
        }

        // Fetch the user
        Member member = event.getGuild().getMemberById(userTag);

        // Check user is valid
        if (member == null) {
            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(Color.red)
                    .build()).queue();
            return;
        }

        // Get the user from the member
        User user = member.getUser();
        boolean silent = false;


        // Handle all the option args
        // We clone the args here to prevent a CME
        for (String arg : args.toArray(new String[0])) {
            if (!arg.startsWith("-") || arg.length() < 2) {
                break;
            }

            switch (arg.toCharArray()[1]) {
                // Check for silent flag
                case 's':
                    silent = true;
                    break;


                default:
                    event.getMessage().reply(new EmbedBuilder()
                            .setTitle("Invalid option")
                            .setDescription("The option `" + arg + "` is invalid")
                            .setColor(Color.red)
                            .build()).queue();
                    break;
            }

            args.remove(0);
        }

        // Let the user know they're muted if we are not being silent
        if (!silent) {
            user.openPrivateChannel().queue((channel) ->
                    channel.sendMessage(new EmbedBuilder()
                            .setTitle("You have been muted from GeyserMC!")
                            .addField("Reason", String.join(" ", args), false)
                            .setTimestamp(Instant.now())
                            .setColor(Color.red)
                            .build()).queue());
        }
        // TODO: Add rolepersist
        member.mute(true);

        MessageEmbed mutedEmbed = new EmbedBuilder()
                .setTitle("Muted user")
                .addField("User", user.getAsMention(), false)
                .addField("Staff member", event.getAuthor().getAsMention(), false)
                .addField("Reason", String.join(" ", args), false)
                .setTimestamp(Instant.now())
                .setColor(Color.green)
                .build();

        // Send the embed as a reply and to the log
        ServerSettings.getLogChannel(event.getGuild()).sendMessage(mutedEmbed).queue();
        event.getMessage().reply(mutedEmbed).queue();
    }
}
