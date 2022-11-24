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
import org.geysermc.discordbot.util.BotColors;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;
import pw.chew.chewbotcca.util.RestClient.RestResponse;

import java.util.Collections;
import java.util.UUID;

public class FloodgateUuidCommand extends SlashCommand {

    public FloodgateUuidCommand() {
        this.name = "uuid";
        this.help = "Get the floodgate uuid from a bedrock username.";
        this.arguments = "<bedrock-username>";
        this.guildOnly = false;
        this.cooldown = 60;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "bedrock-username", "username to grab floodgate uuid from").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // get bedrock username, replace char in case they include Floodgate prefix.
        String username = event.optString("bedrock-username", "").replace(".", "");
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Floodgate Player UUID");

        RestResponse<JSONObject> restResponse =
                RestClient.getJsonObject("https://api.geysermc.org/v2/xbox/xuid/" + username);
        int serverCode = restResponse.statusCode();
        JSONObject response = restResponse.body();

        // Check what code the server returns and send the corresponding embed to author.
        switch (serverCode) {
            case 200 -> {
                UUID floodgateUUID = new UUID(0, response.getLong("xuid"));
                builder.addField("Bedrock Player Name", username, false);
                builder.addField("Floodgate UUID", String.valueOf(floodgateUUID), false);
                builder.addField("XUID", String.valueOf(response.getLong("xuid")), false);
                builder.setColor(BotColors.SUCCESS.getColor());
            }
            case 400 -> {
                builder.addField("Global API", "Error " + serverCode, false);
                builder.addField("Bedrock Player Name", username, false);
                builder.addField("Reason", " The Bedrock player name is invalid (empty or longer than 16 chars)", false);
                builder.setColor(BotColors.FAILURE.getColor());
            }
            case 503 -> {
                builder.addField("Global API", "Error " + serverCode, false);
                builder.addField("Bedrock Player Name", username, false);
                builder.addField("Reason", " The requested account was not cached and we where not able to call the Xbox Live API", false);
                builder.addField("Fix", "You have to join a Floodgate server so the Bedrock account will get added to the GlobalAPI cache.", false);
                builder.setColor(BotColors.FAILURE.getColor());
            }
            default -> {
                builder.addField("Global API status", "Server responded with a " + serverCode, false);
                builder.setColor(BotColors.FAILURE.getColor());
            }
        }

        event.replyEmbeds(builder.build()).queue();
    }
}
