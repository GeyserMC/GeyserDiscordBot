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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.LevelInfo;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.PropertiesManager;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LevelCommand extends Command {

    public LevelCommand() {
        this.name = "level";
        this.arguments = "[member]";
        this.help = "Show the level for a member";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        Member member;
        if (args.size() == 0 || args.get(0).isEmpty()) {
            member = event.getMember();
        } else {
            member = BotHelpers.getMember(event.getGuild(), args.remove(0));
        }

        // Check user is valid
        if (member == null) {
            event.getMessage().reply(new EmbedBuilder()
                    .setTitle("Invalid user")
                    .setDescription("The user ID specified doesn't link with any valid user in this server.")
                    .setColor(Color.red)
                    .build()).queue();
            return;
        }

        // Get the user from the member
        User user = member.getUser();

        LevelInfo levelInfo = GeyserBot.storageManager.getLevel(member);

        // Get the level progress
        float progress = (float)levelInfo.getXp() / levelInfo.getXpForNextLevel();
        int size = 20;

        // Generate the progress bar
        StringBuilder progressText = new StringBuilder();
        progressText.append("[");
        for (int i = 0; i < Math.round(size * progress); i++) {
            progressText.append("\u2550");
        }
        for (int i = 0; i < (size - Math.round(size * progress)); i++) {
            progressText.append("\u2500");
        }
        progressText.append("]");

        // Do some fancy stuff so we can add an easter egg
        int progressRounded = Math.round(progress * 100);
        String progressTitle = " (" + progressRounded + "%)";
        if (progressRounded == 99) {
            // Shh leave this easter egg here
            progressTitle = "Pogress" + progressTitle;
        } else {
            progressTitle = "Progress" + progressTitle;
        }

        event.getMessage().reply(new EmbedBuilder()
                .setTitle("Level")
                .setDescription(user.getAsMention())
                .addField("Level", String.valueOf(levelInfo.getLevel()), true)
                .addField("XP", levelInfo.getXp() + "/" + levelInfo.getXpForNextLevel(), true)
                .addField(progressTitle, progressText.toString(), false)
                .setThumbnail(user.getAvatarUrl())
                .setFooter("ID: " + user.getId())
                .setTimestamp(Instant.now())
                .setColor(PropertiesManager.getDefaultColor())
                .build()).queue();
    }
}
