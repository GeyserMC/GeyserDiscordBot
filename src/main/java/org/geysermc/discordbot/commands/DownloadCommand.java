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

import com.google.common.collect.ImmutableMap;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DownloadCommand extends SlashCommand {
    private final DownloadOption defaultDownloadOption;
    private final Map<String, DownloadOption> optionsToRepository;

    public DownloadCommand() {
        this.name = "download";
        this.arguments = "[program]";
        this.help = "Sends a link to download the latest version of Geyser or another program";
        this.guildOnly = false;

        this.defaultDownloadOption = new GeyserDownloadOption("Geyser", "A proxy which allows Bedrock Edition clients to join Java Edition servers.", "https://geysermc.org/download");
        this.optionsToRepository = ImmutableMap.<String, DownloadOption>builder()
                .put("geyser", this.defaultDownloadOption)
                .put("floodgate", new GeyserDownloadOption("Floodgate", "A plugin which allows Bedrock Edition clients to join Java edition servers without a Java Edition account.", "https://geysermc.org/download#floodgate"))
                .put("geyseroptionalpack", new GeyserDownloadOption("GeyserOptionalPack", "A Bedrock Edition resource pack which provides some fixes and parity changes to Geyser.", "https://geysermc.org/download/?project=other-projects&geyseroptionalpack=expanded"))
                .put("floodgate-modded", new GeyserDownloadOption("Floodgate Modded", "A mod which allows Bedrock Edition clients to join Java edition servers without a Java Edition account.", "https://modrinth.com/mod/floodgate"))
                .put("paper", new DownloadOption("Paper", "Paper is a server software based on Spigot with better performance and more modern features.", "https://papermc.io/downloads", "https://github.com/PaperMC.png"))
                .put("viaversion", new DownloadOption("ViaVersion", "ViaVersion is a plugin which allows modern clients to join older Java Edition servers.", "https://ci.viaversion.com/job/ViaVersion/", "https://github.com/ViaVersion.png"))
                .put("hurricane", new GeyserDownloadOption("Hurricane", "A Paper/Spigot plugin, and Fabric mod (unofficial port), that fixes some bugs that otherwise cannot be fixed without server modification.", "https://geysermc.org/wiki/other/hurricane#download"))
                .put("hydraulic", new GeyserDownloadOption("Hydraulic (Beta)", "A companion mod to Geyser which allows for Bedrock players to join modded Java Edition servers.", "https://geysermc.org/download?project=other-projects&hydraulic=expanded"))
                .put("rainbow", new GeyserDownloadOption("Rainbow (Beta)", "A Minecraft mod to generate Geyser item mappings and bedrock resourcepacks for use with Geyser's custom item API (v2). ", "https://geysermc.org/download?project=other-projects&rainbow=expanded"))
                .put("thunder", new GeyserDownloadOption("Thunder (Beta)", "A java application to convert simple Java Edition resource packs to Bedrock Edition ones.", "https://geysermc.org/download?project=other-projects&thunder=expanded"))
                .build();

        List<Command.Choice> choices = new ArrayList<>();
        for (Map.Entry<String, DownloadOption> entry: this.optionsToRepository.entrySet()) {
            choices.add(new Command.Choice(entry.getValue().friendlyName, entry.getKey()));
        }

        this.options = Collections.singletonList( // Future thing: add branch??
                new OptionData(OptionType.STRING, "program", "The program to download from.").addChoices(choices)
        );
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        String program = "geyser";
        if (!args.get(0).isEmpty()) {
            program = args.get(0);
        }

        DownloadOption downloadOption = optionsToRepository.getOrDefault(program.toLowerCase(Locale.ROOT), this.defaultDownloadOption);

        event.getMessage().replyComponents(getEmbedContainer(downloadOption))
                .useComponentsV2()
                .queue();
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String program = event.optString("program", "geyser");

        DownloadOption downloadOption = optionsToRepository.getOrDefault(program.toLowerCase(Locale.ROOT), this.defaultDownloadOption);

        event.replyComponents(getEmbedContainer(downloadOption))
                .useComponentsV2()
                .queue();
    }

    private Container getEmbedContainer(DownloadOption downloadOption) {
        return Container.of(
                Section.of(
                        Thumbnail.fromUrl(downloadOption.imageUrl),
                        TextDisplay.of("## " + downloadOption.friendlyName),
                        TextDisplay.of(downloadOption.description)
                ),
                Separator.createDivider(Separator.Spacing.LARGE),
                ActionRow.of(Button.of(ButtonStyle.LINK, downloadOption.downloadUrl, "Download"))
        ).withAccentColor(BotColors.SUCCESS.getColor());
    }

    private static class DownloadOption {
        private final String friendlyName;
        private final String description;
        private final String downloadUrl;
        private final String imageUrl;

        public DownloadOption(String friendlyName, String description, String downloadUrl, String imageUrl) {
            this.friendlyName = friendlyName;
            this.description = description;
            this.downloadUrl = downloadUrl;
            this.imageUrl = imageUrl;
        }
    }

    private static class GeyserDownloadOption extends DownloadOption {
        public GeyserDownloadOption(String friendlyName, String description, String downloadUrl) {
            super(friendlyName, description, downloadUrl, "https://github.com/GeyserMC.png");
        }
    }
}
