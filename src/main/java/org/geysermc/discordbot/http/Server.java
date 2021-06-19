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

import com.sun.net.httpserver.HttpServer;
import freemarker.template.*;
import org.geysermc.discordbot.util.PropertiesManager;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Server {
    private HttpServer server;
    private Configuration cfg;

    public Server() throws Exception {
        server = HttpServer.create(PropertiesManager.getWebAddress(), 0);
        server.createContext("/", new IndexHandler());
        server.setExecutor(null); // creates a default executor

        cfg = new Configuration(new Version(2, 3, 31));

        cfg.setClassForTemplateLoading(Server.class, "/web");

        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public String processTemplate(String name, Map<String, Object> input) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Template template = cfg.getTemplate(name);
        template.process(input, stringWriter);
        return stringWriter.toString();
    }

    public static Map<String, String> queryToMap(String query) {
        if(query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            try {
                param = URLDecoder.decode(param, "UTF-8");
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            } catch (UnsupportedEncodingException ignored) { }
        }
        return result;
    }

    public static String getUrl(long idLong) {
        return String.format("%s/?guild=%s", PropertiesManager.getPublicWebAddress(), idLong);
    }
}
