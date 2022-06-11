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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.geysermc.discordbot.util.BotColors;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProvidersTest {
    private final ProviderCommand command = new ProviderCommand();

    @Test
    public void testInvalidProviders() {
        assertEquals(0, command.potentialProviders("ifgjnerifejefwifoewjrgeri").size());
        assertEquals(0, command.potentialProviders("wenfiughenriougwhnregiureogn").size());
    }

    @Test
    public void testSimilarProviders() {
        assertEquals("MCProHosting", command.potentialProviders("mcrory").get(0).name());
        assertEquals("Google Cloud", command.potentialProviders("Google").get(0).name());
    }

    @Test
    public void testProviderEmbed() {
        ProviderCommand.Provider provider = command.potentialProviders("Google Cloud").get(0);
        MessageEmbed embed = command.sendProviderEmbed(provider);

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(provider.name(), provider.url());
        embedBuilder.setColor(BotColors.SUCCESS.getColor());
        embedBuilder.addField("Category", provider.getCategory(), false);
        embedBuilder.addField("Instructions", provider.instructions(), false);

        assertEquals(embedBuilder.build(), embed);
    }

    @Test
    public void testProviderCategories() {
        List<ProviderCommand.Provider> providers = command.getProviders();

        List<String> validCategories = Arrays.asList("Built-in Geyser", "Support for Geyser", "Does not support Geyser");
        for (ProviderCommand.Provider provider : providers) {
            assertTrue(validCategories.contains(provider.getCategory()));
        }
    }
}
