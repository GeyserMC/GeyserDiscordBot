package org.geysermc.discordbot.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlowModeHandler extends ListenerAdapter {
    private final long channelId;
    private final int minutes;
    private final Cache<OffsetDateTime, User> messageCache;

    public SlowModeHandler(long channelId, int minutes) {
        this.channelId = channelId;
        this.minutes = minutes;

        this.messageCache = CacheBuilder.newBuilder()
                .expireAfterWrite(minutes, TimeUnit.MINUTES)
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
                PrettyTime t = new PrettyTime(dateTime.plusMinutes(minutes).toInstant());
                event.getMessage().delete().queue(unused ->
                        event.getAuthor().openPrivateChannel().queue(channel ->
                                channel.sendMessage(new EmbedBuilder()
                                        .setTitle("Message deleted")
                                        .appendDescription(event.getAuthor().getAsMention())
                                        .appendDescription(", your message in ").appendDescription(event.getMessage().getChannel().getName())
                                        .appendDescription(" has been removed because there is still a delay before you can type!\nWhen you can post again: ")
                                        .appendDescription(t.format(t.calculatePreciseDuration(event.getMessage().getTimeCreated().toInstant())).replace(" ago", ""))
                                        .build()).queue()));
            } else {
                // Don't add to the cache if a message is deleted - that way the timer doesn't reset.
                this.messageCache.put(event.getMessage().getTimeCreated(), event.getAuthor());
            }
        }
    }

    private boolean canBypassSlowMode(Member member) {
        return false;
    }

    private OffsetDateTime getLastPosted(User user) {
        for (Map.Entry<OffsetDateTime, User> entry : messageCache.asMap().entrySet()) {
            if (entry.getValue().getIdLong() == user.getIdLong()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
