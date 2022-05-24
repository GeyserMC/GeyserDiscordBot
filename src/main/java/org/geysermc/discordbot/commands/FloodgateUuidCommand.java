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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.json.JSONException;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class FloodgateUuidCommand extends SlashCommand {

    public FloodgateUuidCommand() {
        this.name = "fuuid";
        this.help = "get floodgate uuid from player";
        this.arguments = "<bedrock-username>";
        this.guildOnly = false;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "bedrock-username", "username to grab floodgate uuid").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // get bedrock username, replace char in case they include Floodgate prefix.
        String username = Objects.requireNonNull(event.getOption("bedrock-username")).getAsString().replace(".", "");
        try {
            // get xuid as json object and convert xuid into Floodgate uuid
            JSONObject getXuid = new JSONObject(RestClient.get("https://api.geysermc.org/v2/xbox/xuid/" + username));

            if (getXuid.has("xuid")) {
                UUID floodgateUUID = new UUID(0, getXuid.getLong("xuid"));
                event.replyEmbeds(floodgateUUID(username, floodgateUUID, false)).queue();
            }

            if (getXuid.has("message")) {
                event.replyEmbeds(floodgateUUID(username, null, false)).queue();
            }
        } catch (JSONException e) {
            event.replyEmbeds(floodgateUUID(username, null, true)).queue();
            e.printStackTrace();
        }
    }

    private MessageEmbed floodgateUUID(String username, UUID uuid, boolean error) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Floodgate UUID");

        if (!error) {
            if (uuid != null) {
                builder.addField("PlayerName", username, false);
                builder.addField("Floodgate UUID", uuid.toString(), false);
                builder.setColor(BotColors.SUCCESS.getColor());
            } else {
                builder.addField("Error", "Could not find bedrock player: " + username, false);
                builder.setColor(BotColors.FAILURE.getColor());
            }
        } else {
            builder.addField("Error", "Unable to lookup uuid, FloodgateAPI currently unavailable", false);
            builder.setColor(BotColors.FAILURE.getColor());
        }

        builder.setTimestamp(Instant.now());
        return builder.build();
    }
}