package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.geysermc.discordbot.http.Server;
import org.geysermc.discordbot.util.BotColors;

public class LeaderboardCommand extends SlashCommand {

    public LeaderboardCommand() {
        this.name = "leaderboard";
        this.help = "Sends a link to the leaderboard for the current guild";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getMessage().reply(getEmbed(event.getGuild())).queue();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.replyEmbeds(getEmbed(event.getGuild())).queue();
    }

    private MessageEmbed getEmbed(Guild guild) {
        return new EmbedBuilder()
                .setTitle("Leaderboard for " + guild.getName(), Server.getUrl(guild.getIdLong()))
                .setDescription("Click the above for the leaderboard")
                .setThumbnail(guild.getIconUrl())
                .setColor(BotColors.SUCCESS.getColor())
                .build();
    }
}
