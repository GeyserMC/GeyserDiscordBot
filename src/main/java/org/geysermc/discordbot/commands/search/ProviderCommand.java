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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.commands.filter.FilteredSlashCommand;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.geysermc.discordbot.util.MessageHelper;
import pw.chew.chewbotcca.util.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProviderCommand extends FilteredSlashCommand {
    private static final String PROVIDERS_URL = "https://raw.githubusercontent.com/GeyserMC/GeyserWebsite/master/src/data/providers.ts";
    private static final String NOT_LISTED = "not listed";
    private static final Pattern NAME_PATTERN = Pattern.compile("name:\\s*(['\"])(.*?)\\1");

    private List<String> cache = null;
    private long cacheTime = 0;

    public ProviderCommand() {
        this.name = "provider";
        this.arguments = "<provider>";
        this.help = "Search the hosting providers in the Geyser setup guide to find configuration instructions.";
        this.guildOnly = false;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "provider", "The provider to look for", true)
                .setAutoComplete(true)
        );
    }

    @Override
    protected void executeFiltered(SlashCommandEvent event) {
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
     * @return A list of potential provider names
     */
    List<String> potentialProviders(String query) {
        List<String> potential = new ArrayList<>();

        // Collect the providers
        List<String> providers = getProviders();

        // Search the providers by an exact starting match
        for (String provider : providers) {
            if (provider.toLowerCase().startsWith(query.toLowerCase())) {
                potential.add(provider);
            }
        }

        // Find the best similarity
        for (String provider : providers) {
            double similar = DicesCoefficient.diceCoefficientOptimized(query.toLowerCase(), provider.toLowerCase());
            if (similar > 0.2d) {
                potential.add(provider);
            }
        }

        return potential;
    }

    public MessageEmbed handle(String query) {
        List<String> potential = potentialProviders(query);

        EmbedBuilder embed = new EmbedBuilder();
        if (potential.isEmpty()) {
            // Send the generic setup guide if we don't know the provider
            embed.setTitle("Unknown provider");
            embed.setColor(BotColors.FAILURE.getColor());
            embed.setDescription("That provider is not in our list, so it is untested! Try following the [setup guide](" + setupUrl(NOT_LISTED) + ").");
        } else {
            String provider = potential.get(0);
            embed.setTitle(provider);
            embed.setColor(BotColors.SUCCESS.getColor());
            embed.setDescription("[Setup guide for " + provider + "](" + setupUrl(provider) + ")");
        }

        return embed.build();
    }

    @Override
    public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
        // Get the query
        String query = event.getFocusedOption().getValue();

        event.replyChoices(potentialProviders(query).stream()
                .distinct()
                .map(provider -> new Command.Choice(provider, provider))
                .limit(25)
                .toArray(Command.Choice[]::new))
            .queue();
    }

    /**
     * Get the provider names from the website's providers.ts source file
     *
     * @return The list of provider names from GitHub
     */
    List<String> getProviders() {
        // If the cache time is less than an hour (3600 seconds), use the cache
        if (System.currentTimeMillis() - cacheTime < 3600000) {
            return cache;
        }

        List<String> providers = parseProviders(RestClient.get(PROVIDERS_URL).asString());

        // Cache the providers and the time they were cached
        cache = providers;
        cacheTime = System.currentTimeMillis();

        return providers;
    }

    static List<String> parseProviders(String source) {
        List<String> providers = new ArrayList<>();

        int dataStart = source.indexOf("providersData");
        if (dataStart == -1) {
            return providers;
        }

        Matcher matcher = NAME_PATTERN.matcher(source.substring(dataStart));
        while (matcher.find()) {
            providers.add(matcher.group(2));
        }

        return providers;
    }

    static String setupUrl(String provider) {
        return "https://geysermc.org/wiki/geyser/setup?host=provider&provider=" + URLEncoder.encode(provider, StandardCharsets.UTF_8);
    }
}
