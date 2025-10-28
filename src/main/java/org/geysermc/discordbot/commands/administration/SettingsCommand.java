/*
 * Copyright (c) 2020-2025 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.commands.administration;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsCommand extends SlashCommand {

    public SettingsCommand() {
        this.name = "settings";
        this.hidden = true;
        this.help = "Customize the bot's settings";

        this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };

        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "action", "The action to perform", true)
                        .addChoice("add","add")
                        .addChoice("get", "get")
                        .addChoice("set", "set")
                        .addChoice("remove","remove"),
                new OptionData(OptionType.STRING, "key", "The setting name", true) // TODO Make this an ENUM? to make it easier to access
                        .addChoice("Allowed Invites", "allowed-invites")
                        .addChoice("Banned domains","banned-domains")
                        .addChoice("Banned IPs", "banned-ips")
                        .addChoice("Check domains", "check-domains")
                        .addChoice("Convert Extensions", "convert-extensions")
                        .addChoice("Don't level", "dont-level")
                        .addChoice("Don't log","dont-log")
                        .addChoice("Forum Channel", "forum-channel")
                        .addChoice("Health Checks", "health-checks")
                        .addChoice("Log channel", "log-channel")
                        .addChoice("Preview Channel", "preview-channel")
                        .addChoice("Preview Feeds Channel", "preview-feeds-channel")
                        .addChoice("Punishment Message", "punishment-message")
                        .addChoice("Roles", "roles")
                        .addChoice("RSS Feeds", "rss-feeds")
                        .addChoice("Update channel", "update-channel")
                        .addChoice("Exploit reports notify channel", "exploit-reports-notify-channel")
                        .addChoice("Donation feeds channel", "donation-feeds-channel"),

                new OptionData(OptionType.STRING, "value", "The value to set")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Fetch values
        String action = event.getOption("action").getAsString();
        String key = event.getOption("key").getAsString();
        String value = event.optString("value", null);

        event.replyEmbeds(handle(event.getGuild(), action, key, value)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Warn the user that they haven't given enough arguments
        if (args.size() <= 1) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setTitle("Invalid usage")
                    .setDescription("Please specify an action and a value!")
                    .setColor(BotColors.FAILURE.getColor())
                    .build()).queue();
            return;
        }

        String action = args.get(0);
        String key = args.get(1);
        String value;

        if (args.size() >= 3 && (args.get(0).equals("set") || args.get(0).equals("add"))) {
            args.remove(1);
            args.remove(0);
            value = String.join(" ", args);
        } else {
            value = null;
        }

        event.getMessage().replyEmbeds(handle(event.getGuild(), action, key, value)).queue();
    }

    private MessageEmbed handle(Guild guild, String action, String key, String updatedValue) {
        String title;
        String value;

        switch (action) {
            case "get" -> {
                title = "Setting value";
                value = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), key);
            }
            case "set" -> {
                title = "Updated setting";
                value = String.join(" ", updatedValue);
                GeyserBot.storageManager.setServerPreference(guild.getIdLong(), key, value);
            }
            case "add" -> {
                title = "Updated setting";

                List<String> list = ServerSettings.getList(guild.getIdLong(), key);
                list.add(String.join(" ", updatedValue));

                ServerSettings.setList(guild.getIdLong(), key, list);

                value = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), key);
            }
            case "remove" -> {
                title = "Updated setting";

                List<String> list = ServerSettings.getList(guild.getIdLong(), key);
                list.remove(String.join(" ", updatedValue));

                ServerSettings.setList(guild.getIdLong(), key, list);

                value = GeyserBot.storageManager.getServerPreference(guild.getIdLong(), key);
            }
            default -> {
                return new EmbedBuilder()
                        .setTitle("Invalid action specified")
                        .setDescription("Unknown action `" + action + "`, it can be one of: `get`, `set`, `add`, `remove`")
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.FAILURE.getColor())
                        .build();
            }
        }

        return new EmbedBuilder()
                .setTitle(title)
                .addField("Key", "`" + key + "`", false)
                .addField("Value", "`" + value + "`", false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}
