/*
 * Copyright (c) 2021-2025 GeyserMC. http://geysermc.org
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
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryLevel;
import io.sentry.protocol.Message;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class BotCommandListener implements CommandListener {
    // Methods to detect command usage. Currently unused

    public void onCommand(CommandEvent event, Command command) {
    }

    public void onSlashCommand(SlashCommandEvent event, SlashCommand command) {
    }

    // Methods to capture errors and send them to sentry

    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        SentryEvent sentryEvent = buildEvent(command, event.getAuthor(), event.getChannel(), event.getGuild());
        sentryEvent.setThrowable(throwable);
        sentryEvent.setExtra("message_content", event.getMessage().getContentRaw());

        Sentry.captureEvent(sentryEvent);

        // Default rethrow as a runtime exception.
        throw throwable instanceof RuntimeException? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    public void onSlashCommandException(SlashCommandEvent event, SlashCommand command, Throwable throwable) {
        SentryEvent sentryEvent = buildEvent(command, event.getUser(), event.getChannel(), event.getGuild());
        sentryEvent.setThrowable(throwable);
        sentryEvent.setExtra("options", event.getOptions().toArray());

        Sentry.captureEvent(sentryEvent);

        // Default rethrow as a runtime exception.
        throw throwable instanceof RuntimeException? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    /**
     * Builds a basic event for us to fill in when an exception occurs
     *
     * @param command the command class
     * @return a half-baked sentry event, please fill out completely
     */
    private SentryEvent buildEvent(Command command, User author, MessageChannel channel, Guild server) {
        SentryEvent sentryEvent = new SentryEvent();
        Message message = new Message();
        message.setMessage("Exception caught running command " + command.getName());
        sentryEvent.setMessage(message);
        sentryEvent.setLevel(SentryLevel.ERROR);
        sentryEvent.setLogger(command.getClass().getName());

        io.sentry.protocol.User user = new io.sentry.protocol.User();
        user.setId(author.getId());
        user.setUsername(author.getName());
        sentryEvent.setUser(user);

        sentryEvent.setExtra("guild_id", server == null ? "null" : server.getId());
        sentryEvent.setExtra("channel_id", channel.getId());

        return sentryEvent;
    }
}
