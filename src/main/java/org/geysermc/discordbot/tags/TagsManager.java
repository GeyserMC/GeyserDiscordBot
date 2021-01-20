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

package org.geysermc.discordbot.tags;

import com.jagrosh.jdautilities.command.Command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TagsManager {

    private static final List<Command> TAGS = new ArrayList<>();
    private static boolean tagsLoaded = false;

    public static List<Command> getTags() {
        if (!tagsLoaded) {
            loadTags();
        }

        return TAGS;
    }

    private static void loadTags() {
        TAGS.add(new TagAliasCommand());

        for (File folder : new File(TagsManager.class.getClassLoader().getResource("tags").getPath()).listFiles()) {
            if (folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".tag")) {
                        try {
                            String tagName = file.getName().replace(".tag", "");

                            String[] lines = new String(Files.readAllBytes(file.toPath())).split("\n");
                            Map<String, String> tagData = new HashMap<>();
                            StringBuilder content = new StringBuilder();
                            boolean hitSeperator = false;

                            // Get all the tag data
                            for (String line : lines) {
                                line = line.trim();

                                if (hitSeperator) {
                                    content.append(line).append("\n");
                                    continue;
                                }

                                if (line.equals("---")) {
                                    hitSeperator = true;
                                    continue;
                                }

                                String[] lineParts = line.trim().split(":");
                                switch (lineParts[0]) {
                                    case "type":
                                    case "aliases":
                                        tagData.put(lineParts[0], lineParts[1].trim().toLowerCase());
                                        break;

                                    case "image":
                                        tagData.put("image", String.join(":", Arrays.copyOfRange(lineParts, 1, lineParts.length)).trim());
                                        break;

                                    default:
                                        // TODO: Add a decent error message
                                        // Invalid tag option %s for tag %s
                                        break;
                                }
                            }

                            // Create the tag from the stored data
                            Command tag = null;
                            switch (tagData.get("type")) {
                                case "text":
                                    tag = new EmbedTag(tagName, content.toString(), tagData.get("image"), tagData.get("aliases"));
                                    break;

                                case "text-raw":
                                    tag = new RawTag(tagName, content.toString(), tagData.get("aliases"));
                                    break;

                                default:
                                    // TODO: Add a decent error message saying type is invalid
                                    break;
                            }

                            if (tag != null) {
                                TAGS.add(tag);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            // TODO: Add a decent error message
                        }
                    }
                }
            }
        }

        tagsLoaded = true;
    }
}
