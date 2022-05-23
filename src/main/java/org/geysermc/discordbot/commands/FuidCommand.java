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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.HttpUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class FuidCommand extends SlashCommand {
    private static final String urlString = "https://api.geysermc.org/v2/xbox/xuid/";

    public FuidCommand() {
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
        String username = Objects.requireNonNull(event.getOption("bedrock-username")).getAsString();

        HttpUtils.asyncGet(urlString + username)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }

                    JsonObject response = result.getResponse();
                    JsonElement xuidElement = response.get("xuid");

                    if (xuidElement == null) {
                        event.replyEmbeds(handle(username, "User was not found")).queue();
                        return;
                    }

                    long xuid = xuidElement.getAsLong();
                    UUID floodgateUUID = getJavaUuid(xuid);
                    event.replyEmbeds(handle(username, String.valueOf(floodgateUUID))).queue();
                });
    }

    private MessageEmbed handle(String username, String fuuid) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Floodgate UUID")
                .addField("PlayerName", username, false)
                .addField("Floodgate UUID", String.valueOf(fuuid), false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor());
        if (fuuid.contains("User")){
            builder.setColor(BotColors.FAILURE.getColor());
        }

        return builder.build();
    }

    public static UUID getJavaUuid(long xuid) {
        return new UUID(0, xuid);
    }
}