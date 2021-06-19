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
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.LevelInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Staging server:
 * http://127.0.0.1:8000/?guild=742759234906751017
 */
public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        long guildId = 0L;

        String paramGuild;
        if ((paramGuild = Server.queryToMap(t.getRequestURI().getRawQuery()).get("guild")) != null) {
            try {
                guildId = Long.parseLong(paramGuild);
            } catch (NumberFormatException ignored) { }
        }

        String response;

        long finalGuildId = guildId;
        if (GeyserBot.getJDA().getGuilds().stream().anyMatch(guild -> guild.getIdLong() == finalGuildId)) {
            Map<String, Object> input = new HashMap<>();
            input.put("rows", GeyserBot.storageManager.getLevels(guildId));

            try {
                response = GeyserBot.getHttpServer().processTemplate("index.ftl", input);
            } catch (TemplateException e) {
                response = e.getMessage();
            }
        } else {
            response = "Bot not in specified guild!";
        }

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
