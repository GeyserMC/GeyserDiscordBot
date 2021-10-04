package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.kohsuke.github.*;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubCommand extends SlashCommand {


    private static final Pattern REPO_PATTERN = Pattern.compile("(^| )([\\w.\\-]+/)?([\\w.\\-]+)( |$)", Pattern.CASE_INSENSITIVE);

    public GithubCommand() {
        this.name = "github";
        this.arguments = "<repo>";
        this.help = "Get info about a given GitHub repo.";
        this.guildOnly = false;

        this.options = Arrays.asList(
                new OptionData(OptionType.STRING, "repo", "The repository to lookup, defaults to GeyserMC/Geyser")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Repo
        String repo =  event.getOption("repo").getAsString();
        event.replyEmbeds(handle(repo)).queue();
    }

    private MessageEmbed handle(String repoString) {
        GHRepository repo;
        GHUser user;
        String userName;

        try {
            Matcher matcherRepo = REPO_PATTERN.matcher(repoString);
            if (matcherRepo.find()) {
                if (matcherRepo.group(2) == null) {
                    PagedSearchIterable<GHRepository> results = GeyserBot.getGithub().searchRepositories().q(matcherRepo.group(3)).list();
                    if (results.getTotalCount() == 0) {
                        return MessageHelper.errorResponse(null, "Error 404, mayday!", "Could not find a repo with specified arguments.");
                    }
                    repo = results.toArray()[0];
                } else {
                    repo = GeyserBot.getGithub().getRepository(matcherRepo.group(2) + matcherRepo.group(3));
                }
            } else {
                repo = GeyserBot.getGithub().getRepository("GeyserMC/Geyser");
            }
            user = repo.getOwner();
            userName = (user.getName() != null ? user.getName() : user.getLogin());
        } catch (GHFileNotFoundException ignored) {
            return MessageHelper.errorResponse(null, "Error 404, mayday!", "Could not find a repo with specified arguments.");
        } catch (IOException ignored) {
            return MessageHelper.errorResponse(null, "Error occurred!", "Don't ask me what went wrong, I'm just letting you know, try again.");
        }

        String cleanBody = "No description provided.";
        if (repo.getDescription()!= null && repo.getDescription().trim().length() != 0) {
            cleanBody = repo.getDescription().replaceAll("<!--.*-->(\r\n)?", "");
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(userName, String.valueOf(user.getHtmlUrl()), user.getAvatarUrl())
                .setTitle(repo.getName(), String.valueOf(repo.getHtmlUrl()))
                .setDescription(cleanBody.length() > 400 ? cleanBody.substring(0, 400) + "..." : cleanBody)
                .setColor(BotColors.SUCCESS.getColor());

        builder.setColor(Color.decode("#28a745"));
        return builder.build();
    }
}
