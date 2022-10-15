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

package org.geysermc.discordbot.storage;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.geysermc.discordbot.GeyserBot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class gives easy methods for accessing stored data about a server
 * such as configs and settings
 */
public class ServerSettings {

    /**
     * Get a preference as a list of strings delimited by `,`
     *
     * @param serverID ID of the guild to get the preference for
     * @param key The preference key to get
     * @return The list of strings or null if it doesn't exist
     */
    public static List<String> getList(long serverID, String key) {
        String listData = GeyserBot.storageManager.getServerPreference(serverID, key);

        if (listData == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(Arrays.asList(listData.split(",").clone()));
    }

    public static void setList(long serverID, String key, List<String> data) {
        GeyserBot.storageManager.setServerPreference(serverID, key, StringUtils.join(data, ","));
    }

    /**
     * Get a preference as a map of strings delimited by `,` and separated by `|`
     *
     * @param serverID ID of the guild to get the preference for
     * @param key The preference key to get
     * @return The map of strings or empty if it doesn't exist
     */
    public static Map<String, String> getMap(long serverID, String key) {
        String mapData = GeyserBot.storageManager.getServerPreference(serverID, key);
        Map<String, String> map = new HashMap<>();

        if (mapData != null) {
            for (String entry : mapData.split(",")) {
                String[] entryData = entry.split("\\|");
                map.put(entryData[0], entryData[1]);
            }
        }

        return map;
    }

    /**
     * Get the log channel for the selected guild
     *
     * @param guild ID of the guild to get the channel for
     * @return The {@link TextChannel} for logs
     * @throws IllegalArgumentException If the channel is null or invalid
     */
    public static TextChannel getLogChannel(Guild guild) throws IllegalArgumentException {
        String channel = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "log-channel");
        return guild.getTextChannelById(channel);
    }

    /**
     * Get the update channel for the selected guild
     *
     * @param guild ID of the guild to get the channel for
     * @return The {@link TextChannel} for updates
     * @throws IllegalArgumentException If the channel is null or invalid
     */
    public static TextChannel getUpdateChannel(Guild guild) throws IllegalArgumentException {
        String channel = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "update-channel");
        return guild.getTextChannelById(channel);
    }

    /**
     * Get the voice role for the selected guild
     *
     * @param guild  ID of the guild to get the channel for
     * @return The {@link Role} for users in the voice channel
     * @throws IllegalArgumentException If the role is null or invalid
     */
    public static Role getVoiceRole(Guild guild) throws IllegalArgumentException {
        String role = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), "voice-role");
        return guild.getRoleById(role);
    }

    /**
     * Check if the given channel should be excluded from logs
     *
     * @param channel The {@link MessageChannel} to check
     * @return If we should exclude the channel
     */
    public static boolean shouldNotLogChannel(MessageChannel channel) {
        Guild guild = getGuild(channel);

        if (guild == null) {
            return true;
        }

        List<String> dontLog = getList(guild.getIdLong(), "dont-log");
        return dontLog.contains(channel.getId());
    }

    /**
     * Check if the given channel should be excluded from checking errors
     *
     * @param channel The {@link MessageChannel} to check
     * @return If we should exclude the channel
     */
    public static boolean shouldNotCheckError(MessageChannel channel) {

        if (getGuild(channel) == null) {
            return true;
        }

        return getList(channel.getIdLong(), "dont-check-error").contains(channel.getId());
    }

    /**
     * Check if the given channel should be excluded from the level system
     * if value is 0 then disables all channels
     *
     * @param channel The {@link MessageChannel} to check
     * @return If we should exclude the channel
     */
    public static boolean shouldDisableLevels(MessageChannel channel) {
        Guild guild = getGuild(channel);

        if (guild == null) {
            return true;
        }

        List<String> dontLevel = getList(guild.getIdLong(), "dont-level");
        return dontLevel.size() > 0 && dontLevel.get(0).equals("0") || dontLevel.contains(channel.getId());
    }

    /**
     * Get the guild from a {@link MessageChannel}
     *
     * @param channel The channel to get the guild for
     * @return The guild or null
     */
    @Nullable
    private static Guild getGuild(MessageChannel channel) {
        Guild guild = null;

        if (channel instanceof GuildChannel guildChannel) {
            guild = guildChannel.getGuild();
        }

        return guild;
    }

    /**
     * Check if the given guild has leveling disabled
     *
     * @param guild The {@link Guild} to check
     * @return If levels are disabled
     */
    public static boolean serverLevelsDisabled(Guild guild) {
        List<String> dontLevel = getList(guild.getIdLong(), "dont-level");
        return dontLevel.size() > 0 && dontLevel.get(0).equals("0");
    }
}
