/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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
package pw.chew.chewbotcca.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Modified from https://github.com/Chewbotcca/Chewbotcca-Discord/blob/74f17a0d35490696642421b7aafb8154517148d1/src/main/java/pw/chew/chewbotcca/util/RestClient.java
 *
 * Off brand RestClient based on the ruby gem of the same name
 */
public class RestClient {
    public static final String JSON = "application/json; charset=utf-8";
    public static final String USER_AGENT = "GeyserMC-DiscordBot/1.0 (JDA; +https://geysermc.org) DBots/739572267855511652";

    private static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static Duration timeout = Duration.ofSeconds(30);
    private static boolean debug = true;

    /**
     * Gets the HTTP client used for this session
     *
     * @return The HTTP Client
     */
    public static HttpClient getHttpClient() {
        return client;
    }

    /**
     * Sets the timeout used for this session.
     *
     * @param timeout The new timeout.
     */
    public static void setTimeout(Duration timeout) {
        RestClient.timeout = timeout;
    }

    /**
     * Sets debug logging for this session.
     *
     * @param debug The new debug option.
     */
    public static void setDebug(boolean debug) {
        RestClient.debug = debug;
    }

    /**
     * Make a GET request
     *
     * @param url the url to get
     * @param headers Optional set of headers as "Header: Value" like "Authorization: Bearer bob"
     * @throws IllegalArgumentException If an invalid header is passed
     * @throws RuntimeException If the request fails
     * @return a String response
     */
    public static Response get(String url, String ...headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(timeout);

        for (String header : headers) {
            String[] details = header.split(":");
            if (details.length != 2) {
                throw new IllegalArgumentException("Invalid header syntax provided: " + header);
            }
            request.header(details[0].trim(), details[1].trim());
        }

        if (debug) LoggerFactory.getLogger(RestClient.class).debug("Making call to GET " + url);
        return performRequest(request.build());
    }

    /**
     * Make a POST Request
     *
     * @param url the url
     * @param data the body to send (will run through toString())
     * @param headers vararg of headers in "Key: Value" format
     * @throws IllegalArgumentException If an invalid header is passed
     * @throws RuntimeException If the request fails
     * @return a response
     */
    public static Response post(String url, Object data, String... headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .header("User-Agent", USER_AGENT)
                .timeout(timeout);

        for (String header : headers) {
            String[] details = header.split(":");
            if (details.length != 2) {
                throw new IllegalArgumentException("Invalid header syntax provided: " + header);
            }
            request.header(details[0].trim(), details[1].trim());
        }

        if (data instanceof JSONObject || data instanceof JSONArray) {
            request.header("Content-Type", JSON);
        }

        if (debug) LoggerFactory.getLogger(RestClient.class).debug("Making call to POST {}", url);
        return performRequest(request.build());
    }

    /**
     * Actually perform the request
     * @param request a request
     * @return a response
     */
    public static Response performRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            String body = response.body();
            if (debug) {
                LoggerFactory.getLogger(RestClient.class).debug("Response is " + body);
            }
            return new Response(code, body);
        } catch (IOException | InterruptedException e) {
            // Rethrow exceptions as runtime
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * A response from a REST call
     */
    public record Response(int code, String response) {
        /**
         * Check to see if the request was successful.
         * Codes 200-299 are considered successful.
         * @return true if successful
         */
        public boolean success() {
            return code >= 200 && code < 300;
        }

        /**
         * Get the response as a String
         * @return a String
         */
        public String asString() {
            return response;
        }

        /**
         * Get the response as a JSONObject
         * @return a JSONObject
         */
        public JSONObject asJSONObject() {
            return new JSONObject(response);
        }

        /**
         * Get the response as a JSONArray
         * @return a JSONArray
         */
        public JSONArray asJSONArray() {
            return new JSONArray(response);
        }

        /**
         * Get the response as a String
         * @return a String
         */
        @Override
        public String toString() {
            return asString();
        }
    }
}
