package org.geysermc.discordbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;

/**
 * Handle the help command
 */
public class HelpCommand extends Command {

    public HelpCommand() {
        this.name = "help";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        // TODO: Add a command registration system instead of hardcoding
        EmbedBuilder helpEmbed = new EmbedBuilder()
            .setColor(Color.decode("#00FF00"))
            .setTitle("Geyser Bot Help");

        helpEmbed.addField("`!help`", "I think you already know what this does", true);
        helpEmbed.addField("`!wiki <search>`", "Search the Geyser wiki", true);
        helpEmbed.addField("`!provider <provider>`", "Search the Supported Providers page on the Geyser wiki to see if a provider is supported", true);
        helpEmbed.addField("`!tags`", "List all the known (non-alias) tags", true);
        helpEmbed.addField("`!tag <name>`", "Display a tag for the given name", true);

        event.reply(helpEmbed.build());
    }
}
