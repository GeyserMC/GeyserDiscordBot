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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.geysermc.discordbot.util.BotColors;

import java.awt.Color;
import java.util.List;

public class FixMutedCommand extends SlashCommand {

    public FixMutedCommand() {
        this.name = "fixmuted";
        this.help = "Check the muted role exists and is configured correctly";
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.KICK_MEMBERS };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        InteractionHook interactionHook = event.deferReply().complete();

        List<Role> roles = event.getGuild().getRolesByName("muted", true);
        if (roles.size() == 0) {
            // Create the muted role
            event.getGuild().createRole()
                    .setName("Muted")
                    .setColor(Color.decode("#818386"))
                    .setPermissions(Permission.VIEW_CHANNEL, Permission.NICKNAME_CHANGE, Permission.MESSAGE_HISTORY, Permission.VOICE_CONNECT)
                    .queue(role -> {
                        fixChannelPermissions(event.getGuild(), role, () -> {
                            respond(interactionHook, role, roles);
                        });
                    });
        } else {
            // Just update the channel permissions
            Role role = roles.get(0);

            role.getManager()
                    .setName("Muted")
                    .setColor(Color.decode("#818386"))
                    .setPermissions(Permission.VIEW_CHANNEL, Permission.NICKNAME_CHANGE, Permission.MESSAGE_HISTORY, Permission.VOICE_CONNECT)
                    .queue(unused -> {
                        fixChannelPermissions(event.getGuild(), role, () -> {
                            respond(interactionHook, role, roles);
                        });
                    });
        }
    }

    private void respond(InteractionHook interactionHook, Role role, List<Role> roles) {
        String message = "";

        if (roles.size() > 1) {
            message += "**Warning:** There are multiple roles named `muted`\n";
        }

        if (!roles.contains(role)) {
            message += "**Warning:** The role `muted` was not found, a new one was created\n";
        }

        if (!message.equals("")) {
            message += "\n";
        }

        message += "Done, updated channel permissions";

        interactionHook.editOriginalEmbeds(new EmbedBuilder()
                .setTitle("Fixing muted role")
                .setDescription(message)
                .addField("Role", role.getAsMention(), false)
                .setColor(BotColors.SUCCESS.getColor())
                .build()).queue();
    }

    private void fixChannelPermissions(Guild guild, Role role, Runnable callback) {
        guild.getChannels().forEach(channel -> {
            channel.getPermissionContainer().upsertPermissionOverride(role)
                    .setDenied(Permission.MESSAGE_SEND, Permission.MESSAGE_SEND_IN_THREADS, Permission.VOICE_SPEAK, Permission.REQUEST_TO_SPEAK, Permission.MESSAGE_ADD_REACTION)
                .queue();
        });

        callback.run();
    }
}
