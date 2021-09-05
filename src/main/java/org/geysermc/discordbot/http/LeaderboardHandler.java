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

package org.geysermc.discordbot.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import freemarker.template.TemplateException;
import net.dv8tion.jda.api.entities.Guild;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LeaderboardHandler extends PageHandler {

    @Override
    public String requestUrl() {
        return "/leaderboard/";
    }

    @Override
    public String requestUrlRegex() {
        return "/leaderboard/.*";
    }

    @Override
    protected void handleRequest(HttpExchange t) {
        String serverIdArg = t.getRequestURI().getPath().replace(requestUrl(), "").trim();
        if (serverIdArg.isEmpty()) {
            response = "No server id specified";
            code = 400;

            return;
        }

        // Get the guild from the request
        long serverId;
        try {
            serverId = Long.parseLong(serverIdArg);
        } catch (NumberFormatException e) {
            response = "Invalid server id specified";
            code = 400;

            return;
        }

        // Check we are in the specified guild
        Optional<Guild> guild = GeyserBot.getJDA().getGuilds().stream().filter(filterGuild -> filterGuild.getIdLong() == serverId).findFirst();
        if (guild.isPresent()) {
            if (!ServerSettings.serverLevelsDisabled(guild.get())) {
                Map<String, Object> input = new HashMap<>();
                input.put("guild", guild.get());
                input.put("rows", GeyserBot.storageManager.getLevels(serverId));

                buildTemplate(t, "leaderboard.ftl", input);
            } else {
                response = "Levels are disabled for this server!";
                code = 403;
            }
        } else {
            response = "Bot not in specified server!";
            code = 404;
        }
    }
}
