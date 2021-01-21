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

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> roles = new ArrayList<>();
        for (Role role : member.getRoles()) {
            roles.add(role.getAsMention());
        }
        if (roles.isEmpty()) {
            roles.add("No roles");
        }

        // Maybe worth getting rid of this depends on how many times its used
        User user = member.getUser();
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Showing info for " + user.getName())
                .addField("Nick", user.getAsMention(), false)
                .addField("Joined", member.getTimeJoined().getMonth().getValue() + "/" + member.getTimeJoined().getDayOfMonth() + "/" + member.getTimeJoined().getYear() + " at " + member.getTimeJoined().getHour() + ":" + member.getTimeJoined().getMinute(), false)
                .addField("Registered", user.getTimeCreated().getMonth().getValue() + "/" + user.getTimeCreated().getDayOfMonth() + "/" + user.getTimeCreated().getYear() + " at " + user.getTimeCreated().getHour() + ":" + user.getTimeCreated().getMinute(), false)
                .addField("Roles [" + member.getRoles().size() + "]", String.join(" ", roles), false)
                .addField("ID", user.getId(), false)
                .addField("Key Permissions", member.getPermissions().toString().replace("_", " ").toLowerCase(), false)
                .setThumbnail(user.getAvatarUrl())
                .setColor(Color.green)
                .build();
        event.getChannel().sendMessage(embed).queue();

    }
}
