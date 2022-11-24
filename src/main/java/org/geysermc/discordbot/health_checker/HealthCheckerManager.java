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

package org.geysermc.discordbot.health_checker;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HealthCheckerManager {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final Map<String, HealthStatus> HEALTH_STATUS = new HashMap<>();

    /**
     * Setup the schedule for health checks
     */
    public static void setup() {
        GeyserBot.getGeneralThreadPool().schedule(() -> {
            User self = GeyserBot.getJDA().getSelfUser();
            for (Guild guild : GeyserBot.getJDA().getGuilds()) {
                for (Map.Entry<String, String> check : ServerSettings.getMap(guild.getIdLong(), "health-checks").entrySet()) {
                    // If we can't get the channel skip the check
                    if (!BotHelpers.channelExists(guild, check.getKey())) continue;

                    boolean found = false;

                    // Check the last 100 messages for a health check message
                    for (Message message : guild.getTextChannelById(check.getKey()).getHistory().retrievePast(100).complete()) {
                        if (message.getAuthor() != self) {
                            continue;
                        }

                        // Check for embeds
                        List<MessageEmbed> embeds = message.getEmbeds();
                        if (embeds.size() == 0 || !embeds.get(0).getTitle().startsWith("Health check: ")) {
                            continue;
                        }

                        // Go over the first embed and find the status code field
                        for (MessageEmbed.Field field : embeds.get(0).getFields()) {
                            if (field.getName().equals("Status code")) {

                                HEALTH_STATUS.put(check.getKey(), new HealthStatus(message, Integer.parseInt(field.getValue())));

                                found = true;
                                break;
                            }
                        }

                        // We have the wanted embed so we can stop
                        if (found) {
                            break;
                        }
                    }
                }
            }
        }, 5, TimeUnit.SECONDS);

        GeyserBot.getGeneralThreadPool().scheduleAtFixedRate(HealthCheckerManager::doChecks, 60L, 60L, TimeUnit.SECONDS);
    }

    /**
     * Check for health status and notify the channel of the change
     */
    private static void doChecks() {
        for (Guild guild : GeyserBot.getJDA().getGuilds()) {
            for (Map.Entry<String, String> check : ServerSettings.getMap(guild.getIdLong(), "health-checks").entrySet()) {
                try {
                    // If we can't get the channel skip the check
                    if (!BotHelpers.channelExists(guild, check.getKey())) continue;

                    // Build the request check
                    Request request = new Request.Builder()
                            .url(check.getValue())
                            .get()
                            .build();

                    boolean success;
                    int responseCode;
                    long responseTime;
                    long responseBodyLength;
                    String responseBody = null;

                    // Make the request and store the info for the embed
                    try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                        success = response.code() == 204;
                        responseCode = response.code();
                        responseTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
                        responseBodyLength = response.body().contentLength();
                        if (responseBodyLength > 0) {
                            responseBody = response.body().string();
                        }
                    } catch (IOException e) {
                        success = false;
                        responseCode = 0;
                        responseTime = 0;
                        responseBodyLength = 0;
                        responseBody = e.toString();
                    }
                    responseBody = responseBody != null ? "```\n" + responseBody + "```" : "None";

                    // Get the existing message if it's the same status
                    Message message = null;
                    HealthStatus healthStatus = HEALTH_STATUS.get(check.getKey());
                    if (healthStatus != null && healthStatus.wasSuccess() == success) {
                        message = healthStatus.getMessage();
                    }

                    // Build the updated embed
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("Health check: " + check.getValue(), check.getValue())
                            .addField("Status code", String.valueOf(responseCode), false)
                            .addField("Response time", responseTime + "ms", false)
                            .addField("Response (" + responseBodyLength + ")", responseBody, false)
                            .setTimestamp(Instant.now())
                            .setColor(success ? BotColors.SUCCESS.getColor() : BotColors.FAILURE.getColor())
                            .build();

                    // Update or send the embed
                    if (message != null) {
                        message = message.editMessageEmbeds(embed).complete();
                    } else {
                        message = guild.getTextChannelById(check.getKey()).sendMessageEmbeds(embed).complete();
                    }

                    // Update our cache
                    healthStatus = new HealthStatus(message, responseCode);
                    HEALTH_STATUS.put(check.getKey(), healthStatus);
                } catch (Exception e) {
                    GeyserBot.LOGGER.error("Error checking health on: " + check.getValue(), e);
                }
            }
        }
    }
}
