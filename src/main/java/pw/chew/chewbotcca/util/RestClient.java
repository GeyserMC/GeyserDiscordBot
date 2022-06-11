/*
 * Copyright (C) 2020 Chewbotcca
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pw.chew.chewbotcca.util;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.geysermc.discordbot.GeyserBot;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Off brand RestClient based on the ruby gem of the same name
public class RestClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Make a GET request
     * @param url the url to get
     * @return a response
     */
    public static String get(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request);
    }

    /**
     * Make an Authenticated GET Request
     * @param url the url
     * @param key the auth key
     * @return a response
     */
    public static String get(String url, String key) {
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", key)
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .get()
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request);
    }

    /**
     * Make an Authenticated POST Request
     * @param url the url
     * @param args the arguments to pass
     * @param key the auth key
     * @return a response
     */
    public static String post(String url, HashMap<String, Object> args, String key) {
        Request request = new Request.Builder()
            .url(url)
            .post(bodyFromHash(args))
            .addHeader("Authorization", key)
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to POST " + url);
        return performRequest(request);
    }

    /**
     * Make an Unauthenticated POST Request with JSON Body
     * @param url the url
     * @param json the json body to send
     * @return a response
     */
    public static String post(String url, JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to POST " + url);
        return performRequest(request);
    }

    /**
     * Make an Authenticated POST Request with JSON Body
     * @param url the url
     * @param key the auth key
     * @param json the json body to send
     * @return a response
     */
    public static String post(String url, String key, JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), JSON);

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", key)
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to POST " + url);
        return performRequest(request);
    }

    /**
     * Make an Authenticated DELETE Request
     * @param url the url
     * @param key the auth key
     * @return a response
     */
    public static String delete(String url, String key) {
        Request request = new Request.Builder()
            .url(url)
            .delete()
            .addHeader("Authorization", key)
            .addHeader("User-Agent", "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652") // GeyserMC - Replace with our bot user agent
            .build();

        LoggerFactory.getLogger(RestClient.class).debug("Making call to DELETE " + url);
        return performRequest(request);
    }

    /**
     * Actually perform the request
     * @param request a request
     * @return a response
     */
    public static String performRequest(Request request) {
        // GeyserMC - Replace JDA call with our JDA
        OkHttpClient client = GeyserBot.getJDA() == null ? new OkHttpClient() : GeyserBot.getJDA().getHttpClient();
        try (Response response = client.newCall(request).execute()) {
            String body;
            ResponseBody responseBody = response.body();
            if(responseBody == null) {
                body = "{}";
            } else {
                body = responseBody.string();
            }
            LoggerFactory.getLogger(RestClient.class).debug("Response is " + body);
            return body;
        } catch (SSLHandshakeException e) {
            LoggerFactory.getLogger(RestClient.class).warn("Call to " + request.url() + " failed with SSLHandshakeException!");
            return "{error: 'SSLHandshakeException'}";
        } catch (IOException e) {
            LoggerFactory.getLogger(RestClient.class).warn("Call to " + request.url() + " failed with IOException!");
            return "{error: 'IOException'}";
        }
    }

    public static RequestBody bodyFromHash(HashMap<String, Object> args) {
        FormBody.Builder bodyArgs = new FormBody.Builder();
        if (args == null)
            return bodyArgs.build();
        for(Map.Entry<String, Object> entry : args.entrySet()) {
            bodyArgs.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return bodyArgs.build();
    }
}
