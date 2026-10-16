/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.background;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.time.OffsetDateTime;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.guild.SecurityIncidentActions;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;

public class SecurityActionsApplier implements Runnable {
    @Override
    public void run() {
        Long2ObjectMap<List<String>> enabledActionsPerGuild = ServerSettings.getListForAllServers("security-actions");
        if (enabledActionsPerGuild.isEmpty()) {
            return;
        }

        // The max is 24h, so just set it to 12h to be sure.
        // It runs every hour anyway.
        OffsetDateTime later = OffsetDateTime.now().plusHours(12);

        for (Long2ObjectMap.Entry<List<String>> entry : enabledActionsPerGuild.long2ObjectEntrySet()) {
            Guild guild = GeyserBot.getJDA().getGuildById(entry.getLongKey());
            if (guild == null || !guild.getSelfMember().hasPermission(Permission.MANAGE_SERVER)) continue;

            boolean invitesDisabled = entry.getValue().contains("invites");
            boolean dmsDisabled = entry.getValue().contains("dms");

            if (invitesDisabled || dmsDisabled) {
                guild.modifySecurityIncidents(SecurityIncidentActions.enabled(
                    invitesDisabled ? later : null,
                    dmsDisabled ? later : null
                )).queue();
                continue;
            }

            SecurityIncidentActions actions = guild.getSecurityIncidentActions();
            if (actions.getDirectMessagesDisabledUntil() != null || actions.getInvitesDisabledUntil() != null) {
                guild.modifySecurityIncidents(SecurityIncidentActions.disabled()).queue();
            }
        }
    }
}
