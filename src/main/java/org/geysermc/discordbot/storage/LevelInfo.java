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

package org.geysermc.discordbot.storage;

import net.dv8tion.jda.api.entities.User;
import org.geysermc.discordbot.GeyserBot;

public class LevelInfo {
    private long userId;
    private int level;
    private int xp;
    private int messages;

    public LevelInfo(long userId, int level, int xp, int messages) {
        this.userId = userId;
        this.level = level;
        this.xp = xp;
        this.messages = messages;
    }

    public User getUser() {
        // Get the user from the cache
        User user = GeyserBot.getJDA().getUserById(userId);

        // Get the user from the api since it wasn't in the cache
        if (user == null) {
            user = GeyserBot.getJDA().retrieveUserById(userId).complete();
        }

        return user;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getMessages() {
        return messages;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setMessages(int messages) {
        this.messages = messages;
    }

    public int getNextLevel() {
        return level + 1;
    }

    public int getXpForNextLevel() {
        return getXpForLevel(getNextLevel());
    }

    private int getXpForLevel(int level) {
        return Math.round(5f / 6 * level * (2 * level * level + 27 * level + 91));
    }

    public int getXpToNextLevel() {
        return getXpForNextLevel() - getXp();
    }

    public float getLevelProgress() {
        return (float)(getXp() - getXpForLevel(getLevel())) / (getXpForNextLevel() - getXpForLevel(getLevel()));
    }
}
