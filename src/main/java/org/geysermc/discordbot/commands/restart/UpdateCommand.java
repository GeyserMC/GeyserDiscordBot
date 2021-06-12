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

package org.geysermc.discordbot.commands.restart;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.Permission;
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

public class UpdateCommand extends Command {

    // TODO: Make this download from the ci and rename update-restart
    public UpdateCommand() {
        this.name = "update";
        this.hidden = true;
        this.userMissingPermMessage = "";
        this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().reply("```\nChecking for updates...\n```").queue(message -> {
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
                        message.editMessage("```\n" + logText + "\n```").queue();
                        return;
                    } else {
                        logText.append("\n").append(latestBuildNum - buildNum).append(" versions behind, updating...");
                        message.editMessage("```\n" + logText + "\n```").queue();
                    }
                } else {
                    logText.append("\n").append("Buildnumber missing from Jenkins!");
                    message.editMessage("```\n" + logText + "\n```").queue();
                    return;
                }
            } catch (IOException | AssertionError | NumberFormatException e) {
                logText.append("\n").append("Not in production, can't update!");
                message.editMessage("```\n" + logText + "\n```").queue();
                return;
            }

            try {
                String fileName = "GeyserBot.jar";
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    fileName = "GeyserBot.upd.jar";

                    logText.append("\n").append("Warning! Windows detected please rename the ").append(fileName).append(" when the update has finished.");
                    message.editMessage("```\n" + logText + "\n```").queue();
                }

                InputStream in = new URL("https://ci.opencollab.dev/job/GeyserMC/job/GeyserDiscordBot/job/" + URLEncoder.encode(gitProp.getProperty("git.branch"), StandardCharsets.UTF_8.toString()) + "/lastSuccessfulBuild/artifact/target/GeyserBot.jar").openStream();
                Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
                logText.append("\n").append("Updated!").append("\n\n").append("Restarting...");
                message.editMessage("```\n" + logText + "\n```").queue(ignored -> event.getJDA().shutdown());
            } catch (IOException e) {
                logText.append("\n").append("Unable to download updated jar!").append("\n").append(e.toString());
                message.editMessage("```\n" + logText + "\n```").queue();
            }
        });
    }
}
