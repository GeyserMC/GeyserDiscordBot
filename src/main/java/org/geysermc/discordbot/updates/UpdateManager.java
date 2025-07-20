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

package org.geysermc.discordbot.updates;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpdateManager {

    private static final List<AbstractUpdateCheck> UPDATE_CHECKERS = new ArrayList<>();
    private static final List<TextChannel> UPDATE_CHANNELS = new ArrayList<>();

    static {
        UPDATE_CHECKERS.add(new JiraUpdateCheck("MC", "Java"));
        UPDATE_CHECKERS.add(new JiraUpdateCheck("MCPE", "Bedrock"));
        UPDATE_CHECKERS.add(new MinecraftUpdateCheck());
    }

    /**
     * Setup the initial caches and schedule the update check
     */
    public static void setup() {
        for (AbstractUpdateCheck updateCheck : UPDATE_CHECKERS) {
            try {
                updateCheck.populate();
            } catch (JSONException e) {
                GeyserBot.LOGGER.error("Unable to load update checker '" + updateCheck.getClass().getName() + "': ", e);
            }
        }

        GeyserBot.getGeneralThreadPool().scheduleAtFixedRate(UpdateManager::doUpdates, 60L, 60L, TimeUnit.SECONDS);
    }

    /**
     * Check for any updates and notify if there is
     */
    private static void doUpdates() {
        // Update the UPDATE_CHANNELS
        UPDATE_CHANNELS.clear();
        for (Guild guild : GeyserBot.getJDA().getGuilds()) {
            try {
                TextChannel updateChannel = ServerSettings.getUpdateChannel(guild);
                if (updateChannel != null) {
                    UPDATE_CHANNELS.add(updateChannel);
                }
            } catch (IllegalArgumentException ignored) { }
        }

        for (AbstractUpdateCheck updateCheck : UPDATE_CHECKERS) {
            try {
                updateCheck.check();
            } catch (JSONException e) {
                GeyserBot.LOGGER.error("Unable to run update checker '" + updateCheck.getClass().getName() + "': " + e.getMessage());
            }
        }
    }

    /**
     * Send a message into the known update channels
     *
     * @param message Message to send
     */
    public static void sendMessage(String message) {
        for (TextChannel updateChannel : UPDATE_CHANNELS) {
            Message msg = updateChannel.sendMessage(message).complete();
            // Crosspost or silently fail
            try {
                msg.crosspost().queue();
            } catch (UnsupportedOperationException | IllegalStateException | InsufficientPermissionException ignored) {}
        }
    }
}
