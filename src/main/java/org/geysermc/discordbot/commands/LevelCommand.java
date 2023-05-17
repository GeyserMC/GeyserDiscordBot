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
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LevelCommand extends SlashCommand {

    public LevelCommand() {
        this.name = "level";
        this.arguments = "[member]";
        this.help = "Show the level for a member";

        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "member", "The member you want to get the level for")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        Member member = event.optMember("member", event.getMember());

        // Defer to wait for us to load a response and allows for files to be uploaded
        InteractionHook interactionHook = event.deferReply().complete();

        File levelFile = handle(member);
        interactionHook.editOriginalAttachments(FileUpload.fromData(levelFile)).queue(message -> levelFile.delete());
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        Member member;
        if (args.size() == 0 || args.get(0).isEmpty()) {
            member = event.getMember();
        } else {
            member = BotHelpers.getMember(event.getGuild(), args.remove(0));
        }

        // Check user is valid
        if (member == null) {
            MessageHelper.errorResponse(event, "Invalid user", "The user ID specified doesn't link with any valid user in this server.");
            return;
        }

        File levelFile = handle(member);
        event.getMessage().replyFiles(FileUpload.fromData(levelFile)).queue(message -> levelFile.delete());
    }

    protected File handle(Member member) {
        // Get the user from the member
        User user = member.getUser();

        LevelInfo levelInfo = GeyserBot.storageManager.getLevel(member);

        try {
            XMLResourceDescriptor.setCSSParserClassName(InkscapeCssParser.class.getName());

            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

            Document doc = f.createDocument(LevelCommand.class.getClassLoader().getResource("assets/level.svg").toString());

            // Set the text for the svg fields
            doc.getElementById("level").getFirstChild().setTextContent(String.valueOf(levelInfo.getLevel()));
            doc.getElementById("name").getFirstChild().setTextContent(user.getName());
            doc.getElementById("discriminator").getFirstChild().setTextContent("#" + user.getDiscriminator());
            doc.getElementById("xp").getFirstChild().setTextContent(BotHelpers.coolFormat(levelInfo.getXp()));
            doc.getElementById("xpnext").getFirstChild().setTextContent(BotHelpers.coolFormat(levelInfo.getXpForNextLevel()));
            doc.getElementById("avatar").setAttributeNS("http://www.w3.org/1999/xlink", "href", user.getAvatarUrl().replace(".gif", ".png"));

            // Progress bar
            float progressWidth = Float.parseFloat(doc.getElementById("progressbg").getAttribute("width"));
            doc.getElementById("progress").setAttribute("width", String.valueOf(progressWidth * levelInfo.getLevelProgress()));

            TranscoderInput transcoderInput = new TranscoderInput(doc);

            // Set the output file
            File tempLevelFile = File.createTempFile("GeyserBot-Level-", ".png");
            OutputStream outputStream = new FileOutputStream(tempLevelFile);
            TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);

            // Convert the svg
            PNGTranscoder pngTranscoder = new PNGTranscoder();
            pngTranscoder.transcode(transcoderInput, transcoderOutput);

            // Close the output stream
            outputStream.flush();
            outputStream.close();

            // Send the message and delete the temp file
            return tempLevelFile;
        } catch (IOException | TranscoderException e) {
            e.printStackTrace();
        }

        return null;
    }
}
