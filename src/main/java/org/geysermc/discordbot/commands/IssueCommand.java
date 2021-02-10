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
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.util.PropertiesManager;
import org.kohsuke.github.*;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IssueCommand extends Command {

    private static final Pattern REPO_PATTERN = Pattern.compile("(^| )([\\w]+\\/)?([\\w]+)( |$)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISSUE_PATTERN = Pattern.compile("(^| )#?([0-9]+)( |$)", Pattern.CASE_INSENSITIVE);

    public IssueCommand() {
        this.name = "issue";
        this.aliases = new String[] {"pr"};
        this.arguments = "<number> [repo]";
        this.help = "Get info about a given PR";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        Matcher matcherIssue = ISSUE_PATTERN.matcher(event.getArgs());

        if (!matcherIssue.find()) {
            return;
        }

        GHIssue issue;
        GHUser user;
        String userName;
        Instant timestamp;

        try {
            GitHub github = GitHub.connect();

            GHRepository repo;
            Matcher matcherRepo = REPO_PATTERN.matcher(event.getArgs().replace(matcherIssue.group(0), ""));

            if (matcherRepo.find()) {
                if (matcherRepo.group(2) == null) {
                    PagedSearchIterable<GHRepository> results = github.searchRepositories().q(matcherRepo.group(3)).list();
                    if (results.getTotalCount() == 0) {
                        // TODO: Handle error
                        return;
                    }
                    repo = results.toArray()[0];
                } else {
                    repo = github.getRepository(matcherRepo.group(2) + matcherRepo.group(3));
                }
            } else {
                repo = github.getRepository("GeyserMC/Geyser");
            }

            issue = repo.getIssue(Integer.parseInt(matcherIssue.group(2)));
            user = issue.getUser();
            userName = (user.getName() != null ? user.getName() : user.getLogin());
            timestamp = issue.getCreatedAt().toInstant();
        } catch (IOException ignored) {
            // TODO: Handle error
            return;
        }

        String cleanBody = "No description provided.";
        if (issue.getBody() != null && issue.getBody().trim().length() != 0) {
            cleanBody = issue.getBody().replaceAll("<!--.*-->(\r\n)?", "");
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(userName, String.valueOf(user.getHtmlUrl()), user.getAvatarUrl())
                .setTitle(issue.getTitle() + " (#" + issue.getNumber() + ")", String.valueOf(issue.getHtmlUrl()))
                .setDescription(cleanBody.length() > 400 ? cleanBody.substring(0, 400) + "..." : cleanBody)
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

        event.getMessage().reply(builder.build()).queue();
    }
}
