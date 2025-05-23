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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import org.geysermc.discordbot.util.BotColors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SlashTag {
    private final String name;
    private final String title;
    private final String content;
    private final Color color;
    private final String image;
    private final String aliases;
    private final List<Button> buttons;
    private final int type;

    public SlashTag(@Nonnull String name, @Nullable String title, @Nullable String content, @Nullable String color, @Nullable String image, @Nullable String aliases, @Nonnull List<Button> buttons, int type) {
        //Type #0 is an EmbedTag, Type #1 is a RawTag
        this.type = type;

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name may not be null or empty");
        }

        if (type == 0 && (content == null || content.isEmpty()) && (image == null || image.isEmpty())) {
            throw new IllegalArgumentException("description may not be null or empty while image is null or empty");
        }

        if (type == 1 && (content == null || content.isEmpty())) {
            throw new IllegalArgumentException("content may not be null or empty on raw tag");
        }

        if (type != 0 && type != 1) {
            throw new IllegalArgumentException("invalid type range specified");
        }

        this.name = name;
        this.title = title;
        this.content = content;
        this.color = color == null || color.isEmpty() ? BotColors.SUCCESS.getColor() : BotColors.valueOf(color).getColor();
        this.image = image;
        this.aliases = aliases;
        this.buttons = buttons;
    }

    public void replyWithTag(SlashCommandEvent event) {
        if (type == 0) {

            List<ContainerChildComponent> components = new ArrayList<>();

            if (title != null && !title.isEmpty()) {
                components.add(TextDisplay.of("# " + title));
                components.add(Separator.createDivider(Separator.Spacing.LARGE));
            }

            components.add(TextDisplay.of(content));

            if (image != null && !image.isEmpty()) {
                components.add(MediaGallery.of(MediaGalleryItem.fromUrl(image)));
            }

            if (!buttons.isEmpty()) {
                components.add(Separator.createDivider(Separator.Spacing.SMALL));
                components.add(ActionRow.of(buttons));
            }

            event.replyComponents(Container.of(components)
                    .withAccentColor(color))
                .useComponentsV2()
                .queue();
        } else if (type == 1) {
            var reply = event.reply(content);
            if (!buttons.isEmpty()) {
                reply = reply.setComponents(ActionRow.of(buttons));
            }

            reply.queue();
        }
    }

    public String getName() {
        return name;
    }

    public String getAliases() {
        return aliases;
    }
}
