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

package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.ServerSettings;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RankCommand extends Command {

    public RankCommand() {
        this.name = "rank";
        this.aliases = new String[] {"role"};
        this.arguments = "<role>";
        this.help = "Give yourself a role";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        List<String> roles = ServerSettings.getList(event.getGuild().getIdLong(), "roles");
        for (String roleData : roles) {
            String[] data = roleData.split("\\|");
            if (args.get(0).equals(data[0])) {
                Role role = event.getGuild().getRoleById(data[1]);
                if (role == null) {
                    event.getMessage().reply(new EmbedBuilder()
                            .setTitle("Invalid role")
                            .setDescription("Invalid role specified in configuration")
                            .setTimestamp(Instant.now())
                            .setColor(Color.red)
                            .build()).queue();
                    return;
                }

                if (event.getMember().getRoles().contains(role)) {
                    event.getGuild().removeRoleFromMember(event.getMember(), role).queue();
                    event.getMessage().reply(new EmbedBuilder()
                            .setTitle("Removed role")
                            .setDescription("Removed " + role.getAsMention() + " from " + event.getMember().getAsMention())
                            .setTimestamp(Instant.now())
                            .setColor(Color.green)
                            .build()).queue();
                } else {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                    event.getMessage().reply(new EmbedBuilder()
                            .setTitle("Granted role")
                            .setDescription("Given " + role.getAsMention() + " to " + event.getMember().getAsMention())
                            .setTimestamp(Instant.now())
                            .setColor(Color.green)
                            .build()).queue();
                }
                return;
            }
        }

        event.getMessage().reply(new EmbedBuilder()
                .setTitle("Invalid role")
                .setDescription("Role not found")
                .setTimestamp(Instant.now())
                .setColor(Color.red)
                .build()).queue();
    }
}
