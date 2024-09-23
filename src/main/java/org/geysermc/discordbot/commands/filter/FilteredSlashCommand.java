/*
 * Copyright (c) 2020-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.commands.filter;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

import java.util.regex.Pattern;

public abstract class FilteredSlashCommand extends SlashCommand {
    @Override
    protected final void execute(SlashCommandEvent event) {
        Pattern filterPattern = null;
        for (OptionMapping option : event.getOptions()) {
            if (option.getType() != OptionType.STRING) continue;
            if ((filterPattern = SwearHandler.checkString(option.getAsString())) != null) break;
        }

        if (filterPattern != null) {
            event.reply(event.getUser().getAsMention() +
                    " your command cannot be processed because it contains profanity! Please read our rules for more information.")
                    .setEphemeral(true).queue();

            // Log the event
            if (event.getGuild() != null) {
                String channel = event.getChannel() == null ? "Unknown" : event.getChannel().getAsMention();

                ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Profanity blocked command")
                        .setDescription("**Sender:** " + event.getUser().getAsMention() + "\n" +
                                "**Channel:** " + channel + "\n" +
                                "**Regex:** `" + filterPattern + "`\n" +
                                "**Command:** " + event.getCommandString())
                        .setColor(BotColors.FAILURE.getColor())
                        .build()).queue();
            }
            return;
        }

        executeFiltered(event);
    }

    protected abstract void executeFiltered(SlashCommandEvent event);
}
