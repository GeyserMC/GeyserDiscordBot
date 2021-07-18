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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import pw.chew.chewbotcca.util.RestClient;

import java.time.Instant;

public class QueueCommand extends SlashCommand {

    public QueueCommand() {
        this.name = "queue";
        this.help = "Show stats about the current global API skin queue";
        this.aliases = new String[] {"skinqueue"};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Defer to wait for us to load a response and allows for files to be uploaded
        InteractionHook interactionHook = event.deferReply().complete();
        interactionHook.editOriginalEmbeds(handle()).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().replyEmbeds(handle()).queue();
    }

    protected MessageEmbed handle() {
        JSONObject stats = null;
        String errorMessage = "";
        try {
            stats = new JSONObject(RestClient.get("https://api.geysermc.org/v1/stats"));

            if (stats.has("error")) {
                errorMessage = stats.getString("error");
            }
        } catch (JSONException e) {
            errorMessage = e.getMessage();
        }

        // We have an error
        if (!errorMessage.trim().isEmpty()) {
            return MessageHelper.errorResponse(null, "Unable to fetch queue stats", "An error occured while trying to contact the status page: " + errorMessage);
        }

        // Calculate the queue time and generate a nice string for it
        Instant now = Instant.now();
        PrettyTime t = new PrettyTime(now);
        Instant queueTime = now.plusSeconds((long) stats.getJSONObject("upload_queue").getFloat("estimated_duration"));
        String queueTimeText = t.format(t.calculatePreciseDuration(queueTime));
        queueTimeText = queueTimeText.replace(" from now", "");

        return new EmbedBuilder()
                .setTitle("Current global api skin queue")
                .addField("Pre-upload queue",  String.format("%,d", stats.getJSONObject("pre_upload_queue").getInt("length")), true)
                .addField("Upload queue",  String.format("%,d", stats.getJSONObject("upload_queue").getInt("length")), true)
                .addField("Upload queue time", queueTimeText, true)
                .setFooter("https://api.geysermc.org/v1/stats")
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}
