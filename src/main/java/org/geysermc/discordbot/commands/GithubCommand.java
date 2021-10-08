package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
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
                new OptionData(OptionType.STRING, "repo", "The repository to lookup, defaults to GeyserMC/Geyser")
                        .setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String repo = Objects.requireNonNull(event.getOption("repo")).getAsString();
        event.deferReply(false).queue(interactionHook -> {
            try {
                interactionHook.editOriginalEmbeds(handle(repo)).queue();
            } catch (IOException ignored) { }
        });
    }

    private MessageEmbed handle(String repoString) throws IOException {
        GHRepository repo;
        try {
           repo = (GHRepository) BotHelpers.getRepo(repoString);
        } catch (Exception e) {
            return MessageHelper.errorResponse(null, "Error 404, mayday!", "Could not find a repo with specified arguments.");
        }
        GHUser user;
        String userName;
        user = repo.getOwner();
        userName = (user.getName() != null ? user.getName() : user.getLogin());

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(userName, String.valueOf(user.getHtmlUrl()), user.getAvatarUrl());
        builder.setTitle(repo.getName(), String.valueOf(repo.getHtmlUrl()));
        builder.setDescription(repo.getDescription());
        builder.addField("Most Used Language", repo.getLanguage(), false);
        builder.addField("Forks", String.valueOf(repo.getForksCount()), false);
        builder.addField("Watchers", String.valueOf(repo.getWatchersCount()), false);
        if (repo.getLicense() != null)
            builder.addField("License", repo.getLicense().getName(), false);
        if (repo.getCreatedAt() != null)
            builder.setFooter("Created at ").setTimestamp(repo.getCreatedAt().toInstant());
        builder.setColor(BotColors.SUCCESS.getColor());

        return builder.build();
    }
}
