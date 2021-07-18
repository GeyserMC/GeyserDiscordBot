package org.geysermc.discordbot.util;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

/**
 * This class helps us with error messages when handling commands of varying types
 */
public class MessageHelper {
    /**
     * Parse and reply an error response if the user needs to be told about their incorrect ways.
     * Handles both CommandEvent and SlashCommandEvent to ease support for both.
     *
     * @param event the event to handle
     * @param title the title of the embed
     * @param message the content of the embed
     * @throws IllegalArgumentException if event isn't CommandEvent or SlashCommandEvent
     * @return a MessageEmbed, or null if you want to pass it up
     */
    public static MessageEmbed errorResponse(Object event, String title, String message) {
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(title) // Set our title
            .setDescription(message) // Set the description
            .setColor(BotColors.FAILURE.getColor()) // Set the color
            .build(); // Finalize it
        if (event == null) {
            return embed;
        }
        if (event instanceof CommandEvent) { // If this is a normal !command
            ((CommandEvent) event)
                .getMessage()
                .reply(embed)
                .queue();
        } else if (event instanceof MessageReceivedEvent) { // If this is a /command
            ((MessageReceivedEvent) event)
                    .getMessage()
                    .reply(embed)
                    .queue();
        } else if (event instanceof SlashCommandEvent) { // If this is a /command
            ((SlashCommandEvent) event)
                .replyEmbeds(embed) // Have to do this nonsense...
                .setEphemeral(true) // Only show error to the user
                .queue();
        } else {
            throw new IllegalArgumentException("Event must be one of CommandEvent, SlashCommandEvent");
        }
        return null;
    }
}
