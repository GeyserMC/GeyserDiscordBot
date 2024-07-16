/*
 * Copyright (c) 2024 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.automod.AutoModTriggerType;
import net.dv8tion.jda.api.events.automod.AutoModExecutionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

import javax.annotation.Nonnull;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class AutoModHandler extends ListenerAdapter {
    private final Cache<Long, Integer> executionCache;

    public AutoModHandler() {
        this.executionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public void onAutoModExecution(@Nonnull AutoModExecutionEvent event) {
        if (event.getTriggerType() == AutoModTriggerType.MEMBER_PROFILE_KEYWORD) return;

        long userId = event.getUserIdLong();
        Integer executions = this.executionCache.getIfPresent(userId);

        if (executions == null) {
            executions = 0;
        }

        this.executionCache.put(userId, ++executions);

        if (executions < 3) return;

        Member member = event.getGuild().getMemberById(userId);
        if (member == null) return;

        Guild guild = event.getGuild();
        String reason = "Suspected account compromise";

        User user = member.getUser();
        user.openPrivateChannel().queue((channel) -> {
            MessageEmbed embed = new EmbedBuilder()
                .setTitle("You have been automatically kicked from " + guild.getName() + "!")
                .addField("Reason", reason, false)
                .addField("Recommended Actions", "Change your Discord password, enable 2FA, and scan your computer for malware. See [Discord's article](https://support.discord.com/hc/en-us/articles/24160905919511-My-Discord-Account-was-Hacked-or-Compromised) for more info.", false)
                .setTimestamp(Instant.now())
                .setColor(BotColors.WARNING.getColor())
                .build();

            channel.sendMessageEmbeds(embed).queue(message -> {
                // Kick user
                guild.kick(user).reason(reason).queue();
            }, throwable -> {
                // Kick user
                guild.kick(user).reason(reason).queue();
            });
        }, throwable -> {
            // Kick user
            guild.kick(member).reason(reason).queue();
        });


        Member selfMember = guild.getSelfMember();
        int id = GeyserBot.storageManager.addLog(selfMember, "kick", user, reason);

        MessageEmbed logEmbed = new EmbedBuilder()
            .setTitle("Kicked user")
            .addField("User", user.getAsMention(), false)
            .addField("Staff member", selfMember.getAsMention(), false)
            .addField("Reason", reason, false)
            .setFooter("ID: " + id)
            .setTimestamp(Instant.now())
            .setColor(BotColors.WARNING.getColor())
            .build();

        ServerSettings.getLogChannel(guild).sendMessageEmbeds(logEmbed).queue();
    }
}
