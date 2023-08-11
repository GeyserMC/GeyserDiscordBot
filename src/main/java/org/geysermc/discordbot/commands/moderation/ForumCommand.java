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
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Checks;
import org.geysermc.discordbot.storage.ServerSettings;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.*;

public class ForumCommand extends SlashCommand {

    public ForumCommand() {
        this.name = "post";
        this.hidden = true;
        this.help = "Help tool to manage forum posts.";
        this.guildOnly = true;
        this.children = new SlashCommand[]{
                new CreatePostSubCommand(),
                new CloseOldPostSubCommand(),
                new ClosePostSubCommand(),
                new RenamePostSubCommand(),
                new AddTagPostSubCommand(),
                new removeTagPostSubCommand()
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
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
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

    public static class CloseOldPostSubCommand extends SlashCommand {
        public CloseOldPostSubCommand() {
            this.name = "close-old";
            this.help = "Close old posts";
            this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
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

    public static class ClosePostSubCommand extends SlashCommand {
        public ClosePostSubCommand() {
            this.name = "close";
            this.help = "Close post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (isForumChannel(event)) {
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

    public static class RenamePostSubCommand extends SlashCommand {
        public RenamePostSubCommand() {
            this.name = "rename";
            this.help = "Rename post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = Collections.singletonList(new OptionData(OptionType.STRING, "title", "change the forum title", true));
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (isForumChannel(event)) {
                return;
            }

            ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager().setName(Objects.requireNonNull(event.optString("title")));
            manager.queue(
                    unused -> event.reply("Post is renamed!").queue(),
                    error -> event.reply("Could not rename post").queue()
            );
        }
    }

    public static class AddTagPostSubCommand extends SlashCommand {
        public AddTagPostSubCommand() {
            this.name = "add-tag";
            this.help = "Add a tag to post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = getTags();
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (isForumChannel(event)) {
                return;
            }

            String tag = event.optString("tag");
            List<ForumTag> tags = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTags();
            int index = -1;
            boolean matchFound = false;
            for (int i = 0; i < tags.size(); i++) {
                ForumTag currentTag = tags.get(i);
                assert tag != null;
                if (currentTag.getName().contains(tag)) {
                    index = i;
                    matchFound = true;
                    break;
                }
            }

            if (matchFound) {
                List<ForumTag> currentTags = event.getChannel().asThreadChannel().getAppliedTags();
                ForumTagSnowflake newTag = ForumTagSnowflake.fromId(tags.get(index).getId());
                List<ForumTagSnowflake> updatedTags = new ArrayList<>(currentTags);
                updatedTags.add(newTag);
                ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager().setAppliedTags(updatedTags);
                manager.queue(
                        unused -> event.reply("Post is tagged with " + tag + ".").queue(),
                        error -> event.reply("Could not tag post").queue()
                );

            } else {
                event.reply("No matching tag found for " + tag + ".").queue();
            }
        }
    }

    public static class removeTagPostSubCommand extends SlashCommand {
        public removeTagPostSubCommand() {
            this.name = "remove-tag";
            this.help = "Remove a tag from post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = getTags();
        }

        @Override
        protected void execute(@NotNull SlashCommandEvent event) {
            if (isForumChannel(event)) {
                return;
            }

            String tagToRemove = event.optString("tag");
            int indexToRemove = -1;
            List<ForumTag> currentTags = event.getChannel().asThreadChannel().getAppliedTags();
            for (int i = 0; i < currentTags.size(); i++) {
                ForumTag currentTag = currentTags.get(i);
                if (currentTag.getName().equalsIgnoreCase(tagToRemove)) {
                    indexToRemove = i;
                    break;
                }
            }

            if (indexToRemove != -1) {
                List<ForumTagSnowflake> updatedTags = new ArrayList<>(currentTags);
                updatedTags.remove(indexToRemove);
                ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager().setAppliedTags(updatedTags);
                manager.queue(
                        unused -> event.reply("Tag " + tagToRemove + " removed.").queue(),
                        error -> event.reply("Could not remove tag from post").queue()
                );

            } else {
                event.reply("Post was not tagged with  " + tagToRemove + ".").queue();
            }
        }
    }

    @NotNull
    public static List<OptionData> getTags() {
        return Collections.singletonList(
                new OptionData(OptionType.STRING, "tag", "name of the tag")
                        .addChoice("Error On Startup", "Error On Startup")
                        .addChoice("Closed", "Closed")
                        .addChoice("Gameplay Error", "Gameplay Error")
                        .addChoice("Other/Misc.", "Other/Misc.")
                        .addChoice("Spigot/Paper", "Spigot/Paper")
                        .addChoice("Fabric", "Fabric")
                        .addChoice("BungeeCord", "BungeeCord")
                        .addChoice("Velocity", "Velocity")
                        .addChoice("Standalone", "Standalone")
                        .addChoice("Answered", "Answered")
                        .addChoice("Resource Packs", "Resource Packs")
                        .setRequired(true)
        );
    }

    private static boolean isForumChannel(@NotNull SlashCommandEvent event) {
        if (event.getChannel().asThreadChannel().getParentChannel().getId().equals(Objects.requireNonNull(ServerSettings.getForumChannel(Objects.requireNonNull(event.getGuild()))).getId())) {
            return true;
        }
        event.reply("Command can only be used in forum channels").queue();
        return false;
    }
}