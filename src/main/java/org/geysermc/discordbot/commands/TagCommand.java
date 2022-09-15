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

package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.tags.SlashTag;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.BotColors;

import java.util.Collections;

public class TagCommand extends SlashCommand {

    public TagCommand() {
        this.name = "tag";
        this.help = "Fetch a tag";
        this.guildOnly = false;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "name", "The tag to get (Supports aliases)", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String tagName = event.getOption("name").getAsString();
        SlashTag tag = null;

        for (SlashTag slashTag : TagsManager.getEmbedTags()) {
            if (slashTag.getName().equalsIgnoreCase(tagName)) {
                tag = slashTag;
                break;
            }

            if (slashTag.getAliases() != null && !slashTag.getAliases().isEmpty()) {
                for (String alias : slashTag.getAliases().split(",")) {
                    if (alias.equalsIgnoreCase(tagName)) {
                        tag = slashTag;
                        break;
                    }
                }
            }
        }

        if (tag != null) {
            tag.replyWithTag(event);
        } else {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(BotColors.FAILURE.getColor())
                    .setTitle("Invalid tag")
                    .setDescription("Missing requested tag")
                    .build()).queue();
        }


    }
}
