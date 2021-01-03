package org.geysermc.discordbot.util;

import java.util.Properties;

// config.json and .env manager
public class PropertiesManager {
    public static Properties properties;

    public static void loadProperties(Properties config) {
        properties = config;
    }

    /**
     * @return bot token from discord
     */
    public static String getToken() {
        return System.getenv("BOT_TOKEN");
    }

    /**
     * @return who can edit the config
     */
    public static String getConfigEditingUsers() {
        return System.getenv("CONFIG_EDITING_USERS");
    }

    /**
     * @return what groups can edit config
     */
    public static String getConfigEditingGroups() {
        return System.getenv("CONFIG_EDITING_GROUPS");
    }

    /**
     * @return Top.gg token for DBL/Top.gg bot info command
     */
    public static String getTopggToken() {
        return properties.getProperty("dbl");
    }

    /**
     * @return Owner ID of the bot, all perms
     */
    public static String getOwnerId() {
        return properties.getProperty("owner_id");
    }

    /**
     * @return Wordnik token for define command
     */
    public static String getWordnikToken() {
        return properties.getProperty("wordnik");
    }

    /**
     * @return Google key for YouTube command
     */
    public static String getGoogleKey() {
        return properties.getProperty("google");
    }

    /**
     * @return Last.fm token for LastFM command
     */
    public static String getLastfmToken() {
        return properties.getProperty("lastfm");
    }

    /**
     * @return Bot prefix
     */
    public static String getPrefix() {
        return properties.getProperty("prefix");
    }

    /**
     * @return Github token for github commands
     */
    public static String getGithubToken() {
        return properties.getProperty("github");
    }

    /**
     * @return Sentry DSN for error tracking
     */
    public static String getSentryDsn() {
        return properties.getProperty("sentry-dsn");
    }

    /**
     * @return Sentry Environment for error tracking
     */
    public static String getSentryEnv() {
        return properties.getProperty("sentry-env");
    }

    /**
     * @return Chew's API key for Chew's bot profiles/server settings.
     */
    public static String getChewKey() {
        return properties.getProperty("chewkey");
    }

    /**
     * @return a DiscordExtremeList api token
     */
    public static String getDELToken() {
        return properties.getProperty("del");
    }

    /**
     * @return the key for paste.gg
     */
    public static String getPasteGgKey() {
        return properties.getProperty("pastegg");
    }

    /**
     * @return the Memerator API key
     */
    public static String getMemeratorKey() {
        return properties.getProperty("memerator");
    }
}