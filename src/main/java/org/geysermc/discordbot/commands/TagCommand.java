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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.tags.SlashTag;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.BotColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagCommand extends SlashCommand {

    public TagCommand() {
        this.name = "tag";
        this.help = "Tags";
        this.hidden = true;
        this.guildOnly = true;
        this.children = new SlashCommand[]{
                new FetchTag(),
                new TagsList()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // unused
    }

    public static class FetchTag extends SlashCommand {
        public FetchTag() {
            this.name = "fetch";
            this.help = "Fetch a tag";
            this.arguments = "<name>";
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

    public static class TagsList extends SlashCommand {
        public TagsList() {
            this.name = "list";
            this.arguments = "[search]";
            this.help = "List all the known (non-alias) tags";
            this.guildOnly = false;

            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "search", "The term you want to search for")
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String search = event.optString("search", "");

            event.replyEmbeds(handle(search)).queue();
        }

        protected MessageEmbed handle(String search) {
            EmbedBuilder embed = new EmbedBuilder();

            // Get tag names based on search
            List<String> tagNames = new ArrayList<>();
            for (Command tag : TagsManager.getTags()) {
                if (!tag.getName().equals("alias") && tag.getName().contains(search)) {
                    tagNames.add(tag.getName());
                }
            }

            // Sort the tag names
            Collections.sort(tagNames);

            if (tagNames.isEmpty()) {
                embed.setColor(BotColors.FAILURE.getColor());
                embed.setTitle("No tags found");
                embed.setDescription("No tags were found for your search.");
                embed.setFooter("Use `/tag list aliases <name>` to see all the aliases for a certain tag");
            } else {
                embed.setColor(BotColors.SUCCESS.getColor());
                embed.setTitle("Tags (" + tagNames.size() + ")");
                embed.setDescription("`" + String.join("`, `", tagNames) + "`");
                embed.setFooter("Use `/tag fetch <name>` to show a tag");
            }

            return embed.build();
        }
    }
}
