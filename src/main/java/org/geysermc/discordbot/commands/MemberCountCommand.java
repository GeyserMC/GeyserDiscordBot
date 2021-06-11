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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.PropertiesManager;

import java.time.Instant;
import java.util.Collections;

public class MemberCountCommand extends SlashCommand {

    public MemberCountCommand() {
        this.name = "membercount";
        this.aliases = new String[] {"members"};
        this.arguments = "[role]";
        this.help = "Show the current member count";

        this.options = Collections.singletonList(
                new OptionData(OptionType.ROLE, "role", "The role you want to filter for")
        );
    }


    @Override
    protected void execute(SlashCommandEvent event) {
        Role role = null;
        if (event.getOption("role") != null) {
            role = event.getOption("role").getAsRole();
        }

        event.replyEmbeds(handle(event.getGuild(), role)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().reply(handle(event.getGuild(), BotHelpers.getRole(event.getGuild(), event.getArgs()))).queue();
    }

    protected MessageEmbed handle(Guild guild, Role role) {
        return new EmbedBuilder()
                .addField("Members" + (role != null ? " in " + role.getName() : ""), String.valueOf(role != null ? guild.getMembersWithRoles(role).size() : guild.getMemberCount()), false)
                .setTimestamp(Instant.now())
                .setColor(PropertiesManager.getDefaultColor())
                .build();
    }
}
