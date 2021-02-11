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
import org.geysermc.discordbot.util.BotHelpers;

import java.awt.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class SettingsCommand extends Command {

    public SettingsCommand() {
        this.name = "settings";
        this.hidden = true;
        this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        String title;
        String key = args.get(1);
        String value;

        switch (args.get(0)) {
            case "get":
                title = "Setting value";
                value = GeyserBot.storageManager.getServerPreference(event.getGuild().getIdLong(), key);
                break;

            case "set":
                title = "Updated setting";
                value = args.get(2);
                GeyserBot.storageManager.setServerPreference(event.getGuild().getIdLong(), key, value);
                break;

            default:
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle("Invalid action specified")
                        .setDescription("Unknown action `" + args.get(0) + "`, it can be one of: `get`, `set`")
                        .setTimestamp(Instant.now())
                        .setColor(Color.red)
                        .build()).queue();
                return;
        }

        event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle(title)
                .addField("Key", "`" + key + "`", false)
                .addField("Value", "`" + value + "`", false)
                .setTimestamp(Instant.now())
                .setColor(Color.green)
                .build()).queue();
    }
}
