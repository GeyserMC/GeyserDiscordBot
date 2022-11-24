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

import okhttp3.*;
import org.geysermc.discordbot.GeyserBot;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// Off brand RestClient based on the ruby gem of the same name
public class RestClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String USER_AGENT = "GeyserMC-9444/2.0 (JDA; +https://geysermc.org) DBots/739572267855511652";

    /**
     * Make a GET request
     * @param url the url to get
     * @return a JSONArray response
     * @throws RuntimeException when an (IO)Exception occurred while executing the request
     */
    public static JSONArray simpleGetJsonArray(String url) throws RuntimeException {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", USER_AGENT)
            .build();
        try {
            return performRequestJsonArray(request).body();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Make a GET request
     * @param url the url to get
     * @return a RestResponse with a JSONObject body
     */
    public static RestResponse<JSONObject> getJsonObject(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", USER_AGENT)
            .build();
        return performRequestJsonObject(request);
    }

    /**
     * Make a GET request
     * @param url the url to get
     * @return a JSONObject response
     */
    public static JSONObject simpleGetJsonObject(String url) {
        return getJsonObject(url).body();
    }

    /**
     * Make a GET request
     * @param url the url to get
     * @return a string response
     */
    public static String simpleGetString(String url) {
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("User-Agent", USER_AGENT)
            .build();
        return performRequestString(request).body();
    }

    /**
     * Make an Unauthenticated POST Request with a RequestBody
     * @param url the url
     * @param requestBody the requestBody to send
     * @return the JSONObject response
     */
    public static JSONObject simplePost(String url, RequestBody requestBody) {
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("User-Agent", USER_AGENT)
            .build();
        return performRequestJsonObject(request).body();
    }

    private static RestResponse<JSONArray> performRequestJsonArray(Request request) throws IOException {
        RestResponse<String> response = performRequestUnsafe(request);
        return new RestResponse<>(response.statusCode(), new JSONArray(response.body()));
    }

    private static RestResponse<JSONObject> performRequestJsonObject(Request request) {
        RestResponse<String> response;
        try {
            response = performRequestUnsafe(request);
        } catch (IOException exception) {
            response = new RestResponse<>(
                    -1,
                    "{\"error\": \"%s\"}".formatted(JSONObject.quote(exception.getMessage()))
            );
        }
        return new RestResponse<>(response.statusCode(), new JSONObject(response.body()));
    }

    private static RestResponse<String> performRequestString(Request request) {
        try {
            return performRequestUnsafe(request);
        } catch (IOException exception) {
            return new RestResponse<>(-1, exception.getMessage());
        }
    }

    private static RestResponse<String> performRequestUnsafe(Request request) throws IOException {
        // GeyserMC - Replace JDA call with our JDA
        OkHttpClient client = GeyserBot.getJDA() == null ? new OkHttpClient() : GeyserBot.getJDA().getHttpClient();

        LoggerFactory.getLogger(RestClient.class).debug(
                "Making call to %s %s".formatted(request.method(), request.url())
        );
        try (Response response = client.newCall(request).execute()) {
            //noinspection ConstantConditions - responseBody cannot be null when using 'execute'
            String body = response.body().string();
            LoggerFactory.getLogger(RestClient.class).debug("Response is " + body);
            return new RestResponse<>(response.code(), body);
        } catch (IOException exception) {
            LoggerFactory.getLogger(RestClient.class).warn(
                    "Call to %s failed with %s!".formatted(request.url(), exception.getClass().getSimpleName())
            );
            throw exception;
        }
    }

    public record RestResponse<T>(int statusCode, T body) {
    }
}
