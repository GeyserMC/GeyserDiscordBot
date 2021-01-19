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

package org.geysermc.discordbot.commands.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.geysermc.discordbot.util.PropertiesManager;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pw.chew.chewbotcca.util.RestClient;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WikiCommand extends Command {

    public WikiCommand() {
        this.name = "wiki";
        this.arguments = "<search>";
        this.help = "Search the Geyser wiki";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder();

        String query = event.getArgs();

        // Check to make sure we have a search term
        if (query.isEmpty()) {
            embed.setTitle("Invalid usage");
            embed.setDescription("Missing search term. `" + PropertiesManager.getPrefix() + name + " <search>`");
            embed.setColor(Color.red);
            event.reply(embed.build());
            return;
        }

        List<WikiResult> results = new ArrayList<>();
        try {
            results = doSearch(query);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        // Set the title and color for the embed
        embed.setTitle("Search for " + query, "https://github.com/GeyserMC/Geyser/search?q=" + query + "&type=Wikis");
        embed.setColor(Color.green);

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
                    return;
                }

                // Add the result as a field
                embed.addField(result.getTitle(), result.getUrl() + "\n" + result.getDescription(), true);
            }
        } else {
            // We found no results
            embed.setDescription("No results");
            embed.setColor(Color.red);
        }

        event.reply(embed.build());
    }

    /**
     * Search the wiki on GitHub
     * and parse + format the results
     *
     * @param query The search query
     * @return The list of provider objects with title, desc, updated and url
     */
    public List<WikiResult> doSearch(String query) throws UnsupportedEncodingException {
        // Fetch the search page
        String contents = RestClient.get("https://github.com/GeyserMC/Geyser/search?q=" + URLEncoder.encode(query, "UTF-8") + "&type=Wikis");

        // Make sure we got a response
        if (contents.equals("")) {
            return null;
        }

        // Load the page response into a cheerio object
        List<WikiResult> results = new ArrayList<>();

        // Configure parser for later
        TagNode tagNode = new HtmlCleaner().clean(contents);
        Document doc;
        try {
            doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        // Find the summary text and image (if there is one)
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node src;
        try {
            src = (Node) xPath.evaluate("/html/body/div[4]/div/main/div/div/div/div[3]/div/div[3]/div", doc, XPathConstants.NODE);
            NodeList nodeList = src.getChildNodes();
            src.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                NodeList children = child.getChildNodes();

                String title = children.item(0).getTextContent().trim();
                String desc = children.item(1).getNodeValue().replaceAll("<em>", "").replaceAll("</em>", "").trim();
                String updated = children.item(2).getTextContent().trim();
                String url = "https://github.com" + children.item(0).getAttributes().getNamedItem("a").toString();

                results.add(new WikiResult(title, desc, updated, url));
            }
        } catch (NullPointerException ignored) {
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }

        return results;
    }

    private static class WikiResult {
        private final String title, description, updated, url;

        public WikiResult(String title, String description, String updated, String url) {
            this.title = title;
            this.description = description;
            this.updated = updated;
            this.url = url;
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
