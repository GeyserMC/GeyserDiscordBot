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

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class BotHelpers {

    /**
     * Get a guild member from a given id string
     * Input examples:
     *  <@!1234>
     *  1234
     *
     * @param guild Guild to get the member for
     * @param userTag The tag to use to find the member
     * @return The found Member or null
     */
    @Nullable
    public static Member getMember(Guild guild, String userTag) {
        try {
            // Check for a mention (<@!1234>)
            if (userTag.startsWith("<@!") && userTag.endsWith(">")) {
                userTag = userTag.substring(3, userTag.length() - 1);
            } else {
                // Check for a user tag (example#1234)
                Matcher m = User.USER_TAG.matcher(userTag);
                if (m.matches()) {
                    return guild.getMemberByTag(m.group(1), m.group(2));
                }
            }

            // Try to get the member by ID
            return guild.getMemberById(userTag);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
