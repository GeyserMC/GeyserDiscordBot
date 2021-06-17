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

package org.geysermc.discordbot.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.geysermc.discordbot.GeyserBot;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

public class BotHelpers {

    private static final Int2ObjectMap<String> BEDROCK_VERSIONS = new Int2ObjectOpenHashMap<>();

    static {
        BEDROCK_VERSIONS.put(291, "1.7.0");
        BEDROCK_VERSIONS.put(313, "1.8.0");
        BEDROCK_VERSIONS.put(332, "1.9.0");
        BEDROCK_VERSIONS.put(340, "1.10.0");
        BEDROCK_VERSIONS.put(354, "1.11.0");
        BEDROCK_VERSIONS.put(361, "1.12.0");
        BEDROCK_VERSIONS.put(388, "1.13.0");
        BEDROCK_VERSIONS.put(389, "1.14.0 - 1.14.50");
        BEDROCK_VERSIONS.put(390, "1.14.60");
        BEDROCK_VERSIONS.put(407, "1.16.0 - 1.16.10");
        BEDROCK_VERSIONS.put(408, "1.16.20");
        BEDROCK_VERSIONS.put(419, "1.16.100");
        BEDROCK_VERSIONS.put(422, "1.16.200 - 1.16.201");
        BEDROCK_VERSIONS.put(428, "1.16.210");
        BEDROCK_VERSIONS.put(431, "1.16.220");
        BEDROCK_VERSIONS.put(440, "1.17.0");
    }

    /**
     * Get a guild member from a given id string
     *
     * @param guild Guild to get the member for
     * @param userTag The tag to use to find the member
     * @return The found Member or null
     */
    @Nullable
    public static Member getMember(Guild guild, String userTag) {
        try {
            return guild.getMember(getUser(userTag));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Get a discord user from a given id string
     * Input examples:
     *  <@!1234>
     *  1234
     *  abc#1234
     *
     * @param userTag The tag to use to find the member
     * @return The found User or null
     */
    @Nullable
    public static User getUser(String userTag) {
        try {
            // Check for a mention (<@!1234>)
            if (userTag.startsWith("<@!") && userTag.endsWith(">")) {
                userTag = userTag.substring(3, userTag.length() - 1);
            } else {
                // Check for a user tag (example#1234)
                Matcher m = User.USER_TAG.matcher(userTag);
                if (m.matches()) {
                    return GeyserBot.getJDA().getUserByTag(m.group(1), m.group(2));
                }
            }

            // Try to get the member by ID
            return GeyserBot.getJDA().retrieveUserById(userTag).complete();
        } catch (NumberFormatException | ErrorResponseException ignored) {
            return null;
        }
    }

    /**
     * Get a discord role from a given id string
     * Input examples:
     *  <@&1234>
     *  1234
     *  admin
     *
     * @param guild The guild to find the role in
     * @param roleTag The tag to use to find the member
     * @return The found User or null
     */
    @Nullable
    public static Role getRole(Guild guild, String roleTag) {
        try {
            // Check for a mention (<@&1234>)
            if (roleTag.startsWith("<@&") && roleTag.endsWith(">")) {
                roleTag = roleTag.substring(3, roleTag.length() - 1);
            } else {
                // Find the role by name
                List<Role> foundRole = guild.getRolesByName(roleTag, false);
                if (!foundRole.isEmpty()) {
                    return foundRole.get(0);
                }
            }

            // Try to get the role by ID
            return guild.getRoleById(roleTag);
        } catch (NumberFormatException | ErrorResponseException ignored) {
            return null;
        }
    }

    private static final char[] FORMAT_CHARS = new char[]{'k', 'm', 'b', 't'};

    public static String coolFormat(int n) {
        return n < 1000 ? String.valueOf(n) : coolFormat(n, 0);
    }

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invocation.
     *
     * https://stackoverflow.com/a/4753866/5299903
     *
     * @param n the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100L) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + FORMAT_CHARS[iteration])
                : coolFormat(d, iteration+1));

    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * http://www.uofr.net/~greg/java/get-resource-listing.html
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
        URL dirURL = clazz.getClassLoader().getResource(path);
        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
        }

        if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/")+".class";
            dirURL = clazz.getClassLoader().getResource(me);
        }

        if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
            Set<String> result = new HashSet<>(); //avoid duplicates in case it is a subdirectory
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(path)) { //filter according to the path
                    String entry = name.substring(path.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[0]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
    }

    /**
     * @param resourcePath the resource to read off of
     * @return the byte array of an InputStream
     */
    public static byte[] bytesFromResource(String resourcePath) {
        InputStream stream = BotHelpers.class.getClassLoader().getResourceAsStream(resourcePath);

        try {
            int size = stream.available();
            byte[] bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(stream);
            buf.read(bytes, 0, bytes.length);
            buf.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Error while trying to read input stream!");
        }
    }

    /**
     * Get the name of a bedrock version from the protocol version number
     *
     * @param protocolVersion Protocol version number
     * @return The name of the version or 'Unknown'
     */
    public static String getBedrockVersionName(int protocolVersion) {
        return BEDROCK_VERSIONS.getOrDefault(protocolVersion, "Unknown");
    }

    /**
     * Get a time in seconds from a time string
     * EG: 1h2m
     * https://stackoverflow.com/a/4015476/5299903
     *
     * @param input Time string to parse
     * @return Time in seconds
     */
    public static int parseTimeString(String input) {
        int result = 0;
        String number = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c) && !number.isEmpty()) {
                result += convert(Integer.parseInt(number), c);
                number = "";
            }
        }
        return result;
    }

    private static int convert(int value, char unit) {
        switch (unit) {
            case 'd' : return value * 60 * 60 * 24;
            case 'h' : return value * 60 * 60;
            case 'm' : return value * 60;
            case 's' : return value;
        }
        return 0;
    }
}
