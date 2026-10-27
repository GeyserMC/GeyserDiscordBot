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
import org.geysermc.discordbot.util.BotColors;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProvidersTest {
    private final ProviderCommand command = new ProviderCommand();

    @Test
    public void testParseProviders() {
        String source = """
            const ports = { assign: { obtain: translate({ id: 'providers.ports.assign', message: "..." }) } }

            export const providersData: HostingProvider[] = [
                {
                    name: 'Akliz',
                    url: 'https://www.akliz.net/',
                    config: { udp: ports.assign, confirmed: false }
                },
                {
                    name: "Apex Hosting",
                    url: null
                }
            ]
            """;

        assertEquals(List.of("Akliz", "Apex Hosting"), ProviderCommand.parseProviders(source));
    }

    @Test
    public void testSetupUrl() {
        assertEquals("https://geysermc.org/wiki/geyser/setup?host=provider&provider=Apex+Hosting", ProviderCommand.setupUrl("Apex Hosting"));
    }

    @Test
    public void testInvalidProviders() {
        assertEquals(0, command.potentialProviders("qjzxqjzxqjzx").size());
        assertEquals(0, command.potentialProviders("wenfiughenriougwhnregiureogn").size());
    }

    @Test
    public void testSimilarProviders() {
        assertEquals("EnviroMC", command.potentialProviders("mcrory").get(0));
        assertEquals("CloudNord", command.potentialProviders("cloud").get(0));
    }

    @Test
    public void testProviderEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("CloudNord");
        embedBuilder.setColor(BotColors.SUCCESS.getColor());
        embedBuilder.setDescription("[Setup guide for CloudNord](https://geysermc.org/wiki/geyser/setup?host=provider&provider=CloudNord)");

        assertEquals(embedBuilder.build(), command.handle("cloud"));
    }

    @Test
    public void testUnknownProviderEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Unknown provider");
        embedBuilder.setColor(BotColors.FAILURE.getColor());
        embedBuilder.setDescription("That provider is not in our list, so it is untested! Try following the [setup guide](https://geysermc.org/wiki/geyser/setup?host=provider&provider=not+listed).");

        assertEquals(embedBuilder.build(), command.handle("qjzxqjzxqjzx"));
    }
}
