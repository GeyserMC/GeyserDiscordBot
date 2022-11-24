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

package org.geysermc.discordbot.commands.search;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.PropertiesManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WikiCommand extends SlashCommand {

    public WikiCommand() {
        this.name = "wiki";
        this.arguments = "<search>";
        this.help = "Search the Geyser wiki";
        this.guildOnly = false;

        // OptionData for Slash command
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "search", "The query to search.").setRequired(true)
        );
    }

    // /wiki
    @Override
    protected void execute(SlashCommandEvent event) {
        // Get arguments
        String args = event.optString("search", "");
        MessageEmbed response = handle(args);
        if (response != null) event.replyEmbeds(response).queue();
    }

    // !wiki
    @Override
    protected void execute(CommandEvent event) {
        MessageEmbed response = handle(event.getArgs());
        if (response != null) event.getMessage().replyEmbeds(response).queue();
    }

    public MessageEmbed handle(String query) {
        EmbedBuilder embed = new EmbedBuilder();

        // Check to make sure we have a search term
        if (query.isEmpty()) {
            return MessageHelper.errorResponse(null, "Invalid usage", "Missing search term. `" + PropertiesManager.getPrefix() + name + " <search>`");
        }

        if (query.length() > 128) {
            return MessageHelper.errorResponse(null, "Query too long", "Search query is over the max allowed character count of 128 (" + query.length() + ")");
        }

        List<WikiResult> results;
        results = doSearch(query);

        String url;
        url = "https://github.com/GeyserMC/Geyser/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&type=Wikis";

        // Set the title and color for the embed
        embed.setTitle("Search for " + query, url);
        embed.setColor(BotColors.SUCCESS.getColor());

        if (results.size() >= 1) {
            // Replace the results with the identical title match
            for (WikiResult result : results) {
                if (result.getTitle().equalsIgnoreCase(query)) {
                    results = new ArrayList<>();
                    results.add(result);
                }
            }

            for (WikiResult result : results) {
                // Ignore pages starting with `_` which are usually meta pages
                if (result.getTitle().startsWith("_")) {
                    continue;
                }

                // Add the result as a field
                embed.addField(result.getTitle(), result.getUrl() + "\n" + result.getDescription(), false);
            }
        } else {
            // We found no results
            embed.setDescription("No results");
            embed.setColor(BotColors.FAILURE.getColor());
        }

        return embed.build();
    }

    /**
     * Search the wiki on GitHub
     * and parse + format the results
     *
     * @param query The search query
     * @return The list of provider objects with title, desc, updated and url
     */
    public List<WikiResult> doSearch(String query) {
        // Fetch the search page
        String contents = RestClient.simpleGetString("https://github.com/GeyserMC/Geyser/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&type=Wikis");

        // Make sure we got a response
        if (contents.equals("")) {
            return null;
        }

        // Load the page response into a cheerio object
        List<WikiResult> results = new ArrayList<>();

        // Parse the page content
        Document doc = Jsoup.parse(contents);

        // Get the result elements
        Elements resultElements = doc.select("#wiki_search_results > div:first-child > div");

        // Build a result for each element
        for (Element result : resultElements) {
            String title = result.children().get(0).text().trim();
            String desc = result.children().get(1).html().replaceAll("<em>", "**").replaceAll("</em>", "**").trim();
            String updated = result.children().get(2).text().trim();
            String url = "https://github.com" + result.children().get(0).getElementsByAttribute("href").get(0).attr("href");

            results.add(new WikiResult(title, desc, updated, url));
        }

        return results;
    }

    private static class WikiResult {
        private final String title, description, updated, url;

        public WikiResult(String title, String description, String updated, String url) {
            this.title = title;
            this.description = description;
            this.updated = updated;

            // Fix last character breaking urls
            String lastChar = url.substring(url.length() - 1);
            lastChar = URLEncoder.encode(lastChar, StandardCharsets.UTF_8);

            this.url = url.substring(0, url.length() - 1) + lastChar;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getUpdated() {
            return updated;
        }

        public String getUrl() {
            return url;
        }
    }
}
