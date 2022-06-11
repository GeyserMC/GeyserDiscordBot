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

package org.geysermc.discordbot.util;

import java.net.InetSocketAddress;
import java.util.Properties;

public class PropertiesManager {
    private static Properties properties;

    public static void loadProperties(Properties config) {
        properties = config;
    }

    /**
     * @return Bot token from discord
     */
    public static String getToken() {
        return properties.getProperty("token");
    }

    /**
     * @return Bot prefix
     */
    public static String getPrefix() {
        return properties.getProperty("prefix");
    }

    /**
     * @return Database connection type
     */
    public static String getDatabaseType() {
        return properties.getProperty("db-type");
    }

    /**
     * @return Database server hostname
     */
    public static String getHost() {
        return properties.getProperty("db-host");
    }

    /**
     * @return Database server database
     */
    public static String getDatabase() {
        return properties.getProperty("db-database");
    }

    /**
     * @return Database server database
     */
    public static String getUser() {
        return properties.getProperty("db-user");
    }

    /**
     * @return Database server database
     */
    public static String getPass() {
        return properties.getProperty("db-pass");
    }

    /**
     * @return Should the web server be enabled
     */
    public static boolean enableWeb() {
        return properties.containsKey("web-address");
    }

    /**
     * @return Web server address and port
     */
    public static InetSocketAddress getWebAddress() {
        return new InetSocketAddress(properties.getProperty("web-address"), Integer.parseInt(properties.getProperty("web-port")));
    }

    /**
     * @return Web server public address
     */
    public static String getPublicWebAddress() {
        return properties.getProperty("web-public-address");
    }

    /**
     * @return GitHub OAuth token
     */
    public static String getGithubToken() {
        return properties.getProperty("github-token");
    }

    /**
     * @return Sentry DSN
     */
    public static String getSentryDsn() {
        return properties.getProperty("sentry-dsn");
    }

    /**
     * @return Sentry environment
     */
    public static String getSentryEnv() {
        return properties.getProperty("sentry-env");
    }

    /**
     * @return Ocr path
     */
    public static String getOCRPath() { return properties.getProperty("ocr-path");
    }
}