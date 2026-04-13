/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.ModerationHelper;
import org.jetbrains.annotations.NotNull;

public class HoneyPotHandler extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (Guild guild : event.getJDA().getGuilds()) {
            String honeyPotChannelId = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "honey-pot-channel");
            if (honeyPotChannelId == null) continue;

            TextChannel channel = guild.getTextChannelById(honeyPotChannelId);
            if (channel == null) continue;

            channel.getHistory().retrievePast(100).queue(messages -> {
                // Add a honey pot message if the bot has no messages in the channel
                if (messages.stream().filter(m -> m.getAuthor().getId().equals(guild.getSelfMember().getId())).toList().isEmpty()) {
                    channel.sendMessage("""
                            # DO NOT POST ANY MESSAGES HERE
                            
                            This is a honey pot channel designed to catch scam accounts. Sending a message here will lead to a quarantine of your account.
                            """).queue();
                }
            });
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        String honeyPotChannelId = GeyserBot.storageManager.getServerPreference(event.getGuild().getIdLong(), "honey-pot-channel");
        if (honeyPotChannelId == null) return;

        if (event.getChannel().getId().equals(honeyPotChannelId)) {
            event.getMessage().delete().queue();
            ModerationHelper.quarantineMember(event.getMember(), event.getGuild(), "Messaged in the honey pot channel.", false, null);
        }
    }
}
