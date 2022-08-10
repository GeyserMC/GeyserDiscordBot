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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.MessageHelper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhoisCommand extends SlashCommand {

    private static final List<Permission> PRIVILEGED_PERMISSIONS = Arrays.asList(Permission.ADMINISTRATOR, Permission.MANAGE_SERVER, Permission.MANAGE_ROLES, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MANAGE, Permission.MANAGE_WEBHOOKS, Permission.NICKNAME_MANAGE, Permission.MANAGE_EMOJIS_AND_STICKERS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS, Permission.MESSAGE_MENTION_EVERYONE);

    public WhoisCommand() {
        this.name = "whois";
        this.arguments = "[member]";
        this.help = "Show some information for a member";

        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "member", "The member you want to get the information for")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member member = event.optMember("member", event.getMember());

        event.replyEmbeds(handle(member)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        Member member;
        if (args.size() == 0 || args.get(0).isEmpty()) {
            member = event.getMember();
        } else {
            member = BotHelpers.getMember(event.getGuild(), args.remove(0));
        }

        // Check user is valid
        if (member == null) {
            MessageHelper.errorResponse(event, "Invalid user", "The user ID specified doesn't link with any valid user in this server.");
            return;
        }

        event.getMessage().replyEmbeds(handle(member)).queue();
    }

    protected MessageEmbed handle(Member member) {
        // Get the user from the member
        User user = member.getUser();

        // Get the roles
        String roles = "None";
        if (member.getRoles().size() > 0) {
            roles = member.getRoles().stream().map(Role::getAsMention).collect(Collectors.joining(" "));
        }

        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setDescription(user.getAsMention())
                .addField("Joined", TimeFormat.DATE_TIME_LONG.format(member.getTimeJoined()), true)
                .addField("Registered", TimeFormat.DATE_TIME_LONG.format(member.getTimeCreated()), true)
                .addField("Roles [" + member.getRoles().size() + "]", roles, false)
                .addField("Key Permissions", member.getPermissions().stream().filter(PRIVILEGED_PERMISSIONS::contains).map(Permission::getName).collect(Collectors.joining(", ")), false)
                .setThumbnail(user.getAvatarUrl())
                .setFooter("ID: " + user.getId())
                .setTimestamp(Instant.now())
                .setColor(member.getColor())
                .build();
    }
}
