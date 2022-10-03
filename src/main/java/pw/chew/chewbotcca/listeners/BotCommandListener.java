/*
 * Copyright (C) 2021 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.listeners;

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
        user.setUsername(author.getAsTag());
        sentryEvent.setUser(user);

        sentryEvent.setExtra("guild_id", server == null ? "null" : server.getId());
        sentryEvent.setExtra("channel_id", channel.getId());

        return sentryEvent;
    }
}
