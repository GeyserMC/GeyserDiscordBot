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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.geysermc.discordbot.http.Server;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

public class LeaderboardCommand extends SlashCommand {

    public LeaderboardCommand() {
        this.name = "leaderboard";
        this.help = "Sends a link to the leaderboard for the current server";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().replyEmbeds(getEmbed(event.getGuild())).queue();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(getEmbed(event.getGuild())).queue();
    }

    private MessageEmbed getEmbed(Guild guild) {
        if (ServerSettings.serverLevelsDisabled(guild)) {
            return new EmbedBuilder()
                    .setTitle("Levels disabled")
                    .setDescription("Levels are disabled for this server!")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        return new EmbedBuilder()
                .setTitle("Leaderboard for " + guild.getName(), Server.getUrl(guild.getIdLong()))
                .setDescription("Click the above for the leaderboard")
                .setThumbnail(guild.getIconUrl())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}
