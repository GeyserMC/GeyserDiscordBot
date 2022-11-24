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

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import br.com.azalim.mcserverping.MCPingUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PingCommand extends SlashCommand {

    public PingCommand() {
        this.name = "ping";
        this.aliases = new String[] { "status" };
        this.arguments = "<server>";
        this.help = "Ping a server to check if its accessible";
        this.guildOnly = false;

        this.options = Collections.singletonList(
            new OptionData(OptionType.STRING, "server", "The IP Address of the server you want to ping").setRequired(true)
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        // Defer to wait for us to load a response and allows for files to be uploaded
        InteractionHook interactionHook = event.deferReply().complete();

        String ip = event.getOption("server").getAsString();

        interactionHook.editOriginalEmbeds(handle(ip)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Check they specified an ip
        if (args.get(0).isEmpty()) {
            MessageHelper.errorResponse(event, "Missing IP", "Please specify an IP to ping.");
            return;
        }

        event.getMessage().replyEmbeds(handle(args.get(0))).queue();
    }

    private MessageEmbed handle(String ip) {
        // Check we were given a valid IP/domain
        if (!ip.matches("[\\w.\\-:]+")) {
            return MessageHelper.errorResponse(null, "IP invalid", "The given IP appears to be invalid and won't be queried. If you believe this is incorrect please contact an admin.");
        }

        // Make sure the IP is not longer than 128 characters
        if (ip.length() > 128) {
            return MessageHelper.errorResponse(null, "IP too long", "Search query is over the max allowed character count of 128 (" + ip.length() + ")");
        }

        // If the ip is in url form remove that
        if (ip.startsWith("http")) {
            ip = ip.replaceAll("https?://", "").split("/")[0];
        }

        String[] ipParts = ip.split(":");

        String hostname = ipParts[0];

        if (NetworkUtils.isInternalIP(hostname)) {
            return MessageHelper.errorResponse(null, "IP invalid", "The given IP appears to be an internal address and won't be queried.");
        }

        int jePort = 25565;
        int bePort = 19132;

        if (ipParts.length > 1) {
            try {
                jePort = Integer.parseInt(ipParts[1]);
                bePort = jePort;
            } catch (NumberFormatException ignored) {
                return MessageHelper.errorResponse(null, "Invalid port", "The port you specified is not a valid number.");
            }
        }

        if (jePort < 1 || jePort > 65535) {
            return MessageHelper.errorResponse(null, "Invalid port", "The port you specified is not a valid number.");
        }

        String javaInfo = "Unable to find Java server at the requested address";
        String bedrockInfo = "Unable to find Bedrock server at the requested address";
        boolean success = false;

        try {
            MCPingOptions options = MCPingOptions.builder()
                    .hostname(hostname)
                    .port(jePort)
                    .timeout(1500)
                    .build();

            MCPingResponse data = MCPing.getPing(options);

            javaInfo = "**MOTD:** \n```\n" + data.getDescription().getStrippedText() + "\n```\n" +
                    "**Players:** " + (data.getPlayers() == null ? "Unknown" : data.getPlayers().getOnline() + "/" + data.getPlayers().getMax()) + "\n" +
                    "**Version:** " + data.getVersion().getName() + " (" + data.getVersion().getProtocol() + ")";
            success = true;
        } catch (IOException ignored) {
        }

        BedrockClient client = null;
        try {
            InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
            client = new BedrockClient(bindAddress);

            client.bind().join();

            InetSocketAddress addressToPing = new InetSocketAddress(hostname, bePort);
            BedrockPong pong = client.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            bedrockInfo = "**MOTD:** \n```\n" + MCPingUtil.stripColors(pong.getMotd()) + (pong.getSubMotd() != null ? "\n" + MCPingUtil.stripColors(pong.getSubMotd()) : "") + "\n```\n" +
                    "**Players:** " + pong.getPlayerCount() + "/" + pong.getMaximumPlayerCount() + "\n" +
                    "**Version:** " + pong.getVersion() + " (" + pong.getProtocolVersion() + ")";
            success = true;
        } catch (InterruptedException | ExecutionException ignored) {
        } finally {
            if (client != null) {
                client.close();
            }
        }

        return new EmbedBuilder()
                .setTitle("Pinging server: " + ip)
                .addField("Java", javaInfo, false)
                .addField("Bedrock", bedrockInfo, false)
                .setTimestamp(Instant.now())
                .setColor(success ? BotColors.SUCCESS.getColor() : BotColors.FAILURE.getColor())
                .build();
    }
}
