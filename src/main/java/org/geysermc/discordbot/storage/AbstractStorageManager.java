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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;

public abstract class AbstractStorageManager {

    /**
     * Do any initial setup for the storage when the bot starts
     */
    public abstract void setupStorage();

    /**
     * Close any connections or do clean-up
     */
    public abstract void closeStorage();

    /**
     * Get a preference from the database
     *
     * @param serverID Guild ID to get the preference for
     * @param preference Key of the requested preference
     * @return The string value of the preference
     */
    public abstract String getServerPreference(long serverID, String preference);

    /**
     * Set a preference in the database
     *
     * @param serverID Guild ID to set the preference for
     * @param preference Key of the preference to change
     * @param value Value to set the preference to
     */
    public abstract void setServerPreference(long serverID, String preference, String value);

    /**
     * Store a persistent role in the database
     *
     * @param member {@link Member} to store the role for
     * @param role {@link Role} to store for the {@link Member}
     */
    public abstract void addPersistentRole(Member member, Role role);

    /**
     * Remove a persistent role from the database
     *
     * @param member {@link Member} to remove the role for
     * @param role {@link Role} to remove for the {@link Member}
     */
    public abstract void removePersistentRole(Member member, Role role);

    /**
     * Get a list of persistent roles for a {@link Member}
     *
     * @param member The Guild {@link Member} to get persistent roles for
     * @return List of the Guilds {@link Role} marked as persistent for the {@link Member}
     */
    public abstract List<Role> getPersistentRoles(Member member);

    /**
     * Log a moderation event to the database
     *
     * @param user Guild {@link Member} that created the event
     * @param action The action that was done
     * @param target The {@link User} that was targeted
     * @param reason The reason for this if specified
     * @return The ID of the inserted moderation log
     */
    public abstract int addLog(Member user, String action, User target, String reason);

    /**
     * Get the moderation log for a user
     *
     * @param guild Guild to get the log for
     * @param target The {@link User} that was targeted
     * @param limit The limit of results to get
     * @return List of {@link ModLog} returned
     */
    public abstract List<ModLog> getLogs(Guild guild, User target, int limit);

    /**
     * Calls {@link AbstractStorageManager#getLogs(Guild, User, int)} with the default limit of 5
     *
     * @see AbstractStorageManager#getLogs(Guild, User, int)
     */
    public List<ModLog> getLogs(Guild guild, User target) {
        return getLogs(guild, target, 5);
    }

    /**
     * Get a moderation log by id
     *
     * @param guild Guild to get the log for
     * @param id The id of the wanted log
     * @return A {@link ModLog}
     */
    public abstract ModLog getLog(Guild guild, int id);

    /**
     * Update the reason for a moderation log
     *
     * @param guild Guild to update the log of
     * @param id The id of the log to update
     * @param reason The new reason for the log
     */
    public abstract void updateLog(Guild guild, int id, String reason);

    /**
     * Get the level info for a given Guild Member
     *
     * @param user Member to get level for
     * @return The level info from the database
     */
    public abstract LevelInfo getLevel(Member user);

    /**
     * Set the level data for a given Guild Member
     * @param user Member to set the level data of
     * @param levelInfo The data to set
     */
    public abstract void setLevel(Member user, LevelInfo levelInfo);

    /**
     * @see AbstractStorageManager#getLevels(long)
     */
    public List<LevelInfo> getLevels(Guild guild) {
        return getLevels(guild.getIdLong());
    }

    /**
     * Get the top 100 levels for a given guild id
     *
     * @param guildId The guild to get the levels for
     * @return A list of up to 100 level info entries
     */
    public abstract List<LevelInfo> getLevels(long guildId);

    /**
     * Get the slow mode channels for a {@link Guild}
     *
     * @param guild The Guild to get the channels for
     * @return The slow mode channels
     */
    public abstract List<SlowModeInfo> getSlowModeChannels(Guild guild);

    /**
     * Enable slow mode for a channel with the given delay
     *
     * @param channel The channel to set the delay on
     * @param delay Delay between messages in seconds
     */
    public abstract void setSlowModeChannel(TextChannel channel, int delay);
}
