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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.RequestBody;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FileHandler extends ListenerAdapter {
    private final Cache<Long, Long> fileCache;

    public FileHandler() {
        this.fileCache = CacheBuilder.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            List<String> extensions;

            // Get the guild extensions and if not in a guild just use some defaults
            if (event.isFromGuild()) {
                extensions = ServerSettings.getList(event.getGuild().getIdLong(), "convert-extensions");
            } else {
                extensions = new ArrayList<>();
                extensions.add("txt");
                extensions.add("log");
                extensions.add("yml");
                extensions.add("0");
            }

            if (extensions.contains(attachment.getFileExtension())) {
                EmbedBuilder embed = new EmbedBuilder();

//                // Handled by Discord's new display feature
//                if (attachment.getSize() < 50000) {
//                    continue;
//                }

                try {
                    File attachmentFile = attachment.downloadToFile().get();

                    RequestBody body = RequestBody.create("{" +
                                "\"name\":" + JSONObject.quote(attachment.getFileName()) + "," +
                                "\"expires\":\"" + event.getMessage().getTimeCreated().plusDays(1) + "\"," +
                                "\"files\": [" +
                                    "{" +
                                        "\"name\":" + JSONObject.quote(attachment.getFileName()) + "," +
                                        "\"content\": {" +
                                            "\"format\": \"text\"," +
                                            "\"value\": " + JSONObject.quote(new String(Files.readAllBytes(attachmentFile.toPath()))) +
                                        "}" +
                                    "}" +
                                "]" +
                            "}", RestClient.JSON);

                    JSONObject response = RestClient.simplePost("https://api.paste.gg/v1/pastes", body);

                    // Cleanup the file
                    attachmentFile.delete();

                    if (response.has("error")) {
                        throw new AssertionError(response.get("error"));
                    }

                    String pasteUrl = "https://paste.gg/" + response.getJSONObject("result").getString("id");

                    embed.setColor(BotColors.SUCCESS.getColor());
                    embed.setTitle(pasteUrl, pasteUrl);
                    embed.setDescription("Converted `" + attachment.getFileName() + "` to a paste.gg link!");
                } catch (InterruptedException | ExecutionException | IOException | AssertionError e) {
                    embed.setColor(BotColors.FAILURE.getColor());
                    embed.setTitle("paste.gg upload failed!");
                    embed.setDescription("An exception occurred during upload: " + e.getMessage());
                }

                event.getMessage().replyEmbeds(embed.build()).queue(message -> {
                    fileCache.put(event.getMessageIdLong(), message.getIdLong());
                });
            }
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        Long responseId = fileCache.getIfPresent(event.getMessageIdLong());
        if (responseId != null) {
            event.getChannel().deleteMessageById(responseId).queue();
        }
    }
}
