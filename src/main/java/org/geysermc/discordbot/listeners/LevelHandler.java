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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.LevelInfo;
import org.geysermc.discordbot.util.PropertiesManager;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LevelHandler extends ListenerAdapter {
    private static final Random RANDOM = new Random();

    private final Cache<Long, Long> messageCache;

    public LevelHandler() {
        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        // Ignore bots
        if (event.getAuthor().isBot()) {
            return;
        }

        // Check if the message hasn't expired
        Long time = messageCache.getIfPresent(event.getAuthor().getIdLong()) ;
        if (time != null && time >= Instant.now().toEpochMilli()) {
            return;
        }

        // Set the new value
        messageCache.put(event.getAuthor().getIdLong(), Instant.now().toEpochMilli() + (1000 * 60));

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
                    .setTimestamp(Instant.now())
                    .setColor(PropertiesManager.getDefaultColor())
                    .build()).queue();
        }

        // Update the level
        GeyserBot.storageManager.setLevel(event.getMember(), levelInfo);
    }
}
