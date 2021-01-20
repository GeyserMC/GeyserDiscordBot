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
import org.geysermc.discordbot.listeners.SwearHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateRestartCommand extends Command {

    // TODO: Make this download from the ci and rename update-restart
    public UpdateRestartCommand() {
        this.name = "update-restart";
        this.hidden = true;
        this.userMissingPermMessage = "";
        this.userPermissions = new Permission[] { Permission.MANAGE_ROLES };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        event.getMessage().reply("```\nUpdating...\n```").queue(message -> {
            StringBuilder logText = new StringBuilder("Updating...");
            logText.append("\n");
            try {
                Process process = Runtime.getRuntime().exec("git pull");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    logText.append("\n").append(line.replaceAll("`", "\\`"));
                    message.editMessage("```\n" + logText + "\n```").queue();
                }

                logText.append("\n\n");
                logText.append("Restarting...");
                message.editMessage("```\n" + logText + "\n```").queue(ignored -> System.exit(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
