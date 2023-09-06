/*
 * Copyright (c) 2020-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.commands.moderation;

import com.jagrosh.jdautilities.command.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Checks;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.DicesCoefficient;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.*;

public class ForumPostCommand extends SlashCommand {

    public ForumPostCommand() {
        this.name = "post";
        this.hidden = true;
        this.help = "Help tool to manage forum posts.";
        this.guildOnly = true;
        this.children = new SlashCommand[] {
                new CreatePostSubCommand(),
                new RenamePostSubCommand(),
                new ClosePostSubCommand(),
                new AddPostTagSubCommand(),
                new RemovePostTagSubCommand(),
                new CloseOldPostSubCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // unused
    }

    public static class CreatePostSubCommand extends SlashCommand {
        public CreatePostSubCommand() {
            this.name = "create";
            this.help = "Create post";
            this.userPermissions = new Permission[] { Permission.CREATE_PUBLIC_THREADS };
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "title", "Add the post title", true),
                    new OptionData(OptionType.STRING, "issue", "Add the post message/issue", true),
                    new OptionData(OptionType.USER, "member", "The member to ping in the post", false)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            Checks.notNull(event.getGuild(), "server");

            String title = event.optString("title", "");
            OptionMapping memberMapping = event.getOption("member");
            String targetUser = memberMapping == null ? "" : memberMapping.getAsUser().getAsMention();
            String issue = event.optString("issue", "");

            if (!targetUser.isEmpty()) {
                issue += " " + targetUser;
            }

            ForumChannel forumChannel = ServerSettings.getForumChannel(event.getGuild());
            if (forumChannel == null) {
                event.reply("Forum channel not found.").queue();
                return;
            }

            forumChannel.createForumPost(title, MessageCreateData.fromContent(issue))
                    .queue(
                            unused -> event.reply("Post is created").queue(),
                            error -> event.reply("Could not create post").queue()
                    );
        }
    }

    public static class RenamePostSubCommand extends SlashCommand {
        public RenamePostSubCommand() {
            this.name = "rename";
            this.help = "Rename post";
            this.userPermissions = new Permission[] { Permission.CREATE_PUBLIC_THREADS };
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "title", "change the forum title", true));
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (!isForumChannel(event)) {
                return;
            }

            ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager().setName(Objects.requireNonNull(event.optString("title")));
            manager.queue(
                    unused -> event.reply("Post is renamed!").queue(),
                    error -> event.reply("Could not rename post").queue()
            );
        }
    }

    public static class ClosePostSubCommand extends SlashCommand {
        public ClosePostSubCommand() {
            this.name = "close";
            this.help = "Close post";
            this.userPermissions = new Permission[] { Permission.CREATE_PUBLIC_THREADS };
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (!isForumChannel(event)) {
                return;
            }

            if (event.getChannel().asThreadChannel().isArchived()) {
                event.reply("Post is already closed.").queue();
                return;
            }

            ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager();
            event.reply("Closing post...").queue(
                    reply -> manager.setArchived(true).queue(
                            unused -> reply.editOriginal("Post is closed.").queue(),
                            error -> reply.editOriginal("Could not close post.").queue()
                    ));
        }
    }

    public static class AddPostTagSubCommand extends SlashCommand {
        public AddPostTagSubCommand() {
            this.name = "add-tag";
            this.help = "Add a tag to post";
            this.userPermissions = new Permission[] { Permission.CREATE_PUBLIC_THREADS };
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "tag", "The tag to add", true)
                    .setAutoComplete(true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (!isForumChannel(event)) {
                return;
            }

            ThreadChannel threadChannel = event.getChannel().asThreadChannel();

            // Find the tag from the users input
            String tag = event.optString("tag");
            ForumTag tagFound = null;
            for (ForumTag forumTag : threadChannel.getParentChannel().asForumChannel().getAvailableTags()) {
                if (forumTag.getName().equalsIgnoreCase(tag)) {
                    tagFound = forumTag;
                    break;
                }
            }

            if (tagFound != null) {
                // Create the new tag list
                List<ForumTagSnowflake> updatedTags = new ArrayList<>(threadChannel.getAppliedTags());

                // Make sure we don't add the same tag twice
                ForumTagSnowflake newTag = ForumTagSnowflake.fromId(tagFound.getId());
                if (updatedTags.contains(newTag)) {
                    event.reply("Post is already tagged with " + tag + ".").queue();
                    return;
                }

                updatedTags.add(newTag);

                threadChannel.getManager()
                    .setAppliedTags(updatedTags)
                    .queue(
                        unused -> event.reply("Post is tagged with " + tag + ".").queue(),
                        error -> event.reply("Could not tag post").queue()
                    );
            } else {
                event.reply("No matching tag found for " + tag + ".").queue();
            }
        }

        @Override
        public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
            // Get the query
            String query = event.getFocusedOption().getValue();

            // Get the tags
            List<ForumTag> tags = potentialTags(event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTags(), query);

            event.replyChoices(tags.stream()
                    .distinct()
                    .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                    .limit(25)
                    .toArray(Command.Choice[]::new))
                .queue();
        }
    }

    public static class RemovePostTagSubCommand extends SlashCommand {
        public RemovePostTagSubCommand() {
            this.name = "remove-tag";
            this.help = "Remove a tag from post";
            this.userPermissions = new Permission[] { Permission.CREATE_PUBLIC_THREADS };
            this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "tag", "The tag to remove", true)
                    .setAutoComplete(true)
            );
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (!isForumChannel(event)) {
                return;
            }

            ThreadChannel threadChannel = event.getChannel().asThreadChannel();
            List<ForumTag> currentTags = threadChannel.getAppliedTags();

            // Find the tag from the users input
            String tag = event.optString("tag");
            ForumTag tagFound = null;
            for (ForumTag forumTag : currentTags) {
                if (forumTag.getName().equalsIgnoreCase(tag)) {
                    tagFound = forumTag;
                    break;
                }
            }

            if (tagFound != null) {
                List<ForumTagSnowflake> updatedTags = new ArrayList<>(currentTags);
                updatedTags.remove(ForumTagSnowflake.fromId(tagFound.getId()));
                threadChannel.getManager()
                    .setAppliedTags(updatedTags)
                    .queue(
                        unused -> event.reply("Tag " + tag + " removed.").queue(),
                        error -> event.reply("Could not remove tag from post").queue()
                    );

            } else {
                event.reply("Post was not tagged with " + tag + ".").queue();
            }
        }

        @Override
        public void onAutoComplete(CommandAutoCompleteInteractionEvent event) {
            // Get the query
            String query = event.getFocusedOption().getValue();

            // Get the tags
            List<ForumTag> tags = potentialTags(event.getChannel().asThreadChannel().getAppliedTags(), query);

            event.replyChoices(tags.stream()
                    .distinct()
                    .map(tag -> new Command.Choice(tag.getName(), tag.getName()))
                    .limit(25)
                    .toArray(Command.Choice[]::new))
                .queue();
        }
    }

    public static class CloseOldPostSubCommand extends SlashCommand {
        public CloseOldPostSubCommand() {
            this.name = "close-old";
            this.help = "Close old posts";
            this.userPermissions = new Permission[] { Permission.MESSAGE_MANAGE };
            this.options = Collections.singletonList(new OptionData(OptionType.INTEGER, "days", "The minimum age in days of posts that will be closed in bulk", true));
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            int days = Objects.requireNonNull(event.getOption("days")).getAsInt();
            Checks.notNull(event.getGuild(), "server");

            ForumChannel forumChannel = ServerSettings.getForumChannel(event.getGuild());
            if (forumChannel == null) {
                event.reply("Forum channel not found.").queue();
                return;
            }

            for (ThreadChannel channel : forumChannel.getThreadChannels()) {
                if (channel.isArchived() || channel.isLocked() || channel.isPinned()) {
                    continue;
                }
                OffsetDateTime channelCreated = channel.getTimeCreated();
                OffsetDateTime closeTime = OffsetDateTime.now().minusDays(days);
                if (channelCreated.isBefore(closeTime)) {
                    ThreadChannelManager manager = channel.getManager().setArchived(true);
                    manager.queue();
                }
            }

            event.reply("All forum posts older than " + days + " days have been closed!").queue();
        }
    }

    private static List<ForumTag> potentialTags(List<ForumTag> tags, String query) {
        List<ForumTag> potential = new ArrayList<>();

        // Search the providers by an exact starting match
        for (ForumTag tag : tags) {
            if (tag.getName().toLowerCase().startsWith(query.toLowerCase())) {
                potential.add(tag);
            }
        }

        // Find the best similarity
        for (ForumTag tag : tags) {
            double similar = DicesCoefficient.diceCoefficientOptimized(query.toLowerCase(), tag.getName().toLowerCase());
            if (similar > 0.2d) {
                potential.add(tag);
            }
        }

        // Send a message if we don't know what provider
        return potential;
    }

    private static boolean isForumChannel(@NotNull SlashCommandEvent event) {
        if (event.getChannel().asThreadChannel().getParentChannel().getId().equals(Objects.requireNonNull(ServerSettings.getForumChannel(Objects.requireNonNull(event.getGuild()))).getId())) {
            return true;
        }
        event.reply("Command can only be used in forum channels").queue();
        return false;
    }
}