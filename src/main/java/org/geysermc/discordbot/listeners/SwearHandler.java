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
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SwearHandler extends ListenerAdapter {

    public static List<Long> filteredMessages = new ArrayList<>();
    public static List<Pattern> filterPatterns = new ArrayList<>();

    public static void loadFilters() {
        int fileCount = 0;
        for (File file : new File(SwearHandler.class.getClassLoader().getResource("filters").getPath()).listFiles()) {
            if (file.isFile() && file.getName().endsWith(".wlist")) {
                fileCount++;
                try {
                    // Load the lines
                    String[] lines = new String(Files.readAllBytes(file.toPath())).split("\n");
                    for (String line : lines) {
                        filterPatterns.add(Pattern.compile("(^| )" + line.trim() + "( |$)", Pattern.CASE_INSENSITIVE));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO: Handle error
                }
            }
        }

        GeyserBot.LOGGER.info("Loaded " + filterPatterns.size() + " filter patterns from " + fileCount + " files");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (Pattern filterPattern : filterPatterns) {
            if (filterPattern.matcher(event.getMessage().getContentRaw()).matches()) {
                filteredMessages.add(event.getMessageIdLong());

                // Delete message
                event.getMessage().delete().queue(unused -> {
                    // Alert the user message
                    event.getChannel().sendMessage(new MessageBuilder()
                            .append(event.getAuthor().getAsMention())
                            .append(" your message has been removed because it contains profanity! Please read our rules for more information.")
                            .build()).queue();

                    // Send a log to the admin channel
                    ServerSettings.getLogChannel(event.getGuild()).sendMessage(new EmbedBuilder()
                            .setTitle("Profanity removed")
                            .setDescription("**Sender:** " + event.getAuthor().getAsMention() + "\n" +
                                    "**Channel:** <#" + event.getChannel().getId() + ">\n" +
                                    "**Regex:** `" + filterPattern + "`\n" +
                                    "**Message:** " + event.getMessage().getContentRaw())
                            .setColor(Color.red)
                            .build()).queue();

                    // Remove the message from filteredMessages after 5s
                    // this should be enough time for the rest of the events to fire
                    GeyserBot.getGeneralThreadPool().schedule(() -> {
                        filteredMessages.remove(event.getMessageIdLong());
                    }, 5, TimeUnit.SECONDS);
                });

                return;
            }
        }
    }
}
