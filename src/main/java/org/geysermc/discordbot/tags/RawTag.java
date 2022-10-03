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
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.geysermc.discordbot.listeners.SwearHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class RawTag extends Command {

    private final String content;
    private final List<Button> buttons;

    /**
     * Create a new RawTag
     *
     * @param name The name of the tag to be used when calling it
     * @param content The text content
     * @param aliases Any aliases that can be used to call it
     * @throws IllegalArgumentException If name or content is null or empty. It would result in a tag with no content.
     */
    @SuppressWarnings("ConstantConditions")
    public RawTag(@Nonnull String name, @Nonnull String content, @Nullable String aliases, @Nonnull List<Button> buttons) throws IllegalArgumentException {
        if (name == null || name.isEmpty() || content == null || content.isEmpty()) {
            throw new IllegalArgumentException("name or content may not be null or empty");
        }

        this.name = name;
        this.content = content;
        this.guildOnly = false;
        if (aliases != null && !aliases.isEmpty()) {
            this.aliases = Arrays.stream(aliases.split(",")).map(String::trim).toArray(String[]::new);
        }

        this.buttons = buttons;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        var reply = event.getMessage().reply(content);
        if (!buttons.isEmpty()) {
            reply = reply.setComponents(ActionRow.of(buttons));
        }
        reply.queue();
    }
}
