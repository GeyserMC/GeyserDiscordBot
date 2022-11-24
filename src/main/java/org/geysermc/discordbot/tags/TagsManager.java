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

package org.geysermc.discordbot.tags;

import com.jagrosh.jdautilities.command.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.BotHelpers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagsManager {

    private static final List<Command> TAGS = new ArrayList<>();
    private static final Map<String, String> ISSUE_RESPONSES = new HashMap<>();
    private static final Map<String, String> SELF_HELP = new HashMap<>();
    private static boolean tagsLoaded = false;

    public static List<Command> getTags() {
        if (!tagsLoaded) {
            loadTags();
        }

        return TAGS;
    }

    /**
     * An issue to response Map. All keys and values have no leading or trailing whitespace.
     *
     * @return The issue to response Map.
     */
    public static Map<String, String> getIssueResponses() {
        if (!tagsLoaded) {
            loadTags();
        }

        return ISSUE_RESPONSES;
    }

    public static Map<String, String> getSelfHelp() {
        if (!tagsLoaded) {
            loadTags();
        }

        return SELF_HELP;
    }

    private static void loadTags() {
        TAGS.add(new TagAliasCommand());

        try {
            for (String folderName : BotHelpers.getResourceListing(TagsManager.class, "tags/")) {
                try {
                    String[] files = BotHelpers.getResourceListing(TagsManager.class, "tags/" + folderName + "/");

                    if (files == null) {
                        continue;
                    }

                    for (String fileName : files) {
                        if (fileName.endsWith(".tag")) {
                            String tagName = fileName.replace(".tag", "");

                            String[] lines = new String(BotHelpers.bytesFromResource("tags/" + folderName + "/" + fileName)).split("\n");
                            Map<String, String> tagData = new HashMap<>();
                            String[] issueTriggers = null;
                            String[] selfHelpTrigger = null;
                            StringBuilder content = new StringBuilder();
                            List<Button> buttons = new ArrayList<>();

                            boolean hitSeparator = false;

                            // Get all the tag data
                            for (String line : lines) {
                                line = line.trim();

                                if (hitSeparator) {
                                    content.append(line).append("\n");
                                    continue;
                                } else if (line.equals("---")) {
                                    hitSeparator = true;
                                    continue;
                                } else if (!line.contains(":")) {
                                    continue;
                                }

                                String[] lineParts = line.trim().split(":", 2);
                                if (lineParts.length < 2 || lineParts[0].isEmpty() || lineParts[1].isEmpty()) {
                                    GeyserBot.LOGGER.warn("Invalid tag option line '" + line.trim() + "' for tag '" + tagName + "'!");
                                    continue;
                                }

                                switch (lineParts[0]) { // intentional fallthrough
                                    case "type", "aliases" -> tagData.put(lineParts[0], lineParts[1].trim().toLowerCase());
                                    case "image" -> tagData.put(lineParts[0], lineParts[1].trim());
                                    case "issues" -> issueTriggers = lineParts[1].split("\\|\\|");
                                    case "help" -> selfHelpTrigger = lineParts[1].split("\\|\\|");
                                    case "button" -> {
                                        String[] data = lineParts[1].trim().replace("[", "").replace(")", "").split("]\\(");
                                        buttons.add(Button.link(data[1], data[0]));
                                    }
                                    default -> GeyserBot.LOGGER.warn("Invalid tag option key '" + lineParts[0] + "' for tag '" + tagName + "'!");
                                }
                            }

                            // Create the tag from the stored data
                            switch (tagData.get("type")) {
                                case "text":
                                    try {
                                        TAGS.add(new EmbedTag(tagName, content.toString().trim(), tagData.get("image"), tagData.get("aliases"), buttons));
                                    } catch (IllegalArgumentException e) {
                                        GeyserBot.LOGGER.warn("Failed to create tag: " + e.getMessage());
                                        continue;
                                    }
                                    break;

                                case "text-raw":
                                    try {
                                        TAGS.add(new RawTag(tagName, content.toString().trim(), tagData.get("aliases"), buttons));
                                    } catch (IllegalArgumentException e) {
                                        GeyserBot.LOGGER.warn("Failed to create tag: " + e.getMessage());
                                        continue;
                                    }
                                    break;

                                case "issue-only":
                                    if (tagData.containsKey("aliases")) {
                                        GeyserBot.LOGGER.warn("Tag '" + tagName + "' has aliases listed but is of type 'issue-only'. Ignoring aliases.");
                                    }

                                    if (tagData.containsKey("image")) {
                                        GeyserBot.LOGGER.warn("Tag '" + tagName + "' has image listed but is of type 'issue-only'. Ignoring image.");
                                    }

                                    if (issueTriggers == null) {
                                        GeyserBot.LOGGER.warn("Tag '" + tagName + "' has no issues listed but is of type 'issue-only'.");
                                    }
                                    break;

                                default:
                                    GeyserBot.LOGGER.warn("Invalid tag type '" + tagData.get("type") + "' for tag '" + tagName + "'! Ignoring tag.");
                                    continue;
                            }

                            if (issueTriggers != null) {
                                // allow any tag with issues listed to be an issue response
                                for (String issue : issueTriggers) {
                                    ISSUE_RESPONSES.put(issue.trim(), content.toString().trim());
                                }
                            }

                            if (selfHelpTrigger != null) {
                                for (String help : selfHelpTrigger) {
                                    SELF_HELP.put(help.trim(), content.toString().trim());
                                }
                            }
                        }
                    }
                } catch (IOException | URISyntaxException e) {
                    GeyserBot.LOGGER.error("Failed to load tag subfolders", e);
                }
            }
        } catch (IOException | URISyntaxException e) {
            GeyserBot.LOGGER.error("Failed to load tags folder", e);
        }

        tagsLoaded = true;
    }
}
