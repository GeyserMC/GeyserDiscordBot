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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.PropertiesManager;
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProviderCommand extends SlashCommand {

    public ProviderCommand() {
        this.name = "provider";
        this.arguments = "<provider>";
        this.help = "Search the Supported Providers page on the Geyser wiki to see if a provider is supported";
        this.guildOnly = false;

        // TODO: Provide Choices of providers
        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "provider", "The provider to look for.").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(handle(event.getOption("provider").getAsString())).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String query = event.getArgs();

        // Check to make sure we have a provider
        if (query.isEmpty()) {
            MessageHelper.errorResponse(event, "Invalid usage", "Missing provider to check. `" + event.getPrefix() + name + " <provider>`");
            return;
        }

        event.getMessage().reply(handle(query)).queue();
    }

    public MessageEmbed handle(String query) {
        // Collect the providers
        List<Provider> providers = getProviders();

        // Search the providers by an exact starting match
        for (Provider provider : providers) {
            if (provider.getName().toLowerCase().startsWith(query.toLowerCase())) {
                return sendProviderEmbed(provider);
            }
        }

        // If we haven't found a provider yet use an
        // algorithm to check for the most similar provider
        Provider bestMatch = null;
        double bestSimilarity = 0d;

        // Find the best similarity
        for (Provider provider : providers) {
            double similar = DicesCoefficient.diceCoefficientOptimized(query.toLowerCase(), provider.getName().toLowerCase());
            if (similar > bestSimilarity) {
                bestMatch = provider;
                bestSimilarity = similar;
            }
        }

        // Make sure the rating is over 20%
        if (bestSimilarity >= 0.2d) {
            return sendProviderEmbed(bestMatch);
        }

        // Send a message if we dont know what provider
        return MessageHelper.errorResponse(null, "Unknown provider", "That provider is not on our GitHub so it is untested!");
    }

    /**
     * Build an embed and return it based on the passed provider details
     *
     * @param provider The provider to use for the embed contents
     */
    private MessageEmbed sendProviderEmbed(Provider provider) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(provider.getName(), provider.getUrl());
        embed.setColor(PropertiesManager.getDefaultColor());
        embed.addField("Category", provider.getCategory(), false);
        embed.addField("Instructions", provider.getInstructions(), false);

        return embed.build();
    }

    /**
     * Get the providers from the github page
     * and parse + format the results
     *
     * @return The list of {@link Provider} objects from GitHub
     */
    private List<Provider> getProviders() {
        // Fetch the search page
        String contents = RestClient.get("https://raw.githubusercontent.com/wiki/GeyserMC/Geyser/Supported-Hosting-Providers.md").trim();

        // Make sure we got a response
        if (contents.isEmpty()) {
            return new ArrayList<>();
        }

        String category = "";
        List<Provider> providers = new ArrayList<>();

        Pattern providerPattern = Pattern.compile("\\[([\\w \\\\.-]+)\\]\\(([\\w\\\\.\\\\/:-]+)\\)");
        Pattern instructionsPattern = Pattern.compile(" \\((.+)\\)");

        for (String line : contents.split("\n")) {
            line = line.trim();

            // Check for a header line
            if (line.startsWith("## ")) {
                category = line.replace("## ", "");
                continue;
            }

            // Check for a provider line
            if (line.startsWith("* ")) {
                // Get the provider name and url
                Matcher providerMatcher = providerPattern.matcher(line);
                if (!providerMatcher.find()) {
                    continue;
                }

                // Get the inline instructions
                Matcher instructionsMatcher = instructionsPattern.matcher(line);
                String instructions = "None";
                if (instructionsMatcher.find()) {
                    instructions = instructionsMatcher.group(1);
                }

                providers.add(new Provider(providerMatcher.group(1), providerMatcher.group(2), instructions, category));
            } else if (line.startsWith("- ")) { // Check for indented instructions
                Provider provider = providers.get(providers.size() - 1);
                if ("None".equals(provider.instructions)) {
                    provider.instructions = line.replace("- ", "").trim();
                } else {
                    provider.instructions = (provider.instructions + '\n' + line.replace("- ", "")).trim();
                }
            }
        }

        return providers;
    }

    private static class Provider {
        private final String name;
        private final String url;
        private String instructions;
        private final String category;

        public Provider(String name, String url, String instructions, String category) {
            this.name = name;
            this.url = url;
            this.instructions = instructions;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getInstructions() {
            return instructions;
        }

        public String getCategory() {
            return category;
        }
    }
}
