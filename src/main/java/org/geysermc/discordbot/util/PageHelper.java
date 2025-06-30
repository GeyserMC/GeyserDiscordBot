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

package org.geysermc.discordbot.util;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility class for paginating through a list of embeds using buttons in Discord JDA.
 */
public class PageHelper {

    private final Map<String, Integer> pages;
    private final List<MessageEmbed> embeds;
    private final long time;
    private final User user;
    private final JDA jda;
    private final String userId;

    /**
     * Constructor for Slash Commands.
     *
     * @param embeds      The list of embeds to paginate through.
     * @param event       The SlashCommandEvent.
     * @param time        The time in milliseconds before the paginator expires (default is 5 minutes).
     */
    public PageHelper(
            List<MessageEmbed> embeds,
            SlashCommandEvent event,
            long time
    ) {
        this.pages = new HashMap<>();
        this.embeds = embeds;
        this.time = time > 0 ? time : 1000 * 60 * 5;
        this.user = event.getUser();
        this.jda = event.getJDA();
        this.userId = user.getId();
        this.pages.put(userId, 0);

        event.replyEmbeds(this.embeds.get(this.pages.get(userId)))
                .addComponents(getRow())
                .setEphemeral(false)
                .queue(response -> {
                    response.retrieveOriginal().queue(msg -> {
                        ButtonInteractionListener listener = new ButtonInteractionListener(msg.getId(), userId);
                        jda.addEventListener(listener);

                        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                        scheduler.schedule(() -> {
                            jda.removeEventListener(listener);
                            msg.editMessageComponents().queue();
                            scheduler.shutdown();
                        }, this.time, TimeUnit.MILLISECONDS);
                    });
                });
    }

    /**
     * Constructor for Message-based Commands.
     *
     * @param embeds The list of embeds to paginate through.
     * @param event  The CommandEvent.
     * @param time   The time in milliseconds before the paginator expires (default is 5 minutes).
     */
    public PageHelper(
            List<MessageEmbed> embeds,
            CommandEvent event,
            long time
    ) {
        this.pages = new HashMap<>();
        this.embeds = embeds;
        this.time = time > 0 ? time : 1000 * 60 * 5;
        this.user = event.getAuthor();
        this.jda = event.getJDA();
        this.userId = user.getId();
        this.pages.put(userId, 0);

        event.getMessage().replyEmbeds(this.embeds.get(this.pages.get(userId)))
                .setComponents(getRow())
                .queue(msg -> {
                    ButtonInteractionListener listener = new ButtonInteractionListener(msg.getId(), userId);
                    jda.addEventListener(listener);

                    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
                    scheduler.schedule(() -> {
                        jda.removeEventListener(listener);
                        msg.editMessageComponents().queue();
                        scheduler.shutdown();
                    }, this.time, TimeUnit.MILLISECONDS);
                });
    }

    /**
     * Creates an action row with previous and next buttons.
     *
     * @return A list containing the ActionRow with navigation buttons.
     */
    private ActionRow getRow() {
        int currentPage = pages.get(userId);
        boolean isFirstPage = currentPage == 0;
        boolean isLastPage = currentPage == embeds.size() - 1;

        Button prevButton = Button.secondary("prev_page", Emoji.fromUnicode("\u23ee")).withDisabled(isFirstPage);
        Button nextButton = Button.secondary("next_page", Emoji.fromUnicode("\u23ed")).withDisabled(isLastPage);

        return ActionRow.of(prevButton, nextButton);
    }

    /**
     * Handles button interactions for pagination.
     *
     * @param event The button interaction event.
     */
    private void handleInteraction(ButtonInteractionEvent event) {
        String customId = event.getComponentId();

        if (!customId.equals("prev_page") && !customId.equals("next_page")) {
            return;
        }

        event.deferEdit().queue();

        int currentPage = pages.get(userId);

        if (customId.equals("prev_page") && currentPage > 0) {
            pages.put(userId, --currentPage);
        } else if (customId.equals("next_page") && currentPage < embeds.size() - 1) {
            pages.put(userId, ++currentPage);
        }

        event.getHook().editOriginalEmbeds(embeds.get(currentPage))
                .setComponents(getRow())
                .queue();
    }

    /**
     * An inner class that listens for button interactions related to pagination.
     */
    private class ButtonInteractionListener extends ListenerAdapter {
        private final String messageId;
        private final String userId;

        public ButtonInteractionListener(String messageId, String userId) {
            this.messageId = messageId;
            this.userId = userId;
        }

        @Override
        public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {

            if (!event.getMessageId().equals(messageId)) {
                return;
            }
            if (!event.getUser().getId().equals(userId)) {
                event.reply("These buttons are not for you!").setEphemeral(true).queue();
                return;
            }

            handleInteraction(event);
        }
    }
}