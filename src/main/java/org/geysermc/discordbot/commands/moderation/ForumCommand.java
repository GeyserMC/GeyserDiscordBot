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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.channel.concrete.ThreadChannelManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class ForumCommand extends SlashCommand {

    public static final String FORUM_CHANNEL_ID = "1026497075359264871";

    public ForumCommand() {
        this.name = "post";
        this.hidden = true;
        this.help = "help tool to manage forum posts.";
        this.guildOnly = true;
        this.children = new SlashCommand[]{
                new createPostSubCommand(),
                new CloseOldPostSubCommand(),
                new ClosePostSubCommand(),
                new renamePostSubCommand(),
                new addTagPostCommand(),
                new removeTagPostCommand()
        };
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // unused
    }

    public static class createPostSubCommand extends SlashCommand {
        public createPostSubCommand() {
            this.name = "create";
            this.help = "create post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = Arrays.asList(
                    new OptionData(OptionType.STRING, "title", "add the forum title", true),
                    new OptionData(OptionType.STRING, "issue", "add the forum message/issue", true),
                    new OptionData(OptionType.USER, "member", "The member to add to the forum", false)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            String title = Objects.requireNonNull(event.getOption("title")).getAsString();
            String targetUser = event.getOption("member") != null ? Objects.requireNonNull(event.getOption("member")).getAsUser().getAsMention() : "";
            String issue = Objects.requireNonNull(event.getOption("issue")).getAsString();

            if (!targetUser.isEmpty()) {
                issue += " " + targetUser;
            }

            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Guild not found.").queue();
                return;
            }
            ForumChannel forumChannel = event.getGuild().getForumChannelById(FORUM_CHANNEL_ID);
            if (forumChannel == null) {
                event.reply("Forum channel not found.").queue();
                return;
            }
            forumChannel.createForumPost(title, MessageCreateData.fromContent(issue)).queue();
            // Thread created successfully
            event.reply("Forum post created.").queue();
        }
    }

    public static class CloseOldPostSubCommand extends SlashCommand {
        public CloseOldPostSubCommand() {
            this.name = "close-old";
            this.help = "close old posts";
            this.userPermissions = new Permission[]{Permission.MESSAGE_MANAGE};
            this.options = List.of(
                    new OptionData(OptionType.INTEGER, "days", "Enter the max age of bulk post closing.", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            int days = Objects.requireNonNull(event.getOption("days")).getAsInt();
            Guild guild = event.getGuild();
            if (guild == null) {
                event.reply("Guild not found.").queue();
                return;
            }

            ForumChannel forumChannel = guild.getForumChannelById(FORUM_CHANNEL_ID);
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
                    ThreadChannelManager manager = channel.getManager()
                            .setArchived(true);
                    manager.queue();
                }
            }

            event.reply("All forum post older then " + days + " day's have vein closed!").queue();
        }
    }

    public static class ClosePostSubCommand extends SlashCommand {
        public ClosePostSubCommand() {
            this.name = "close";
            this.help = "Close post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!(event.getChannel() instanceof ThreadChannel) || !event.getChannel().asThreadChannel().getParentChannel().getId().equals(FORUM_CHANNEL_ID)) {
                event.reply("Command can only be used in forum channels.").queue();
                return;
            }

            ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager()
                    .setArchived(true);
            event.reply("Post is closed.").queueAfter(1, TimeUnit.SECONDS, success -> manager.queue());
        }

    }

    public static class renamePostSubCommand extends SlashCommand {
        public renamePostSubCommand() {
            this.name = "rename";
            this.help = "rename post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = List.of(
                    new OptionData(OptionType.STRING, "title", "change the forum title", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!(event.getChannel() instanceof ThreadChannel) || !event.getChannel().asThreadChannel().getParentChannel().getId().equals(FORUM_CHANNEL_ID)) {
                event.reply("Command can only be used in forum channels.").queue();
                return;
            }

            String title = Objects.requireNonNull(event.getOption("title")).getAsString();
            ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager().setName(title);

            event.reply("Post is renamed.").queueAfter(1, TimeUnit.SECONDS, success -> manager.queue());
        }
    }

    public static class addTagPostCommand extends SlashCommand {
        public addTagPostCommand() {
            this.name = "add-tag";
            this.help = "add a tag to post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = List.of(
                    new OptionData(OptionType.STRING, "tag", "name of the tag", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!(event.getChannel() instanceof ThreadChannel) || !event.getChannel().asThreadChannel().getParentChannel().getId().equals(FORUM_CHANNEL_ID)) {
                event.reply("Command can only be used in forum channels.").queue();
                return;
            }

            String tag = Objects.requireNonNull(event.getOption("tag")).getAsString();

            List<ForumTag> tags = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTags();

            int index = -1;
            boolean matchFound = false;

            for (int i = 0; i < tags.size(); i++) {
                ForumTag currentTag = tags.get(i);
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

                ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager()
                        .setArchived(true)
                        .setAppliedTags(updatedTags);
                event.reply("Post is tagged with " + tag + ".").queueAfter(1, TimeUnit.SECONDS, success -> manager.queue());
            } else {
                event.reply("No matching tag found for " + tag + ".").queue();
            }
        }
    }

    public static class removeTagPostCommand extends SlashCommand {
        public removeTagPostCommand() {
            this.name = "remove-tag";
            this.help = "remove a tag from post";
            this.userPermissions = new Permission[]{Permission.CREATE_PUBLIC_THREADS};
            this.options = List.of(
                    new OptionData(OptionType.STRING, "tag", "name of the tag", true)
            );
        }

        @Override
        protected void execute(SlashCommandEvent event) {
            if (!(event.getChannel() instanceof ThreadChannel) || !event.getChannel().asThreadChannel().getParentChannel().getId().equals(FORUM_CHANNEL_ID)) {
                event.reply("Command can only be used in forum channels.").queue();
                return;
            }

            String tagToRemove = Objects.requireNonNull(event.getOption("tag")).getAsString();

            List<ForumTag> tags = event.getChannel().asThreadChannel().getParentChannel().asForumChannel().getAvailableTags();

            int indexToRemove = -1;

            for (int i = 0; i < tags.size(); i++) {
                ForumTag currentTag = tags.get(i);
                if (currentTag.getName().equalsIgnoreCase(tagToRemove)) {
                    indexToRemove = i;
                    break;
                }
            }

            if (indexToRemove != -1) {
                List<ForumTag> currentTags = event.getChannel().asThreadChannel().getAppliedTags();

                List<ForumTagSnowflake> updatedTags = new ArrayList<>(currentTags);

                updatedTags.remove(indexToRemove);

                ThreadChannelManager manager = event.getChannel().asThreadChannel().getManager()
                        .setArchived(true)
                        .setAppliedTags(updatedTags);
                event.reply("Tag " + tagToRemove + " removed.").queueAfter(1, TimeUnit.SECONDS, success -> manager.queue());
            } else {
                event.reply("Tag " + tagToRemove + " not found.").queue();
            }
        }
    }
}