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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwearHandler extends ListenerAdapter {

    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^\\p{N}\\p{L} ]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern NON_ASCII_PATTERN = Pattern.compile("([^\\p{ASCII}])");
    private static final Map<String,String> REPLACE_TOKENS = new HashMap<>();

    public static final List<Long> filteredMessages = new ArrayList<>();
    public static final List<Pattern> filterPatterns = new ArrayList<>();
    public static String[] nicknames;

    static {
        // Add some standard replacement tokens
        // TODO: Find a better way of doing this if we get duplicate chars
        REPLACE_TOKENS.put("\u0430", "a");
        REPLACE_TOKENS.put("\u043A", "k");
        REPLACE_TOKENS.put("\u0441", "c");
        REPLACE_TOKENS.put("\u0443", "y");
        REPLACE_TOKENS.put("\u0435", "e");
        REPLACE_TOKENS.put("\u0445", "x");
        REPLACE_TOKENS.put("\u0440", "p");
        REPLACE_TOKENS.put("\u043e", "o");
    }

    public static void loadFilters() {
        int fileCount = 0;
        try {
            for (String fileName : BotHelpers.getResourceListing(SwearHandler.class, "filters/")) {
                if (fileName.endsWith(".wlist")) {
                    fileCount++;
                    // Load the lines
                    String[] lines = new String(BotHelpers.bytesFromResource("filters/" + fileName)).split("\n");
                    for (String line : lines) {
                        filterPatterns.add(Pattern.compile("(^| )" + line.trim() + "( |$)", Pattern.CASE_INSENSITIVE));
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            GeyserBot.LOGGER.error("Failed to load filters", e);
        }

        GeyserBot.LOGGER.info("Loaded " + filterPatterns.size() + " filter patterns from " + fileCount + " files");

        nicknames = new String(BotHelpers.bytesFromResource("nicknames.wlist")).trim().split("\n");

        GeyserBot.LOGGER.info("Loaded " + nicknames.length + " nicknames");
    }

    @Nullable
    private Pattern checkString(String input) {
        // TODO: Maybe only clean start and end? Then run through the same as normalInput?
        input = input.toLowerCase();
        String cleanInput = CLEAN_PATTERN.matcher(input).replaceAll("");
        String cleanInputSpaces = CLEAN_PATTERN.matcher(input).replaceAll(" ");
        String normalInput = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Find all non ascii chars and normalise them based on REPLACE_TOKENS
        Matcher matcher = NON_ASCII_PATTERN.matcher(normalInput);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, REPLACE_TOKENS.getOrDefault(matcher.group(1), matcher.group(1)));
        }
        matcher.appendTail(sb);

        normalInput = sb.toString();

        for (Pattern filterPattern : filterPatterns) {
            if (filterPattern.matcher(cleanInput).find() || filterPattern.matcher(cleanInputSpaces).find() || filterPattern.matcher(normalInput).find()) {
                return filterPattern;
            }
        }

        return null;
    }

    public static String getRandomNick() {
        return nicknames[new Random().nextInt(nicknames.length)];
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        handleMessageEvent(event.getMessage(), true);
    }

//    Disabled for now
//    @Override
//    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
//        handleMessageEvent(event.getMessage(), false);
//    }

    private void handleMessageEvent(Message message, boolean notifyUser) {
        if (message.getAuthor().isBot() || !message.isFromGuild()) {
            return;
        }

        String disableFilter = GeyserBot.storageManager.getServerPreference(message.getGuild().getIdLong(), "disable-filter");
        if (disableFilter != null && !disableFilter.isEmpty()) {
            return;
        }

        Pattern filterPattern;
        if ((filterPattern = checkString(message.getContentRaw())) != null) {
            filteredMessages.add(message.getIdLong());

            // Delete message
            message.delete().queue(unused -> {
                if (notifyUser) {
                    // Alert the user message
                    message.getChannel().sendMessage(message.getAuthor().getAsMention() +
                            " your message has been removed because it contains profanity! Please read our rules for more information.").queue();
                }

                // Send a log to the admin channel
                ServerSettings.getLogChannel(message.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Profanity removed")
                        .setDescription("**Sender:** " + message.getAuthor().getAsMention() + "\n" +
                                "**Channel:** " + message.getChannel().getAsMention() + "\n" +
                                "**Regex:** `" + filterPattern + "`\n" +
                                "**Message:** " + message.getContentRaw())
                        .setColor(BotColors.FAILURE.getColor())
                        .build()).queue();

                // Remove the message from filteredMessages after 5s
                // this should be enough time for the rest of the events to fire
                GeyserBot.getGeneralThreadPool().schedule(() -> {
                    filteredMessages.remove(message.getIdLong());
                }, 5, TimeUnit.SECONDS);
            });
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (checkString(event.getUser().getName()) != null) {
            event.getMember().modifyNickname(getRandomNick()).queue();
        }
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent event) {
        String name = event.getNewNickname();
        if (name == null) {
            name = event.getUser().getName();
        }

        if (checkString(name) != null) {
            event.getMember().modifyNickname(getRandomNick()).queue();
        }
    }
}
