package org.geysermc.discordbot.commands.filter;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

import java.util.regex.Pattern;

public abstract class FilteredSlashCommand extends SlashCommand {
    @Override
    protected final void execute(SlashCommandEvent event) {
        Pattern filterPattern = null;
        for (OptionMapping option : event.getOptions()) {
            if (option.getType() != OptionType.STRING) continue;
            if ((filterPattern = SwearHandler.checkString(option.getAsString())) != null) break;
        }

        if (filterPattern != null) {
            event.reply(event.getUser().getAsMention() +
                    " your command cannot be processed because it contains profanity! Please read our rules for more information.")
                    .setEphemeral(true).queue();

            // Log the event
            if (event.getGuild() != null) {
                String channel = event.getChannel() == null ? "Unknown" : event.getChannel().getAsMention();

                ServerSettings.getLogChannel(event.getGuild()).sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Profanity blocked command")
                        .setDescription("**Sender:** " + event.getUser().getAsMention() + "\n" +
                                "**Channel:** " + channel + "\n" +
                                "**Regex:** `" + filterPattern + "`\n" +
                                "**Command:** " + event.getCommandString())
                        .setColor(BotColors.FAILURE.getColor())
                        .build()).queue();
            }
            return;
        }

        executeFiltered(event);
    }

    protected abstract void executeFiltered(SlashCommandEvent event);
}
