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

package org.geysermc.discordbot.util;

import io.sentry.Scope;
import io.sentry.Sentry;
import io.sentry.protocol.User;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.InterfacedEventManager;
import org.jetbrains.annotations.NotNull;

public class SentryEventManager extends InterfacedEventManager {
    @Override
    public void handle(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent) {
            Sentry.configureScope(scope -> buildMessageScope(scope, messageReceivedEvent.getAuthor(), messageReceivedEvent.getGuild(), messageReceivedEvent.getChannel(), messageReceivedEvent.getMessageId()));
        }

        super.handle(event);
    }

    private void buildMessageScope(Scope scope, net.dv8tion.jda.api.entities.User author, Guild server, MessageChannel channel, String messageId) {
        User user = new User();
        user.setId(author.getId());
        user.setUsername(author.getAsTag());
        scope.setUser(user);

        scope.setExtra("guild_id", server == null ? "null" : server.getId());
        scope.setExtra("channel_id", channel.getId());
        scope.setExtra("message_id", messageId);
        scope.setExtra("message_link", String.format("https://discord.com/channels/%s/%s/%s", server == null ? "@me" : server.getId(), channel.getId(), messageId));
    }
}
