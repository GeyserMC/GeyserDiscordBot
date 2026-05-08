/*
 * Copyright (c) 2026 GeyserMC. http://geysermc.org
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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.ModerationHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;

public class QuarantineHandler extends ListenerAdapter {
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.isFromGuild()) return;
        if (!event.getMember().hasPermission(event.getGuildChannel(), Permission.VIEW_CHANNEL)) return;

        String customId = event.getComponentId();
        if (!customId.equals("quarantine-handler")) return;

        String actionId = event.getValues().getFirst();

        String userId = event.getMessage().getContentRaw().substring(2, event.getMessage().getContentRaw().length() - 1);

        Member member = event.getGuild().getMemberById(userId);
        if (member == null) {
            event.reply("Member has left server. Cannot take quarantine action.").queue();
            return;
        }

        // Remove the buttons!!!
        event.getMessage().editMessage(new MessageEditBuilder()
                .setContent("Handled by: " + event.getUser().getAsMention())
                .setEmbeds(new EmbedBuilder()
                        .setTitle("Quarantine action handled.")
                        .setDescription("This quarantine was handled with action `%s`.".formatted(actionId))
                        .setTimestamp(Instant.now())
                        .setColor(BotColors.SUCCESS.getColor())
                        .build())
                .setComponents()
                .build()).queue();

        switch (actionId) {
            case "unquarantine" -> {
                member.removeTimeout().queue(v -> {
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Unquarantined member")
                                    .setDescription("Unquarantined " + member.getAsMention() + ".")
                                    .build()
                    ).queue();

                    member.getUser().openPrivateChannel().queue((channel) -> {
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("Welcome back!")
                                .setDescription("You have been unquarantined from " + event.getGuild().getName() + "!")
                                .setTimestamp(Instant.now())
                                .setColor(BotColors.SUCCESS.getColor());

                        channel.sendMessageEmbeds(embedBuilder.build()).queue();
                    });
                }, throwable -> {
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Error")
                                    .setDescription("Issue unquaranting " + member.getAsMention() + ".")
                                    .build()
                    ).queue();
                });
            }
            case "honeypot-misuse", "timeout" -> {
                String reason = actionId.equals("honeypot-misuse") ? "Honey pot channel misuse." : "Timed out while in quarantine.";
                int days = actionId.equals("honeypot-misuse") ? 1 : 7;

                member.removeTimeout().queue(v -> {
                    event.replyEmbeds(ModerationHelper.timeoutUser(member, event.getMember(), event.getGuild(), Duration.ofDays(days), false, reason)).queue();
                }, throwable -> {
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Error")
                                    .setDescription("Issue when changing timeout time for " + member.getAsMention() + ".")
                                    .build()
                    ).queue();
                });
            }
            case "kick" -> {
                member.removeTimeout().queue(v -> {
                    event.replyEmbeds(ModerationHelper.kickUser(member, event.getMember(), event.getGuild(), false, "Kicked from quarantine", event.getChannel())).queue();
                }, throwable -> {
                    event.replyEmbeds(ModerationHelper.kickUser(member, event.getMember(), event.getGuild(), false, "Kicked from quarantine", event.getChannel())).queue();
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Error")
                                    .setDescription("Issue when kicking " + member.getAsMention() + ", couldn't remove timeout.")
                                    .build()
                    ).queue();
                });
            }
            case "compromised", "ban" -> {
                String reason = actionId.equals("compromised") ? "Scammer or compromised account" : "Banned while in quarantine";
                int days = actionId.equals("compromised") ? 1 : 0;

                member.removeTimeout().queue(v -> {
                    event.replyEmbeds(ModerationHelper.banUser(member, event.getMember(), event.getGuild(), days, false, reason, event.getChannel())).queue();
                }, throwable -> {
                    event.replyEmbeds(ModerationHelper.banUser(member, event.getMember(), event.getGuild(), days, false, reason, event.getChannel())).queue();
                    event.replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle("Error")
                                    .setDescription("Issue when kicking " + member.getAsMention() + ", couldn't remove timeout.")
                                    .build()
                    ).queue();
                });
            }
            default -> {
                event.reply("Invalid action ID %s.".formatted(actionId)).queue();
            }
        }
    }
}
