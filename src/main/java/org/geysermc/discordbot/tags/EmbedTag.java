/*
 * Copyright (c) 2020-2025 GeyserMC. http://geysermc.org
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
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.util.BotColors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmbedTag extends Command {

    private final String title;
    private final String description;
    private final Color color;
    private final String image;
    private final List<Button> buttons;

    /**
     * Create a new EmbedTag
     *
     * @param name The name of the tag to be used when calling it
     * @param title The title of the tag
     * @param description The text content
     * @param color The color of the tag
     * @param image The URL of the image to display
     * @param aliases Any aliases that can be used to call it
     * @throws IllegalArgumentException If the description is null or empty while the image is also null or empty. It would result in a tag with no content. Also throws if the name is null or empty.
     */
    @SuppressWarnings("ConstantConditions")
    public EmbedTag(@Nonnull String name, @Nullable String title, @Nullable String description, @Nullable String color, @Nullable String image, @Nullable String aliases, @Nonnull List<Button> buttons) throws IllegalArgumentException {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name may not be null or empty");
        }
        if ((description == null || description.isEmpty()) && (image == null || image.isEmpty())) {
            throw new IllegalArgumentException("description may not be null or empty while image is null or empty");
        }

        this.name = name;
        this.title = title;
        this.description = description;
        this.color = color == null || color.isEmpty() ? BotColors.SUCCESS.getColor() : BotColors.valueOf(color).getColor();
        this.image = image;
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

        List<ContainerChildComponent> components = new ArrayList<>();

        if (title != null && !title.isEmpty()) {
            components.add(TextDisplay.of("# " + title));
            components.add(Separator.createDivider(Separator.Spacing.LARGE));
        }

        components.add(TextDisplay.of(description));

        if (image != null && !image.isEmpty()) {
            components.add(MediaGallery.of(MediaGalleryItem.fromUrl(image)));
        }

        if (!buttons.isEmpty()) {
            components.add(Separator.createDivider(Separator.Spacing.SMALL));
            components.add(ActionRow.of(buttons));
        }

        event.getMessage()
                .replyComponents(Container.of(components)
                        .withAccentColor(color))
                .useComponentsV2()
                .queue();
    }
}
