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

package org.geysermc.discordbot.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import freemarker.template.TemplateException;
import org.geysermc.discordbot.GeyserBot;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class PageHandler implements HttpHandler {
    protected String response = "";
    protected int code = 200;
    protected boolean cache = false;
    protected int cacheTime = 1800;
    protected boolean cacheImmutable = false;

    public abstract String requestUrl();

    public String requestUrlRegex() {
        return requestUrl();
    }

    protected abstract void handleRequest(HttpExchange t);

    @Override
    public void handle(HttpExchange t) throws IOException {
        // 404 any non index requests
        if (!t.getRequestURI().getPath().matches(requestUrlRegex())) {
            code = 404;
            response = "Not found";
        } else {
            handleRequest(t);
        }

        // Handle errors
        if (code != 200) {
            Map<String, Object> input = new HashMap<>();
            input.put("errorCode", code);
            input.put("errorMessage", response);
            buildTemplate(t, "error.ftl", input);
        }

        respond(t, response, code);
    }

    protected void buildTemplate(HttpExchange t, String template, Map<String, Object> input) {
        input.put("darkMode", true);

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
            response = GeyserBot.getHttpServer().processTemplate(template, input);
            code = 200;
        } catch (TemplateException | IOException e) {
            response = e.getMessage();
            code = 500;
        }
    }

    private void respond(HttpExchange t, String response, int code) throws IOException {
        t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

        if (cache) {
            t.getResponseHeaders().set("Cache-Control", String.format("max-age=%d%s, public", cacheTime, (cacheImmutable ? ", immutable" : "")));
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static void register(HttpServer server, Class<? extends PageHandler> clazz) {
        try {
            PageHandler pageHandler = clazz.getDeclaredConstructor().newInstance();
            server.createContext(pageHandler.requestUrl(), pageHandler);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
