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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadLinksHandler extends ListenerAdapter {
    private static Pattern HTTP_PATTERN = Pattern.compile("https?:\\/\\/[^\\s<]+[^<.,:;\"')\\]\\s]", Pattern.CASE_INSENSITIVE);

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        // Ignore users with the manage message perms
        if (event.getMember() == null ||event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            return;
        }

        // Find URLs
        Matcher m = HTTP_PATTERN.matcher(event.getMessage().getContentRaw());

        while (m.find()) {
            String link = m.group();
            String domain = link.split("//")[1].split("/")[0];

            for (String checkDomain : ServerSettings.getList(event.getGuild().getIdLong(), "check-domains")) {
                // Is the domain not exact but still close
                if (!domain.equals(checkDomain) && compareDomain(domain, checkDomain)) {
                    ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                            .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                            .setDescription("**Link removed, sent by** " + event.getAuthor().getAsMention() + " **deleted in** " + event.getChannel().getAsMention() + "\n" + event.getMessage().getContentRaw())
                            .addField("Link", link, false)
                            .addField("Matched domain", checkDomain, false)
                            .setFooter("Author: " + event.getAuthor().getId() + " | Message ID: " + event.getMessageId())
                            .setTimestamp(Instant.now())
                            .setColor(BotColors.FAILURE.getColor())
                            .build()).queue();

                    LogHandler.PURGED_MESSAGES.add(event.getMessageId());

                    event.getMessage().delete().queue();

                    return;
                }
            }
        }

        // http://streamcommunnlty.ru/tradeoffer/new/?partner=1201662247&token=MtT3lJcb
    }

    /**
     * Compare 2 domains using {@link DicesCoefficient#diceCoefficientOptimized(String, String)} but be more strict with .ru domains
     *
     * @param domain Domain to compare
     * @param checkDomain Target domain
     * @return If they are similar
     */
    private boolean compareDomain(String domain, String checkDomain) {
        if (domain.endsWith(".ru")) { // Most phishing sites are ru so be more strict with them
            return (Math.round(DicesCoefficient.diceCoefficientOptimized(domain, checkDomain) * 10.0) / 10.0) >= 0.5f;
        } else {
            return DicesCoefficient.diceCoefficientOptimized(domain, checkDomain) >= 0.6f;
        }
    }
}
