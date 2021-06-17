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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.PropertiesManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

public class FileHandler extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (ServerSettings.getList(event.getGuild().getIdLong(), "convert-extensions").contains(attachment.getFileExtension())) {
                EmbedBuilder embed = new EmbedBuilder();

//                // Handled by Discord's new display feature
//                if (attachment.getSize() < 50000) {
//                    continue;
//                }

                try {
                    File attachmentFile = attachment.downloadToFile().get();

                    RequestBody body = RequestBody.create("{" +
                                "\"name\":" + JSONObject.quote(attachment.getFileName()) + "," +
                                "\"expires\":\"" + event.getMessage().getTimeCreated().plusDays(1).toString() + "\"," +
                                "\"files\": [" +
                                    "{" +
                                        "\"name\":" + JSONObject.quote(attachment.getFileName()) + "," +
                                        "\"content\": {" +
                                            "\"format\": \"text\"," +
                                            "\"value\": " + JSONObject.quote(new String(Files.readAllBytes(attachmentFile.toPath()))) +
                                        "}" +
                                    "}" +
                                "]" +
                            "}", MediaType.parse("application/json"));

                    Request request = new Request.Builder()
                        .url("https://api.paste.gg/v1/pastes")
                        .post(body)
                        .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org)") // GeyserMC - Replace with our bot user agent
                        .build();

                    JSONObject response = new JSONObject(RestClient.performRequest(request));

                    // Cleanup the file
                    attachmentFile.delete();

                    if (response.has("error")) {
                        throw new AssertionError(response.get("error"));
                    }

                    String pasteUrl = "https://paste.gg/" + response.getJSONObject("result").getString("id");

                    embed.setColor(PropertiesManager.getDefaultColor());
                    embed.setTitle(pasteUrl, pasteUrl);
                    embed.setDescription("Converted `" + attachment.getFileName() + "` to a paste.gg link!");
                } catch (InterruptedException | ExecutionException | IOException | AssertionError e) {
                    embed.setColor(Color.red);
                    embed.setTitle("paste.gg upload failed!");
                    embed.setDescription("An exception occurred during upload: " + e.getMessage());
                }

                event.getMessage().reply(embed.build()).queue();
            }
        }
    }
}
