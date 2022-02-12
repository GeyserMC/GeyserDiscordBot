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

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.MessageHelper;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

public class GithubCommand extends SlashCommand {

    public GithubCommand() {
        this.name = "github";
        this.arguments = "<repo>";
        this.help = "Get info about a given GitHub repo.";
        this.guildOnly = false;
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "repo", "The repository to lookup")
                        .setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String repo = event.optString("repo", "");
        event.deferReply(false).queue(interactionHook -> {
            try {
                interactionHook.editOriginalEmbeds(handle(repo)).queue();
            } catch (IOException e) {
                MessageHelper.errorResponse(event.getChannel(), "Error 404, mayday!", "Could not retrieve data from GitHub, try again later!");
            }
        });
    }

    private MessageEmbed handle(String repoString) throws IOException {
        GHRepository repo;
        try {
           repo = BotHelpers.getRepo(repoString);
        } catch (Exception e) { 
            return MessageHelper.errorResponse(null, "Error 404, mayday!", "Could not find a repo with specified arguments.");
        }

        GHUser user = repo.getOwner();;

        EmbedBuilder builder = new EmbedBuilder()
            .setAuthor(user.getName() != null ? user.getName() : user.getLogin(), String.valueOf(user.getHtmlUrl()), user.getAvatarUrl())
            .setTitle(repo.getName(), String.valueOf(repo.getHtmlUrl()))
            .setDescription(repo.getDescription())
            .addField("Most Used Language", repo.getLanguage(), false)
            .addField("Forks", String.valueOf(repo.getForksCount()), false)
            .addField("Watchers", String.valueOf(repo.getWatchersCount()), false)
            .setColor(BotColors.SUCCESS.getColor());

        if (repo.getLicense() != null) {
            builder.addField("License", repo.getLicense().getName(), false);
        }
        if (repo.getCreatedAt() != null) {
            builder.setFooter("Created at ").setTimestamp(repo.getCreatedAt().toInstant());
        }

        return builder.build();
    }
}
