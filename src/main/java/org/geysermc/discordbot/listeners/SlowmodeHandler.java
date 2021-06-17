package org.geysermc.discordbot.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlowmodeHandler extends ListenerAdapter {
    private final long channelId;
    private int seconds;
    private final Cache<OffsetDateTime, User> messageCache;

    public SlowmodeHandler(long channelId, int seconds) {
        this.channelId = channelId;
        this.seconds = seconds;

        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(seconds, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (this.channelId == event.getMessage().getChannel().getIdLong()) {
            if (canBypassSlowMode(event.getMember())) {
                return;
            }

            OffsetDateTime dateTime = getLastPosted(event.getAuthor());
            if (dateTime != null) {
                PrettyTime t = new PrettyTime(event.getMessage().getTimeCreated().toInstant());
                event.getMessage().delete().queue(unused ->
                        event.getAuthor().openPrivateChannel().queue(channel ->
                                channel.sendMessage(new EmbedBuilder()
                                        .setTitle("Message deleted")
                                        .appendDescription("Your message has been removed because there is still a delay before you can type!")
                                        .addField("Channel", event.getMessage().getTextChannel().getAsMention(), false)
                                        .addField("When you can post again", t.format(dateTime.plusSeconds(seconds).toInstant()), false)
                                        .setTimestamp(Instant.now())
                                        .setColor(Color.red)
                                        .build()).queue()));
            } else {
                // Don't add to the cache if a message is deleted - that way the timer doesn't reset.
                this.messageCache.put(event.getMessage().getTimeCreated(), event.getAuthor());
            }
        }
    }

    private boolean canBypassSlowMode(Member member) {
        return member.getUser().isBot() || member.hasPermission(Permission.MESSAGE_MANAGE);
    }

    private OffsetDateTime getLastPosted(User user) {
        for (Map.Entry<OffsetDateTime, User> entry : messageCache.asMap().entrySet()) {
            if (entry.getValue().getIdLong() == user.getIdLong()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
}
