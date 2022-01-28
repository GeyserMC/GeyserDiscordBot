package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.geysermc.discordbot.http.Server;
import org.geysermc.discordbot.storage.ServerSettings;
import org.geysermc.discordbot.util.BotColors;

public class LeaderboardCommand extends SlashCommand {

    public LeaderboardCommand() {
        this.name = "leaderboard";
        this.help = "Sends a link to the leaderboard for the current server";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().replyEmbeds(getEmbed(event.getGuild())).queue();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(getEmbed(event.getGuild())).queue();
    }

    private MessageEmbed getEmbed(Guild guild) {
        if (ServerSettings.serverLevelsDisabled(guild)) {
            return new EmbedBuilder()
                    .setTitle("Levels disabled")
                    .setDescription("Levels are disabled for this server!")
                    .setColor(BotColors.FAILURE.getColor())
                    .build();
        }

        return new EmbedBuilder()
                .setTitle("Leaderboard for " + guild.getName(), Server.getUrl(guild.getIdLong()))
                .setDescription("Click the above for the leaderboard")
                .setThumbnail(guild.getIconUrl())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}
