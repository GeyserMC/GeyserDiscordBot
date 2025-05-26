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

package org.geysermc.discordbot.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.MessageContextMenu;
import com.jagrosh.jdautilities.command.MessageContextMenuEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.geysermc.discordbot.util.BotColors;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

public class CommandErrorHandler extends BotCommandListener implements CommandListener {
    @Override
    public void onSlashCommandException(SlashCommandEvent event, SlashCommand command, Throwable throwable) {
        String errorMessage = getErrorMessage(throwable);

        MessageEmbed errorEmbed = new EmbedBuilder()
                .setTitle("Error handling command")
                .setDescription("An error occurred while handling the command")
                .addField("Command usage", "/" + command.getName() + (command.getArguments() != null ? " " + command.getArguments() : ""), false)
                .addField("Command help", command.getHelp(), false)
                .addField("Error", errorMessage, false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();

        event.replyEmbeds(errorEmbed).setEphemeral(true).queue(message -> {}, throwable1 -> {
            event.getInteraction().getHook().editOriginalEmbeds(errorEmbed).queue();
        });

        super.onSlashCommandException(event, command, throwable);
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        String errorMessage = getErrorMessage(throwable);

        event.getMessage().replyEmbeds(new EmbedBuilder()
                .setTitle("Error handling command")
                .setDescription("An error occurred while handling the command")
                .addField("Command usage", event.getPrefix() + command.getName() + (command.getArguments() != null ? " " + command.getArguments() : ""), false)
                .addField("Command help", command.getHelp(), false)
                .addField("Error", errorMessage, false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build()).queue();

        super.onCommandException(event, command, throwable);
    }

    @Override
    public void onMessageContextMenuException(MessageContextMenuEvent event, MessageContextMenu menu, Throwable throwable) {
        String errorMessage = getErrorMessage(throwable);

        MessageEmbed errorEmbed = new EmbedBuilder()
                .setTitle("Error handling context menu option")
                .setDescription("An error occurred while handling the message context menu option")
                .addField("Menu option", menu.getName(), false)
                .addField("Error", errorMessage, false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();

        event.replyEmbeds(errorEmbed).setEphemeral(true).queue(message -> {}, throwable1 -> {
            event.getInteraction().getHook().editOriginalEmbeds(errorEmbed).queue();
        });

        super.onMessageContextMenuException(event, menu, throwable);
    }

    @Override
    public void onUserContextMenuException(UserContextMenuEvent event, UserContextMenu menu, Throwable throwable) {
        String errorMessage = getErrorMessage(throwable);

        MessageEmbed errorEmbed = new EmbedBuilder()
                .setTitle("Error handling context menu option")
                .setDescription("An error occurred while handling the user context menu option")
                .addField("Menu option", menu.getName(), false)
                .addField("Error", errorMessage, false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.FAILURE.getColor())
                .build();

        event.replyEmbeds(errorEmbed).setEphemeral(true).queue(message -> {}, throwable1 -> {
            event.getInteraction().getHook().editOriginalEmbeds(errorEmbed).queue();
        });

        super.onUserContextMenuException(event, menu, throwable);
    }

    private String getErrorMessage(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String[] errorStack = sw.toString().split("\n");
        int limit = Math.min(errorStack.length, 5);

        StringBuilder errorMessage = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            errorMessage.append(errorStack[i]).append("\n");
        }
        return errorMessage.toString();
    }
}
