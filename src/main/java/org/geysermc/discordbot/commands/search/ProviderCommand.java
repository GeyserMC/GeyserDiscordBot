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
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.geysermc.discordbot.util.MessageHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProviderCommand extends SlashCommand {
    private List<Provider> cache = null;
    private long cacheTime = 0;

    public ProviderCommand() {
        this.name = "provider";
        this.arguments = "<provider>";
        this.help = "Search the Supported Providers page on the Geyser wiki to see if a provider is supported";
        this.guildOnly = false;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "provider", "The provider to look for", true)
                .setAutoComplete(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(handle(event.optString("provider", ""))).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String query = event.getArgs();

        // Check to make sure we have a provider
        if (query.isEmpty()) {
            MessageHelper.errorResponse(event, "Invalid usage", "Missing provider to check. `" + event.getPrefix() + name + " <provider>`");
            return;
        }

        event.getMessage().replyEmbeds(handle(query)).queue();
    }

    /**
     * Returns a list of potential providers based on a few factors:
     * <ul>
     *     <li>Exact match somewhere in the name</li>
     *     <li>The input is at least 20% similar to any specific provider name</li>
     * </ul>
     *
     * @param query The query to search for
     * @return A list of potential providers
     */
    List<Provider> potentialProviders(String query) {
        List<Provider> potential = new ArrayList<>();

        // Collect the providers
        List<Provider> providers = getProviders();

        // Search the providers by an exact starting match
        for (Provider provider : providers) {
            if (provider.name().toLowerCase().startsWith(query.toLowerCase())) {
                potential.add(provider);
            }
        }

        // Find the best similarity
        for (Provider provider : providers) {
            double similar = DicesCoefficient.diceCoefficientOptimized(query.toLowerCase(), provider.name().toLowerCase());
            if (similar > 0.2d) {
                potential.add(provider);
            }
        }

        // Send a message if we don't know what provider
        return potential;
    }

    public MessageEmbed handle(String query) {
        List<Provider> potential = potentialProviders(query);

        // Make sure the rating is over 20%
        if (potential.isEmpty()) {
            // Send a message if we dont know what provider
            return MessageHelper.errorResponse(null, "Unknown provider", "That provider is not on our GitHub so it is untested!");
        } else {
            return sendProviderEmbed(potential.get(0));
        }
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        // Get the query
        String query = event.getFocusedOption().getValue();

        // Get the providers
        List<Provider> providers = potentialProviders(query);

        event.replyChoices(providers.stream()
                .distinct()
                .map(provider -> new Command.Choice(provider.name(), provider.name()))
                .limit(25)
                .toArray(Command.Choice[]::new))
            .queue();
    }

    /**
     * Build an embed and return it based on the passed provider details
     *
     * @param provider The provider to use for the embed contents
     */
    MessageEmbed sendProviderEmbed(Provider provider) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(provider.name(), provider.url());
        embed.setColor(BotColors.SUCCESS.getColor());
        embed.addField("Category", provider.getCategory(), false);
        embed.addField("Instructions", provider.instructions(), false);

        return embed.build();
    }

    /**
     * Get the providers from the github page
     * and parse + format the results
     *
     * @return The list of {@link Provider} objects from GitHub
     */
    List<Provider> getProviders() {
        // If the cache time is less than an hour (3600 seconds), use the cache
        if (System.currentTimeMillis() - cacheTime < 3600000) {
            return cache;
        }

        // Fetch the search page
        JSONObject contents = RestClient.simpleGetJsonObject("https://raw.githubusercontent.com/GeyserMC/GeyserWiki/master/_data/providers.json");
        Map<String, Object> descriptionTemplates = contents.getJSONObject("description_templates").toMap();

        List<Provider> providers = new ArrayList<>();

        for (String category : contents.keySet()) {
            if (category.equals("description_templates")) continue;

            JSONArray categoryProviders = contents.getJSONArray(category);
            for (Object providerObj : categoryProviders) {
                JSONObject provider = (JSONObject) providerObj;

                String template = provider.has("description_template") ? descriptionTemplates.get(provider.getString("description_template")).toString() : "";
                String description = String.format("%s %s", template, provider.optString("description", "")).trim();

                providers.add(new Provider(
                    provider.getString("name"),
                    provider.getString("url"),
                    description,
                    category)
                );
            }
        }

        // Cache the provider and the time it was cached
        cache = providers;
        cacheTime = System.currentTimeMillis();

        // Return
        return providers;
    }

    /**
     * A class to represent a provider
     * @param name The name of the provider
     * @param url The url to this provider
     * @param instructions Geyser installation instructions
     * @param category The category of the provider
     */
    public record Provider(String name, String url, String instructions, String category) {
        public String getCategory() {
            return switch (category) {
                case "built_in" -> "Built-in Geyser";
                case "support" -> "Support for Geyser";
                case "no_support" -> "Does not support Geyser";
                default -> "Unknown";
            };
        }
    }
}
