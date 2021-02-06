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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.util.BotHelpers;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WhoisCommand extends Command {

    public WhoisCommand() {
        this.name = "whois";
        this.hidden = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Fetch the user
        Member member;
        if (args.size() == 0 || args.get(0).isEmpty()) {
            member = event.getMember();
        } else {
            member = BotHelpers.getMember(event.getGuild(), args.remove(0));
        }

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

        // Get the roles
        String roles = "None";
        if (member.getRoles().size() > 0) {
            roles = member.getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(" "));
        }

        event.getMessage().reply(new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setDescription(user.getAsMention())
                .addField("Joined", member.getTimeJoined().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Registered", member.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME), true)
                .addField("Roles [" + member.getRoles().size() + "]", roles, false)
                // TODO: Filter perms to only 'key' ones
                .addField("Permissions", member.getPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")), false)
                .setThumbnail(user.getAvatarUrl())
                .setFooter("ID: " + user.getId())
                .setTimestamp(Instant.now())
                .setColor(member.getColor())
                .build()).queue();
    }
}
