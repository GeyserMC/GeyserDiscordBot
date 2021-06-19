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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "";
        int code = 200;

        // 404 any non index requests
        if (!t.getRequestURI().getPath().equals("/")) {
            code = 404;

            respond(t, response, code);
            return;
        }

        Map<String, String> queryMap;
        if (t.getRequestURI().getRawQuery() == null || (queryMap = Server.queryToMap(t.getRequestURI().getRawQuery())) == null || !queryMap.containsKey("server")) {
            response = "No server id specified";
            code = 400;

            respond(t, response, code);
            return;
        }

        // Get the guild from the request
        long serverId;
        try {
            serverId = Long.parseLong(queryMap.get("server"));
        } catch (NumberFormatException e) {
            response = "Invalid server id specified";
            code = 400;

            respond(t, response, code);
            return;
        }

        // Check we are in the specified guild
        Optional<Guild> guild = GeyserBot.getJDA().getGuilds().stream().filter(filterGuild -> filterGuild.getIdLong() == serverId).findFirst();
        if (guild.isPresent()) {
            Map<String, Object> input = new HashMap<>();
            input.put("guild", guild.get());
            input.put("darkMode", true);
            input.put("rows", GeyserBot.storageManager.getLevels(serverId));

            // Get the darkMode cookie and set the bool for theme based on that
            if (t.getRequestHeaders().containsKey("Cookie")) {
                try {
                    for (String cookie : t.getRequestHeaders().get("Cookie").get(0).split(";")) {
                        String[] httpCookie = cookie.trim().split("=");
                        if (httpCookie[0].equals("darkMode")) {
                            input.put("darkMode", httpCookie.length > 1 && httpCookie[1].trim().equals("dark-mode"));
                            break;
                        }
                    }
                } catch (IndexOutOfBoundsException ignored) { }
            }

            try {
                response = GeyserBot.getHttpServer().processTemplate("index.ftl", input);
            } catch (TemplateException e) {
                response = e.getMessage();
                code = 500;
            }
        } else {
            response = "Bot not in specified server!";
            code = 404;
        }

        respond(t, response, code);
    }

    private void respond(HttpExchange t, String response, int code) throws IOException {
        t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(code, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
