/*
 * Copyright (c) 2020-2022 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.RequestBody;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BadLinksHandler extends ListenerAdapter {
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("(?:[A-z0-9](?:[A-z0-9-]{0,61}[A-z0-9])?\\.)+[A-z0-9][A-z0-9-]{0,61}[A-z0-9]");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Make sure we are in a guild
        if (!event.isFromGuild()) return;

        // Ignore users with the manage message perms
        if (event.getMember() == null || event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
//            return;
        }

        // Find URLs
        Matcher m = DOMAIN_PATTERN.matcher(event.getMessage().getContentRaw());

        List<String> checkDomains = ServerSettings.getList(event.getGuild().getIdLong(), "check-domains");
        List<String> bannedDomains = ServerSettings.getList(event.getGuild().getIdLong(), "banned-domains");
        List<String> bannedIPs = ServerSettings.getList(event.getGuild().getIdLong(), "banned-ips");

        boolean foundMatch = false;

        while (m.find()) {
            String domain = m.group();
            String reason = "";

            for (String bannedDomain : bannedDomains) {
                if (domain.equals(bannedDomain)) {
                    foundMatch = true;
                    reason = "Banned domain";

                    break;
                }
            }

            if (!foundMatch) {
                boolean compareDomainNeeded = true;
                for (String checkDomain : checkDomains) {
                    if (domain.endsWith("." + checkDomain) || domain.equals(checkDomain)) {
                        // If the domain is a good domain or a subdomain of a good domain, don't compare
                        compareDomainNeeded = false;
                        break;
                    }
                }
                if (compareDomainNeeded) {
                    for (String checkDomain : checkDomains) {
                        // Is the domain not exact but still close
                        if (compareDomain(domain, checkDomain)) {
                            foundMatch = true;
                            reason = "Similar to safe domain (" + checkDomain + ")";
                            break;
                        }
                    }
                }
            }

            if (!foundMatch && !bannedIPs.isEmpty()) {
                try {
                    String address = InetAddress.getAllByName(domain)[0].getHostAddress();

                    for (String checkIP : bannedIPs) {
                        // Check if the ip is banned
                        if (address.equals(checkIP)) {
                            foundMatch = true;
                            reason = "Domain resolves to banned IP (" + checkIP + ")";

                            break;
                        }
                    }
                } catch (UnknownHostException ignored) { }
            }

            if (!foundMatch) {
                // Make request to https://anti-fish.bitflow.dev/check
                RequestBody body = RequestBody.create("{\"message\":" + JSONObject.quote(event.getMessage().getContentRaw()) + "}", RestClient.JSON);
                JSONObject response = RestClient.simplePost("https://anti-fish.bitflow.dev/check", body);
                if (response.getBoolean("match")) {
                    JSONArray matches = response.getJSONArray("matches");
                    for (int i = 0; i < matches.length(); i++) {
                        JSONObject match = matches.getJSONObject(i);
                        if (match.getFloat("trust_rating") >= 0.5f) {
                            foundMatch = true;
                            domain = match.getString("domain");
                            reason = "`anti-fish.bitflow.dev` flagged as `" + match.getString("type") + "` from `" + match.getString("source") + "` with a trust rating of " + match.getFloat("trust_rating");
                            break;
                        }
                    }
                }
            }

            if (foundMatch) {
                ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                        .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl())
                        .setDescription("**Link removed, sent by** " + event.getAuthor().getAsMention() + " **deleted in** " + event.getChannel().getAsMention() + "\n" + event.getMessage().getContentRaw())
                        .addField("Block reason", reason, false)
                        .addField("Matched domain", "`" + domain + "`", false)
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
