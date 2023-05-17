/*
 * Copyright (c) 2020-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pw.chew.chewbotcca.util.RestClient;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SupportHandler extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        long channelID = Long.parseLong("1091474266643509408");
        if (!(event.getChannel().getIdLong() == channelID)) {
            return;
        }
        String discordMessage = event.getMessage().getContentRaw().toLowerCase();
        // A list of words to check.
        String[] keywordsToCheck = {"pls", "please", "help", "me", "need", "how", "install", "setup", "geyser", "floodgate", "what", "why"};
        // Check if the message contains a match.
        if (Arrays.stream(keywordsToCheck).noneMatch(p -> Pattern.compile("\\b" + p + "\\b").matcher(discordMessage).find())) {
            return;
        }
        // Joke Api
        RestClient.RestResponse<JSONObject> restResponse =
                RestClient.getJsonObject("https://v2.jokeapi.dev/joke/Programming,Miscellaneous,Pun,Spooky?blacklistFlags=nsfw,religious,political,racist,sexist,explicit");
        JSONObject serverResponse = restResponse.body();

        switch (serverResponse.getString("type")) {
            case "single" -> event.getChannel().sendMessage(event.getAuthor().getName() + ", " + serverResponse.getString("joke")).queue();
            case "twopart" -> {
                long Time0 = System.currentTimeMillis();
                long Time1;
                long runTime = 0;
                // Setup the joke
                event.getChannel().sendMessage(event.getAuthor().getName() + ", " + serverResponse.getString("setup")).queue();
                // Delay the delivery which makes it funnier.
                while (runTime < 1000 * 5) { // 1000 milliseconds or 1 second
                    Time1 = System.currentTimeMillis();
                    runTime = Time1 - Time0;
                }
                // Make the joke
                event.getChannel().sendMessage(serverResponse.getString("delivery")).delay(70L, TimeUnit.SECONDS).queue();
            }
        }

    }
}
