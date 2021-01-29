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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.LevelInfo;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Random;

public class LevelHandler extends ListenerAdapter {
    private static final Random RANDOM = new Random();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        // Ignore bots
        if (event.getMember().getUser().isBot()) {
            return;
        }

        // TODO: Add a 1 lot of xp per min cooldown

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
                    .setColor(Color.green)
                    .build()).queue();
        }

        // Update the level
        GeyserBot.storageManager.setLevel(event.getMember(), levelInfo);
    }
}
