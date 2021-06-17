package org.geysermc.discordbot.commands;

import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.PropertiesManager;

import java.util.*;

public class DownloadCommand extends SlashCommand {
    private final DownloadOption defaultDownloadOption;
    private final Map<String, DownloadOption> optionsToRepository;

    public DownloadCommand() {
        this.name = "download";
        this.help = "Sends a link to download the latest version of Geyser or another program";
        this.guildOnly = false;

        this.defaultDownloadOption = new GeyserDownloadOption("Geyser", "https://ci.opencollab.dev/job/GeyserMC/job/Geyser/job/master/");
        this.optionsToRepository = ImmutableMap.of(
                "geyser", this.defaultDownloadOption,
                "floodgate", new GeyserDownloadOption("Floodgate", "https://ci.opencollab.dev/job/GeyserMC/job/Floodgate/job/master/"),
                "geyser-fabric", new FabricDownloadOption("Geyser-Fabric", "https://ci.opencollab.dev/job/GeyserMC/job/Geyser-Fabric/job/java-1.17/"),
                "floodgate-fabric", new FabricDownloadOption("Floodgate-Fabric", "https://ci.opencollab.dev/job/GeyserMC/job/Floodgate-Fabric/job/master/"),
                "paper", new DownloadOption("Paper", "https://papermc.io/downloads", "https://avatars.githubusercontent.com/u/7608950"));

        List<Command.Choice> choices = new ArrayList<>();
        for (String option : this.optionsToRepository.keySet()) {
            choices.add(new Command.Choice(option, option));
        }

        this.options = Collections.singletonList( // Future thing: add branch??
                new OptionData(OptionType.STRING, "program", "The program to download from.")
                        .setRequired(false)
                        .addChoices(choices)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String program = event.getOptions().size() > 0 ? event.getOptions().get(0).getAsString() : "geyser";

        DownloadOption downloadOption = optionsToRepository.getOrDefault(program.toLowerCase(Locale.ROOT), this.defaultDownloadOption);

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Download " + downloadOption.friendlyName)
                .setDescription("Download at " + downloadOption.downloadUrl)
                .setThumbnail(downloadOption.imageUrl)
                .setColor(PropertiesManager.getDefaultColor())
                .build()).queue();
    }

    private static class DownloadOption {
        private final String friendlyName;
        private final String downloadUrl;
        private final String imageUrl;

        public DownloadOption(String friendlyName, String downloadUrl, String imageUrl) {
            this.friendlyName = friendlyName;
            this.downloadUrl = downloadUrl;
            this.imageUrl = imageUrl;
        }
    }

    private static class GeyserDownloadOption extends DownloadOption {
        public GeyserDownloadOption(String friendlyName, String downloadUrl) {
            super(friendlyName, downloadUrl, "https://avatars.githubusercontent.com/u/52673035");
        }
    }

    private static class FabricDownloadOption extends DownloadOption {
        public FabricDownloadOption(String friendlyName, String downloadUrl) {
            super(friendlyName, downloadUrl, "https://avatars.githubusercontent.com/u/21025855");
        }
    }
}
