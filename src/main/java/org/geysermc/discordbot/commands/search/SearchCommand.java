/*
 * Copyright (c) 2020-2024 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.commands.search;

import com.algolia.search.models.indexing.Query;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.DocSearchResult;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.PageHelper;
import org.geysermc.discordbot.util.PropertiesManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * A command to search the Geyser wiki for a query.
 */
public class SearchCommand extends SlashCommand {
    /**
     * The attributes to retrieve from the Algolia search.
     */
    private static final List<String> ATTRIBUTES = Arrays.asList("hierarchy.lvl0", "hierarchy.lvl1", "hierarchy.lvl2",
            "hierarchy.lvl3", "hierarchy.lvl4", "hierarchy.lvl5", "hierarchy.lvl6", "content", "type", "url");

    /**
     * The facets to filter the Algolia search by.
     */
    private static final List<List<String>> FACETS = Arrays.asList(Arrays.asList("language:en"),
            Arrays.asList("docusaurus_tag:default", "docusaurus_tag:docs-default-current"));

    /**
     * The tag with which to surround exact matches.
     */
    private static final String HIGHLIGHT_TAG = "***";

    /**
     * The maximum number of results to return from Algolia.
     */
    private static final int MAX_RESULTS = 10;

    /**
     * The constructor for the SearchCommand.
     */
    public SearchCommand() {
        this.name = "search";
        this.arguments = "<query>";
        this.help = "Search the Geyser wiki for a query";
        this.guildOnly = false;

        this.options = Arrays.asList(new OptionData(OptionType.STRING, "query", "The search query", true));
    }

    /**
     * Executes the command for a SlashCommandEvent.
     *
     * @param event The SlashCommandEvent.
     */
    @Override
    protected void execute(SlashCommandEvent event) {
        String query = event.optString("query", "");

        if (query.isEmpty()) {
            MessageHelper.errorResponse(event, "Invalid usage",
                    "Missing query to search. `" + event.getName() + " <query>`");
            return;
        }

        getEmbedsFuture(query).whenComplete((embeds, throwable) -> {
            if (throwable != null) {
                MessageHelper.errorResponse(event, "Search Error",
                        "An error occurred while searching for `" + query + "`");
                return;
            }

            new PageHelper(embeds, event, -1);
        });

    }

    /**
     * Executes the command for a CommandEvent.
     *
     * @param event The CommandEvent.
     */
    @Override
    protected void execute(CommandEvent event) {
        String query = event.getArgs();

        if (query.isEmpty()) {
            MessageHelper.errorResponse(event, "Invalid usage",
                    "Missing query to search. `" + event.getPrefix() + name + " <query>`");
            return;
        }

        getEmbedsFuture(query).whenComplete((embeds, throwable) -> {
            if (throwable != null) {
                MessageHelper.errorResponse(event, "Search Error",
                        "An error occurred while searching for `" + query + "`");
                return;
            }

            new PageHelper(embeds, event, -1);
        });
    }

    /**
     * Gets a CompletableFuture of a list of MessageEmbeds for a query.
     *
     * @param query The query to search for.
     * @return A CompletableFuture of a list of MessageEmbeds.
     */
    private CompletableFuture<List<MessageEmbed>> getEmbedsFuture(String query) {
        CompletableFuture<List<MessageEmbed>> future = new CompletableFuture<>();

        try {
            GeyserBot.getAlgolia().searchAsync(new Query(query)
                    .setHitsPerPage(MAX_RESULTS)
                    .setHighlightPreTag(HIGHLIGHT_TAG)
                    .setHighlightPostTag(HIGHLIGHT_TAG)
                    .setAttributesToSnippet(ATTRIBUTES)
                    .setAttributesToRetrieve(ATTRIBUTES)
                    .setFacetFilters(FACETS))
                    .whenComplete((results, throwable) -> {
                        if (throwable != null) {
                            GeyserBot.LOGGER.error("An error occurred while searching for `" + query + "`", throwable);
                            future.completeExceptionally(throwable);
                            return;
                        }

                        List<MessageEmbed> embeds = new ArrayList<>();

                        for (int i = 0; i < results.getHits().size(); i++) {
                            DocSearchResult result = results.getHits().get(i);

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setUrl(result.getUrl())
                                    .setTitle("Search Result", result.getUrl())
                                    .setFooter("Page " + (i + 1) + " of " + results.getHits().size() + " | Query: " + query)
                                    .setColor(BotColors.SUCCESS.getColor());

                            DocSearchResult.SnippetResult sr = result.get_snippetResult();
                            if (sr != null && sr.getContent() != null && sr.getContent().getMatchLevel().equals("full")) {
                                embed.addField("Match:", getMatchFieldBody(sr), false);
                            }

                            embed.addField("", getSeeAllFieldBody(query, results.getNbHits()), false);

                            int remainingLength = Math.min(MessageEmbed.EMBED_MAX_LENGTH_BOT - embed.length(), MessageEmbed.DESCRIPTION_MAX_LENGTH);
                            embed.setDescription(getDescriptionFieldBody(result, query, remainingLength));

                            embeds.add(embed.build());
                        }

                        if (embeds.isEmpty()) {
                            embeds.add(new EmbedBuilder()
                                    .setColor(BotColors.NEUTRAL.getColor())
                                    .setTitle("No results found")
                                    .setDescription("No results were found for query: `" + query + "`.")
                                    .build());
                        }

                        future.complete(embeds);
                    });
        } catch (Exception e) {
            GeyserBot.LOGGER.error("An error occurred while searching for `" + query + "`", e);
            future.completeExceptionally(e);
            return future;
        }

        return future;
    }

    /**
     * Gets the match field body for a snippet.
     *
     * @param snippet The snippet to get the match field body for.
     * @return The match field body.
     */
    private String getMatchFieldBody(DocSearchResult.SnippetResult snippet) {
        String unescapedSnippet = unescapeHtml(snippet.getContent().getValue());
        return ">>> " + unescapedSnippet.replace("\r\n", " ").replace("\n", " ");
    }

    /**
     * Gets the see all field body for a query.
     *
     * @param query The query to get the see all field body for.
     * @param hits  The number of hits for the query.
     * @return The see all field body.
     */
    private String getSeeAllFieldBody(String query, long hits) {
        return "[See all " + hits + " results](" + PropertiesManager.getAlgoliaSiteSearchUrl() + URLEncoder.encode(query, StandardCharsets.UTF_8) + ")";
    }

    /**
     * Gets the description field body for a result.
     *
     * @param result The result to get the description field body for.
     * @param query  The query to search for.
     * @param max    The maximum length of the description.
     * @return The description field body.
     */
    private String getDescriptionFieldBody(DocSearchResult result, String query, int max) {
        String header = getHierarchyChain(result.getHierarchy());
        
        DocSearchResult.HighlightResult hr = result.get_highlightResult();
        if (hr != null && hr.getContent() != null && hr.getContent().getValue() != null) {
            String description = "";

            List<String> lines = Arrays.asList(result.get_highlightResult().getContent().getValue().split("\n"))
                    .stream().distinct().collect(Collectors.toList());
            SortedSet<Integer> includedLines = new TreeSet<>();

            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).toLowerCase().contains(query.toLowerCase())) {
                    includedLines.add(i);

                    for (int j = i - 1; j >= Math.max(0, i - 4); j--) {
                        includedLines.add(j);
                    }

                    for (int j = i + 1; j <= Math.min(lines.size() - 1, i + 4); j++) {
                        includedLines.add(j);
                    }
                }
            }

            if (includedLines.isEmpty()) {
                for (int i = 0; i < Math.min(lines.size(), 9); i++) {
                    includedLines.add(i);
                }
            }

            int lastLine = -1;
            for (int i : includedLines) {
                if (lastLine != -1 && i - lastLine > 1) {
                    description += "**\u2022\u2022\u2022**\n";
                }

                description += "> " + lines.get(i);

                if (i != includedLines.last()) {
                    description += "\n> \n";
                } else {
                    break;
                }

                lastLine = i;
            }

            description = header + "\n**Excerpt:**\n" + description;

            if (description.length() > max) {
                description = removeDanglingFormatMarks(description.substring(0, max - 3) + "...", HIGHLIGHT_TAG);
            }

            return unescapeHtml(description);
        } else if (result.get_snippetResult() != null && result.get_snippetResult().getHierarchy() != null) {
            return unescapeHtml(header);
        } else {
            return "";
        }
    }

    /**
     * Gets the hierarchy chain for a hierarchy as a bulleted list.
     *
     * @param hierarchy The hierarchy to get the chain for.
     * @return The hierarchy chain as a bulleted list.
     */
    private String getHierarchyChain(DocSearchResult.Hierarchy hierarchy) {
        List<String> levels = new ArrayList<>();

        if (hierarchy.getLvl0() != null) levels.add(hierarchy.getLvl0());
        if (hierarchy.getLvl1() != null) levels.add(hierarchy.getLvl1());
        if (hierarchy.getLvl2() != null) levels.add(hierarchy.getLvl2());
        if (hierarchy.getLvl3() != null) levels.add(hierarchy.getLvl3());
        if (hierarchy.getLvl4() != null) levels.add(hierarchy.getLvl4());
        if (hierarchy.getLvl5() != null) levels.add(hierarchy.getLvl5());
        if (hierarchy.getLvl6() != null) levels.add(hierarchy.getLvl6());

        if (levels.isEmpty()) {
            return "- **Untitled**";
        }

        StringBuilder tb = new StringBuilder();

        for (int i = 0; i < levels.size(); i++) {
            String level = levels.get(i);
            if (i > 0)
                tb.append("\n");
            tb.append(String.join("", Collections.nCopies(i * 2, " ")) + "- ");
            if (i == 0 || i == levels.size() - 1)
                tb.append("**");
            tb.append(level);
            if (i == 0 || i == levels.size() - 1)
                tb.append("**");
        }

        return tb.toString();
    }

    /**
     * Removes dangling format marks from a string.
     *
     * @param content The content to remove dangling format marks from.
     * @param mark    The format mark to remove.
     * @return The content with dangling format marks removed.
     */
    private String removeDanglingFormatMarks(String content, String mark) {
        int count = (content.length() - content.replace(mark, "").length()) / mark.length();

        if (count % 2 != 0) {
            int lastIndex = content.lastIndexOf(mark);
            if (lastIndex != -1) {
                return new StringBuilder()
                        .append(content, 0, lastIndex)
                        .append(content.substring(lastIndex + mark.length()))
                        .toString();
            }
        }

        return content;
    }

    /**
     * Unescapes HTML entities in a string.
     *
     * @param html The HTML to unescape.
     * @return The unescaped HTML.
     */
    private String unescapeHtml(String html) {
        return html.replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'");
    }
}
