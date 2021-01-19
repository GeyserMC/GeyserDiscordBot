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

package org.geysermc.discordbot.tags;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.geysermc.discordbot.util.PropertiesManager;

import java.awt.*;
import java.util.Arrays;

public class TagAliasCommand extends Command {

    public TagAliasCommand() {
        this.name = "alias";
        this.aliases = new String[] {"aliases"};
        this.guildOnly = false;

    }

    @Override
    protected void execute(CommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder();

        String[] args = event.getArgs().split(" ");

        if (args.length == 0) {
            embed.setTitle("Invalid usage");
            embed.setDescription("Missing tag name. `" + PropertiesManager .getPrefix() + "tag alias <name>`");
            embed.setColor(Color.red);

            event.reply(embed.build());
            return;
        }

        Command foundTag = null;
        for (Command tag : TagsManager.getTags()) {
            if (tag.getName().equals(args[0]) || Arrays.stream(tag.getAliases()).anyMatch(s -> s.equals(args[0]))) {
                foundTag = tag;
                break;
            }
        }

        if (foundTag == null) {
            embed.setTitle("Missing tag");
            embed.setDescription("No tag with the name `" + args[0] + "`, do `" + PropertiesManager.getPrefix() + "tags` for the full list.");
            embed.setColor(Color.red);
            event.reply(embed.build());
            return;
        }

        if (foundTag.getAliases().length > 0) {
            embed.setTitle("Aliases for " + foundTag.getName() + " (" + foundTag.getAliases().length + ")");
            embed.setDescription("`" + String.join("`, `", foundTag.getAliases()) + "`");
            embed.setFooter("Use `" + PropertiesManager.getPrefix() + "tag <name>` to show a tag");
            embed.setColor(Color.green);
        } else {
            embed.setTitle("No aliases for " + foundTag.getName());
            embed.setDescription("No aliases where found for the tag with the name `" + foundTag.getName() + "`.");
            embed.setColor(Color.red);
        }

        event.reply(embed.build());
    }
}
