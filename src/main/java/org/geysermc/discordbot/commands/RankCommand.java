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

package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RankCommand extends SlashCommand {

    public RankCommand() {
        this.name = "rank";
        this.aliases = new String[] { "role" };
        this.arguments = "<role>";
        this.help = "Give yourself a role";

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "role", "The role you want to get", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (ServerSettings.shouldProhibitUserCommands(event.getMessageChannel(), event.getMember()) && event.getGuild() != null) {
            event.deferReply(true).addContent(MessageHelper.getForbiddenMessage(event.getGuild().getIdLong())).submit();
            return;
        }
        String role = event.optString("role", "");

        event.replyEmbeds(handle(event.getGuild(), event.getMember(), role)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        if (ServerSettings.shouldProhibitUserCommands(event.getChannel(), event.getMember())) {
            event.reply(MessageHelper.getForbiddenMessage(event.getGuild().getIdLong()));
            return;
        }
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Check they specified an role
        if (args.get(0).isEmpty()) {
            MessageHelper.errorResponse(event, "Missing role", "Please specify an role to get.");
            return;
        }

        event.getMessage().replyEmbeds(handle(event.getGuild(), event.getMember(), args.get(0))).queue();
    }

    protected MessageEmbed handle(Guild guild, Member member, String wantedRole) {
        Map<String, String> roles = ServerSettings.getMap(guild.getIdLong(), "roles");
        for (Map.Entry<String, String> roleData : roles.entrySet()) {
            if (wantedRole.equalsIgnoreCase(roleData.getKey())) {
                Role role = guild.getRoleById(roleData.getValue());
                if (role == null) {
                    return new EmbedBuilder()
                            .setTitle("Invalid role")
                            .setDescription("Invalid role specified in configuration")
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.FAILURE.getColor())
                            .build();
                }

                if (member.getRoles().contains(role)) {
                    guild.removeRoleFromMember(member, role).queue();
                    return new EmbedBuilder()
                            .setTitle("Removed role")
                            .setDescription("Removed " + role.getAsMention() + " from " + member.getAsMention())
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.SUCCESS.getColor())
                            .build();
                } else {
                    guild.addRoleToMember(member, role).queue();
                    return new EmbedBuilder()
                            .setTitle("Granted role")
                            .setDescription("Given " + role.getAsMention() + " to " + member.getAsMention())
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.SUCCESS.getColor())
                            .build();
                }
            }
        }

        return new EmbedBuilder()
                .setTitle("Invalid role")
                .setDescription("Role not found")
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();
    }
}
