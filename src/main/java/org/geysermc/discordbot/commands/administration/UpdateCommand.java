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

package org.geysermc.discordbot.commands.administration;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.geysermc.discordbot.GeyserBot;
import pw.chew.chewbotcca.util.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class UpdateCommand extends SlashCommand {

    public UpdateCommand() {
        this.name = "update";
        this.hidden = true;
        this.help = "Update the discord bot";
        this.userMissingPermMessage = "";

        this.userPermissions = new Permission[]{Permission.MANAGE_ROLES};
        this.botPermissions = new Permission[]{Permission.MANAGE_ROLES};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.reply("Please check below for the log").queue();
        handle(event.getTextChannel().sendMessage("```\n```").complete());
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().reply("Please check below for the log").queue();
        handle(event.getTextChannel().sendMessage("```\n```").complete());
    }

    private void handle(Message message) {
        message.editMessage("```\nChecking for updates...\n```").queue(log -> {
            StringBuilder logText = new StringBuilder("Checking for updates...");
            logText.append("\n");

            Properties gitProp = new Properties();
            try {
                gitProp.load(UpdateCommand.class.getClassLoader().getResourceAsStream("git.properties"));

                String buildXML = RestClient.get("https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/" + URLEncoder.encode(gitProp.getProperty("git.branch"), StandardCharsets.UTF_8.toString()) + "/lastSuccessfulBuild/api/xml?xpath=//buildNumber");
                if (buildXML.startsWith("<buildNumber>")) {
                    int latestBuildNum = Integer.parseInt(buildXML.replaceAll("<(\\\\)?(/)?buildNumber>", "").trim());
                    int buildNum = Integer.parseInt(gitProp.getProperty("git.build.number"));
                    if (latestBuildNum == buildNum) {
                        logText.append("\n").append("No updates available!");
                        log.editMessage("```\n" + logText + "\n```").queue();
                        return;
                    } else {
                        logText.append("\n").append(latestBuildNum - buildNum).append(" version(s) behind, updating...");
                        log.editMessage("```\n" + logText + "\n```").queue();
                    }
                } else {
                    logText.append("\n").append("Buildnumber missing from Jenkins!");
                    log.editMessage("```\n" + logText + "\n```").queue();
                    return;
                }
            } catch (IOException | AssertionError | NumberFormatException e) {
                logText.append("\n").append("Not in production, can't update!");
                log.editMessage("```\n" + logText + "\n```").queue();
                return;
            }

            try {
                String fileName = "GeyserBot.jar";
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    fileName = "GeyserBot.upd.jar";

                    logText.append("\n").append("Warning! Windows detected please rename the ").append(fileName).append(" when the update has finished.");
                    log.editMessage("```\n" + logText + "\n```").queue();
                }

                InputStream in = new URL("https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/" + URLEncoder.encode(gitProp.getProperty("git.branch"), StandardCharsets.UTF_8.toString()) + "/lastSuccessfulBuild/artifact/target/GeyserBot.jar").openStream();
                Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                logText.append("\n").append("Updated!").append("\n\n").append("Restarting...");
                log.editMessage("```\n" + logText + "\n```").queue(ignored -> GeyserBot.getJDA().shutdown());
            } catch (IOException e) {
                logText.append("\n").append("Unable to download updated jar!").append("\n").append(e);
                log.editMessage("```\n" + logText + "\n```").queue();
            }
        });

    }
}
