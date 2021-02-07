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

package org.geysermc.discordbot.listeners;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.LevelInfo;
import org.geysermc.discordbot.util.PropertiesManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LevelHandler extends ListenerAdapter {
    private static final Random RANDOM = new Random();

    private static final Long2LongMap LAST_MESSAGE_LOG = new Long2LongOpenHashMap();

    public LevelHandler() {
        GeyserBot.getGeneralThreadPool().scheduleAtFixedRate(() -> {
            for (long user : LAST_MESSAGE_LOG.keySet()) {
                long time = LAST_MESSAGE_LOG.get(user);
                // Remove any old entry's to keep memory usage lower
                if (time <= Instant.now().toEpochMilli()) {
                    LAST_MESSAGE_LOG.remove(user, time);
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        // Ignore bots
        if (event.getMember().getUser().isBot()) {
            return;
        }

        if (LAST_MESSAGE_LOG.containsKey(event.getMember().getIdLong())) {
            long time = LAST_MESSAGE_LOG.get(event.getMember().getIdLong());

            // Check if the message hasn't expired
            if (time >= Instant.now().toEpochMilli()) {
                return;
            }
        }

        // Set the new value
        LAST_MESSAGE_LOG.put(event.getMember().getIdLong(), Instant.now().toEpochMilli() + (1000 * 60));

        int xp = 15 + RANDOM.nextInt(11);

        // Get the level
        LevelInfo levelInfo = GeyserBot.storageManager.getLevel(event.getMember());

        // Increase the xp
        levelInfo.setXp(levelInfo.getXp() + xp);

        // Check if we should have gone up a level
        if (levelInfo.getXpToNextLevel() <= 0) {
            levelInfo.setLevel(levelInfo.getNextLevel());

            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("Level Up!")
                    .setDescription("You leveled up to level " + levelInfo.getLevel() + "!")
                    .setColor(PropertiesManager.getDefaultColor())
                    .build()).queue();
        }

        // Update the level
        GeyserBot.storageManager.setLevel(event.getMember(), levelInfo);
    }
}
