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
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.geysermc.discordbot.util.BotColors;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

public class SupportedVersionsCommand extends SlashCommand {
    public SupportedVersionsCommand() {
        this.name = "supportedversions";
        this.help = "Shows what Minecraft versions Geyser supports";
        this.aliases = new String[]{ "versions", "supportedversions" };
        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        InteractionHook interactionHook = event.deferReply().complete();
        interactionHook.editOriginalEmbeds(handle()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().replyEmbeds(handle()).queue();
    }

    protected MessageEmbed handle() {
        JSONObject result = RestClient.get("https://raw.githubusercontent.com/GeyserMC/GeyserWebsite/master/src/data/versions.json").asJSONObject();

        String javaVersion = result.getJSONObject("java").getString("supported");
        String bedrockVersion = result.getJSONObject("bedrock").getString("supported");

        return new EmbedBuilder()
                .setTitle(":geyser: Geyser Supported Versions")
                .setDescription("Currently, Geyser supports Minecraft Bedrock " + bedrockVersion + " and Minecraft Java " + javaVersion + ".")
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}