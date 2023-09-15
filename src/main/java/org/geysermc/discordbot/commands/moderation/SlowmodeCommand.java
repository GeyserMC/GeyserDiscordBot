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

package org.geysermc.discordbot.commands.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.SlowmodeHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;

import java.time.Instant;
import java.util.Collections;

public class SlowmodeCommand extends SlashCommand {

    public SlowmodeCommand() {
        this.name = "slowmode";
        this.aliases = new String[] { "slow" };
        this.hidden = true;
        this.help = "Set a custom slowmode for a channel";

        this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };
        this.botPermissions = new Permission[] { Permission.MESSAGE_MANAGE };

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "args", "Set length or turn off slowmode", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String arg = event.getOption("args").getAsString();

        if (arg.isBlank()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid usage")
                    .setDescription("Please specify a time in the correct format `1h2m3s`.")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        event.replyEmbeds(handle(event.getMember(), event.getGuild(), event.getTextChannel(), arg)).queue();
    }

    private MessageEmbed handle(Member mod, Guild guild, TextChannel channel, String string) {
        MessageEmbed slowmodeEmbed;

        if (string.equals("off")) {
            for (Object listener : GeyserBot.getJDA().getEventManager().getRegisteredListeners()) {
                if (listener instanceof SlowmodeHandler) {
                    if (((SlowmodeHandler) listener).getChannelId() == channel.getIdLong()) {
                        GeyserBot.getJDA().getEventManager().unregister(listener);
                        break;
                    }
                }
            }

            GeyserBot.storageManager.setSlowModeChannel(channel, 0);

            slowmodeEmbed = new EmbedBuilder()
                    .setTitle("Slowmode")
                    .setDescription("Slowmode disabled for " + channel.getAsMention() + " by " + mod.getAsMention())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.SUCCESS.getColor())
                    .build();
        } else {
            // Get the time
            int delay = BotHelpers.parseTimeString(string);

            // Check if time is valid
            if (delay == 0) {
                return new EmbedBuilder()
                        .setTitle("Invalid usage")
                        .setDescription("Please specify a time in the correct format `1h2m3s`.")
                        .setColor(BotColors.FAILURE.getColor())
                        .build();
            }

            slowmodeEmbed = new EmbedBuilder()
                    .setTitle("Slowmode")
                    .setDescription("Slowmode updated for " + channel.getAsMention() + " set to `" + string + "` (" + delay + "s) by " + mod.getAsMention())
                    .setTimestamp(Instant.now())
                    .setColor(BotColors.SUCCESS.getColor())
                    .build();

            boolean found = false;
            for (Object listener : GeyserBot.getJDA().getEventManager().getRegisteredListeners()) {
                if (listener instanceof SlowmodeHandler) {
                    if (((SlowmodeHandler) listener).getChannelId() == channel.getIdLong()) {
                        ((SlowmodeHandler) listener).setSeconds(delay);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                GeyserBot.getJDA().addEventListener(new SlowmodeHandler(channel.getIdLong(), delay));
            }

            // Update the db
            GeyserBot.storageManager.setSlowModeChannel(channel, delay);
        }

        // Return the embed and send to the log
        ServerSettings.getLogChannel(guild).sendMessageEmbeds(slowmodeEmbed).queue();
        return slowmodeEmbed;
    }
}
