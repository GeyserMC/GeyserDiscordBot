/*
 * Copyright (c) 2025 GeyserMC. http://geysermc.org
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.time.Instant;

public class RoryCommand extends SlashCommand {
    public RoryCommand() {
        this.name = "rory";
        this.help = "Shows a random rory image from rory.cat";
        this.aliases = new String[]{ "rory", "car" };
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        event.getHook().editOriginalEmbeds(handle()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().replyEmbeds(handle()).queue();
    }

    protected MessageEmbed handle() {
        JSONObject result = RestClient.get("https://rory.cat/purr").asJSONObject();

        if (!result.has("url") || !result.has("id")) {
            return MessageHelper.errorResponse(
                    null,
                    "Couldn't find a random Rory image!",
                    "Unable to fetch a valid image url from rory.cat!"
            );
        }

        return new EmbedBuilder()
                .setTitle(":cat: Here’s A Random Rory Image:")
                .setImage(result.getString("url"))
                .setFooter("Cat ID: " + result.getInt("id") + " • Powered by rory.cat")
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}