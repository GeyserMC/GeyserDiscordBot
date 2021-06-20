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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadLinksHandler extends ListenerAdapter {
    private static Pattern HTTP_PATTERN = Pattern.compile("https?:\\/\\/[^\\s<]+[^<.,:;\"')\\]\\s]", Pattern.CASE_INSENSITIVE);

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        // Ignore users with the manage message perms
        if (event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
//            return;
        }

        // Find URLs
        Matcher m = HTTP_PATTERN.matcher(event.getMessage().getContentRaw());

        while (m.find()) {
            String link = m.group();
            String domain = link.split("//")[1].split("/")[0];

            for (String checkDomain : ServerSettings.getList(event.getGuild().getIdLong(), "check-domains")) {
                // Is the domain not exact but still close
                if (!domain.equals(checkDomain) && DicesCoefficient.diceCoefficientOptimized(domain, checkDomain) > 0.6f) {
                    // Likely a phish so remove it
                    event.getMessage().delete().queue();
                }
            }
        }

        // http://streamcommunnlty.ru/tradeoffer/new/?partner=1201662247&token=MtT3lJcb
    }
}
