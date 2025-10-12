/*
 * Copyright (c) 2020-2024 GeyserMC. http://geysermc.org
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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.commands.filter.FilteredSlashCommand;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;

import java.util.List;

public class NewReleaseCommand extends FilteredSlashCommand {

    // Enforces: 1.<two digits, no leading 0>[.<one or two digits, no leading 0>]?
    private static final String VERSION_REGEX = "^1\\.[1-9][0-9](\\.[1-9][0-9]?)?$";

    public NewReleaseCommand() {
        this.name = "newrelease";
        this.arguments = "<version> <preview> <viaversion>";
        this.help = "Spam this when Geyser doesn’t support a new release, or if it works with the preview/viabackwards";
        this.guildOnly = false;

        this.options = List.of(
                new OptionData(OptionType.STRING, "version", "The latest Java release (e.g. 1.21.9 or 1.21.9/1.21.10)", true),
                new OptionData(OptionType.BOOLEAN, "preview", "Has the Geyser Preview released for this version?", true),
                new OptionData(OptionType.BOOLEAN, "viaversion", "Has ViaVersion + ViaBackwards updated for this version?", true)
        );
    }

    @Override
    protected void executeFiltered(SlashCommandEvent event) {
        String version = event.getOption("version").getAsString();
        boolean preview = event.getOption("preview").getAsBoolean();
        boolean viaversion = event.getOption("viaversion").getAsBoolean();

        if (!isValidVersion(version)) {
            event.replyEmbeds(MessageHelper.errorResponse(null, "Invalid version format",
                    "The version must be `1.XX` or `1.XX.X` (e.g. 1.21 or 1.21.9), or a pair like `1.21.9/1.21.10`. Leading zeros are not allowed."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(handle(version, preview, viaversion)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split(" ");
        if (args.length < 3) {
            MessageHelper.errorResponse(event, "Invalid usage",
                    "Usage: `" + event.getPrefix() + name + " <version or version/version+1> <preview:true/false> <viaversion:true/false>`");
            return;
        }

        String version = args[0];
        if (!isValidVersion(version)) {
            MessageHelper.errorResponse(event, "Invalid version format",
                    "The version must be `1.XX` or `1.XX.X` (e.g. 1.21 or 1.21.9), or a pair like `1.21.9/1.21.10`. Leading zeros are not allowed.");
            return;
        }

        boolean preview = Boolean.parseBoolean(args[1]);
        boolean viaversion = Boolean.parseBoolean(args[2]);

        event.getMessage().replyEmbeds(handle(version, preview, viaversion)).queue();
    }

    private boolean isValidVersion(String version) {
        // Single version
        if (version.matches(VERSION_REGEX)) {
            return true;
        }

        // Pair of versions separated by "/"
        if (version.contains("/")) {
            String[] parts = version.split("/");
            if (parts.length != 2) return false;

            String v1 = parts[0];
            String v2 = parts[1];

            if (!v1.matches(VERSION_REGEX) || !v2.matches(VERSION_REGEX)) {
                return false;
            }

            // Parse into segments
            String[] seg1 = v1.split("\\.");
            String[] seg2 = v2.split("\\.");

            // Must have patch segment
            if (seg1.length != 3 || seg2.length != 3) return false;

            // Major and minor must match
            if (!seg1[0].equals(seg2[0]) || !seg1[1].equals(seg2[1])) return false;

            try {
                int patch1 = Integer.parseInt(seg1[2]);
                int patch2 = Integer.parseInt(seg2[2]);
                return patch2 == patch1 + 1;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    private MessageEmbed handle(String version, boolean preview, boolean viaversion) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Geyser Support for " + version);

        String message;

        if (!preview && !viaversion) {
            message = "Geyser does not currently support " + version + ", please wait patiently!";
        } else if (preview && !viaversion) {
            message = "You can use the Geyser Preview at <#1230530815918866453> to support " + version + ".\n" +
                    "On aternos, to get the preview, install \"GeyserMC Preview\" from the plugins or mods tab.";
        } else if (preview && viaversion) {
            message = "You can use the Geyser Preview at <#1230530815918866453> to support " + version + ".\n" +
                    "On aternos, to get the preview, install \"GeyserMC Preview\" from the plugins or mods tab.\n\n" +
                    "Alternatively, you can use ViaVersion + ViaBackwards on release Geyser builds, but keep in mind, " +
                    "due to how ViaBackwards works, " + version + "'s features will show with hacky workarounds on Bedrock. " +
                    "For example, copper golems appeared as a frog named \"Copper Golem\" before Geyser's 1.21.9 release.";
        } else { // only viaversion true
            message = "You can use ViaVersion + ViaBackwards to support " + version + ", but keep in mind, " +
                    "due to how ViaBackwards works, " + version + "'s features will show with hacky workarounds on Bedrock. " +
                    "For example, copper golems appeared as a frog named \"Copper Golem\" before Geyser's 1.21.9 release.";
        }

        embed.setDescription(message);
        embed.setColor(BotColors.SUCCESS.getColor());
        return embed.build();
    }
}
