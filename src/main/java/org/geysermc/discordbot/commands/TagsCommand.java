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

package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.util.PropertiesManager;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TagsCommand extends SlashCommand {
    public TagsCommand() {
        this.name = "tags";
        this.arguments = "[search]";
        this.help = "List all the known (non-alias) tags";
        this.guildOnly = false;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "search", "The term you want to search for")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String search = "";
        if (event.getOption("search") != null) {
            search = event.getOption("search").getAsString();
        }

        event.replyEmbeds(handle(search)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Check they specified an ip
        String search = "";
        if (!args.get(0).isEmpty()) {
            search = args.get(0);
        }

        event.getMessage().reply(handle(search)).queue();
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
            embed.setColor(Color.red);
            embed.setTitle("No tags found");
            embed.setDescription("No tags were found for your search.");
            embed.setFooter("Use `" + PropertiesManager.getPrefix() + "tag aliases <name>` to see all the aliases for a certain tag");
        } else {
            embed.setColor(PropertiesManager.getDefaultColor());
            embed.setTitle("Tags (" + tagNames.size() + ")");
            embed.setDescription("`" + String.join("`, `", tagNames) + "`");
            embed.setFooter("Use `" + PropertiesManager.getPrefix() + "tag <name>` to show a tag");
        }

        return embed.build();
    }
}
