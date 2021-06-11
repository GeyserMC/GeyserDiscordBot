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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.geysermc.discordbot.util.PropertiesManager;

import java.time.Instant;

/**
 * TODO: Add role filter
 */
public class MemberCountCommand extends SlashCommand {

    public MemberCountCommand() {
        this.name = "membercount";
        this.aliases = new String[] {"members"};
        this.help = "Show the current member count";
    }


    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(handle(event.getGuild())).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().reply(handle(event.getGuild())).queue();
    }

    protected MessageEmbed handle(Guild guild) {
        return new EmbedBuilder()
                .addField("Members", String.valueOf(guild.getMemberCount()), false)
                .setTimestamp(Instant.now())
                .setColor(PropertiesManager.getDefaultColor())
                .build();
    }
}
