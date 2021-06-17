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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.PropertiesManager;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueCommand extends SlashCommand {

    private static final Pattern REPO_PATTERN = Pattern.compile("(^| )([\\w]+\\/)?([\\w]+)( |$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISSUE_PATTERN = Pattern.compile("(^| )#?([0-9]+)( |$)", Pattern.CASE_INSENSITIVE);

    public IssueCommand() {
        this.name = "issue";
        this.aliases = new String[] {"pr"};
        this.arguments = "<number> [repo]";
        this.help = "Get info about a given GitHub issue/pr.";

        this.options = Arrays.asList(
            new OptionData(OptionType.INTEGER, "number", "The issue/pr number").setRequired(true),
            new OptionData(OptionType.STRING, "repo", "The repository to lookup, defaults to GeyserMC/Geyser")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Issue
        int issue = (int) event.getOption("number").getAsLong();
        // Repo
        String repo = event.getOptions().size() > 1 ? event.getOption("repo").getAsString() : "";

        event.replyEmbeds(handle(issue, repo)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        Matcher matcherIssue = ISSUE_PATTERN.matcher(event.getArgs());

        if (!matcherIssue.find()) {
            MessageHelper.errorResponse(event, "Invalid Issue Format", "Please specify the issue you wish to locate.\nEG: `#100` or `100`");
            return;
        }

        event.getMessage().reply(handle(Integer.parseInt(matcherIssue.group(2)), event.getArgs().replace(matcherIssue.group(0), ""))).queue();
    }

    private MessageEmbed handle(int issueNumber, String repoString) {
        GHIssue issue;
        GHUser user;
        String userName;
        Instant timestamp;

        try {
            GHRepository repo;
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

            issue = repo.getIssue(issueNumber);
            user = issue.getUser();
            userName = (user.getName() != null ? user.getName() : user.getLogin());
            timestamp = issue.getCreatedAt().toInstant();
        } catch (IOException ignored) {
            return MessageHelper.errorResponse(null, "Error occurred!", "Don't ask me what went wrong, I'm just letting you know, try again.");
        }

        String cleanBody = "No description provided.";
        if (issue.getBody() != null && issue.getBody().trim().length() != 0) {
            cleanBody = issue.getBody().replaceAll("<!--.*-->(\r\n)?", "");
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(userName, String.valueOf(user.getHtmlUrl()), user.getAvatarUrl())
                .setTitle(issue.getTitle() + " (#" + issue.getNumber() + ")", String.valueOf(issue.getHtmlUrl()))
                .setDescription(cleanBody.length() > 400 ? cleanBody.substring(0, 400) + "..." : cleanBody)
                .setFooter("Created")
                .setTimestamp(timestamp)
                .setColor(PropertiesManager.getDefaultColor());

//        // Add assignees
//        if (issue.getAssignees().size() >= 1) {
//            builder.addField("Assignees", issue.getAssignees().stream().map(ghUser -> {
//                try {
//                    return ghUser.getName();
//                } catch (IOException ignored) { }
//                return null;
//            }).collect(Collectors.joining(", ")), false);
//        }

        if (issue.getState() == GHIssueState.OPEN) {
            builder.setColor(Color.decode("#28a745"));
        } else {
            builder.setColor(Color.decode("#d73a49"));
        }

        if (issue.isPullRequest()) {
            try {
                GHPullRequest pull = issue.getRepository().getPullRequest(issue.getNumber());
                if (pull.isMerged()) {
                    builder.setColor(Color.decode("#6f42c1"));
                } else if (pull.isDraft()) {
                    builder.setColor(Color.decode("#6a737d"));
                }

            } catch (IOException ignored) { }
        }

        return builder.build();
    }
}
