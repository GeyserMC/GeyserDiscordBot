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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.storage.LevelInfo;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.InkscapeCssParser;
import org.geysermc.discordbot.util.MessageHelper;
import org.w3c.dom.Document;
import pw.chew.jdachewtils.command.OptionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LMGTFYCommand extends SlashCommand {

    public LMGTFYCommand() {
        this.name = "lmgtfy";
        this.arguments = "[search]";
        this.help = "Generate an LMGTFY link";

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "Search", "The query you want to input into lmgtfy", true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        String arg = event.getOption("Search").getAsString().replaceAll("[^\\s^\\w]", "").replaceAll("\\s", "+");
        event.reply("".equals(arg) || arg == null ? "Your search query is invalid!" : "https://lmgtfy.app/?q=" + arg);
    }

    @Override
    protected void execute(CommandEvent event) {
        String arg = event.getArgs().replaceAll("[^\\s^\\w]", "").replaceAll("\\s", "%20");
        event.reply("".equals(arg) || arg == null ? "Your search query is invalid!" : "https://lmgtfy.app/?q=" + arg);
    }
}
