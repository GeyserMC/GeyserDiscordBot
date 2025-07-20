/*
 * Copyright (c) 2020-2025 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
        event.getMessage().replyComponents(getComponent(event.getGuild()))
            .useComponentsV2()
            .queue();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyComponents(getComponent(event.getGuild()))
            .useComponentsV2()
            .queue();
    }

    private Container getComponent(Guild guild) {
        if (ServerSettings.serverLevelsDisabled(guild)) {
            return Container.of(
                    TextDisplay.of("## Levels disabled"),
                    Separator.createDivider(Separator.Spacing.SMALL),
                    TextDisplay.of("Levels are disabled for this server!")
                )
                .withAccentColor(BotColors.FAILURE.getColor());
        }

        return Container.of(
                TextDisplay.of("## Level leaderboard for " + guild.getName()),
                Separator.createDivider(Separator.Spacing.SMALL),
                TextDisplay.of("Click the button below for the level leaderboard!"),
                ActionRow.of(Button.link(Server.getUrl(guild.getIdLong()), "View the leaderboard").withEmoji(Emoji.fromUnicode("\uD83D\uDCC8")))
            )
            .withAccentColor(BotColors.SUCCESS.getColor());
    }
}
