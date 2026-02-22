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

package org.geysermc.discordbot.util;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.ApplicationEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.EnumMap;

public enum BotEmojis {
    // Platforms // TODO add the real emoji IDs
    FABRIC("1475218517400879346"),
    GEYSER("1475218518432809010"),
    NEOFORGE("1475218519720198317"),
    PAPER("1475218520945070202"),
    VELOCITY("1475218521884459182"),
    VIAPROXY("1475218523042353417"),
    WATERFALL("1475218524116090970");

    private static final EnumMap<BotEmojis, ApplicationEmoji> EMOJI_MAP = new EnumMap<>(BotEmojis.class);

    public static void init(JDA jda) {
        for (BotEmojis emoji : values()) {
            jda.retrieveApplicationEmojiById(emoji.emojiId).queue(e -> EMOJI_MAP.put(emoji, e));
        }
    }

    private final String emojiId;

    BotEmojis(String emojiId) {
        this.emojiId = emojiId;
    }

    public Emoji get() {
        return EMOJI_MAP.get(this);
    }
}
