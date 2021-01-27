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

package org.geysermc.discordbot.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.geysermc.discordbot.GeyserBot;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class BotHelpers {

    /**
     * Get a guild member from a given id string
     *
     * @param guild Guild to get the member for
     * @param userTag The tag to use to find the member
     * @return The found Member or null
     */
    @Nullable
    public static Member getMember(Guild guild, String userTag) {
        try {
            return guild.getMember(getUser(userTag));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Get a discord user from a given id string
     * Input examples:
     *  <@!1234>
     *  1234
     *  abc#1234
     *
     * @param userTag The tag to use to find the member
     * @return The found User or null
     */
    @Nullable
    public static User getUser(String userTag) {
        try {
            // Check for a mention (<@!1234>)
            if (userTag.startsWith("<@!") && userTag.endsWith(">")) {
                userTag = userTag.substring(3, userTag.length() - 1);
            } else {
                // Check for a user tag (example#1234)
                Matcher m = User.USER_TAG.matcher(userTag);
                if (m.matches()) {
                    return GeyserBot.getJDA().getUserByTag(m.group(1), m.group(2));
                }
            }

            // Try to get the member by ID
            return GeyserBot.getJDA().getUserById(userTag);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static final char[] FORMAT_CHARS = new char[]{'k', 'm', 'b', 't'};

    public static String coolFormat(int n) {
        return n < 1000 ? String.valueOf(n) : coolFormat(n, 0);
    }

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     *
     * https://stackoverflow.com/a/4753866/5299903
     *
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + FORMAT_CHARS[iteration])
                : coolFormat(d, iteration+1));

    }
}
