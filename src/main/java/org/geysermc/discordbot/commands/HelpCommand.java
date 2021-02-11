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
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.util.PropertiesManager;

import java.awt.Color;

/**
 * Handle the help command
 */
public class HelpCommand extends Command {

    public HelpCommand() {
        this.name = "help";
        this.help = "I think you already know what this does";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setColor(PropertiesManager.getDefaultColor())
            .setTitle("Geyser Bot Help");

        for (Command command : GeyserBot.COMMANDS) {
            if (!command.isHidden()) {
                helpEmbed.addField("`" + PropertiesManager.getPrefix() + command.getName() + (command.getArguments() != null ? " " + command.getArguments() : "") + "`", command.getHelp(), true);
            }
        }

        helpEmbed.addField("`!tag <name>`", "Display a tag for the given name", true);


        event.getMessage().reply(helpEmbed.build()).queue();
    }
}
