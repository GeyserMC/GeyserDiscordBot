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

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import br.com.azalim.mcserverping.MCPingResponse;
import br.com.azalim.mcserverping.MCPingUtil;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.geysermc.discordbot.commands.filter.FilteredSlashCommand;
import org.geysermc.discordbot.util.BotColors;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.MessageHelper;
import org.geysermc.discordbot.util.NetworkUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PingCommand extends FilteredSlashCommand {
    private static final int TIMEOUT = 1250; // in ms, has to stay below 1500 (1.5s for each platform, total of 3s)

    public PingCommand() {
        this.name = "ping";
        this.aliases = new String[] { "status" };
        this.arguments = "<ip> [port]";
        this.help = "Ping a server to check if its accessible";
        this.guildOnly = false;

        this.options = List.of(
            new OptionData(OptionType.STRING, "ip", "The IP Address of the server you want to ping", true),
            new OptionData(OptionType.INTEGER, "port", "The port of the server you want to ping", false)
                    .setMinValue(1)
                    .setMaxValue(65535)
        );
    }

    @Override
    protected void executeFiltered(SlashCommandEvent event) {
        // Defer to wait for us to load a response and allows for files to be uploaded
        InteractionHook interactionHook = event.deferReply().complete();

        String ip = event.getOption("ip").getAsString();
        String portString = event.getOption("port") != null ? event.getOption("port").getAsString() : null;
        Integer port = portString != null ? Integer.parseInt(portString.replaceAll("[^0-9]", "")) : null;

        interactionHook.editOriginalEmbeds(handle(ip, port)).queue();
    }

    @Override
    protected void execute(CommandEvent event) {
        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        // Check they specified an ip
        if (args.get(0).isEmpty()) {
            MessageHelper.errorResponse(event, "Missing IP", "Please specify an IP to ping.");
            return;
        }

        String ip = args.get(0);
        Integer port = args.size() > 1 ? Integer.parseInt(args.get(1).replaceAll("[^0-9]", "")) : null;

        event.getMessage().replyEmbeds(handle(ip, port)).queue();
    }

    private MessageEmbed handle(String ip, Integer port) {
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

        if (NetworkUtils.isInternalIP(ip)) {
            return MessageHelper.errorResponse(null, "IP invalid", "The given IP appears to be an internal address and won't be queried.");
        }

        int jePort = 25565;
        int bePort = 19132;

        if (port != null) {
            jePort = port;
            bePort = jePort;
        }

        if (jePort < 1 || jePort > 65535) {
            return MessageHelper.errorResponse(null, "Invalid port", "The port you specified is not a valid number.");
        }

        String javaInfo = "Unable to find Java server at the requested address";
        String bedrockInfo = "Unable to find Bedrock server at the requested address";
        boolean success = false;

        try {
            MCPingOptions options = MCPingOptions.builder()
                    .hostname(ip)
                    .port(jePort)
                    .timeout(TIMEOUT)
                    .build();

            MCPingResponse data = MCPing.getPing(options);

            javaInfo = "**MOTD:** \n```\n" + BotHelpers.trim(data.getDescription().getStrippedText(), 100) + "\n```\n" +
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

            InetSocketAddress addressToPing = new InetSocketAddress(ip, bePort);
            BedrockPong pong = client.ping(addressToPing, TIMEOUT, TimeUnit.MILLISECONDS).get();

            bedrockInfo = "**MOTD:** \n```\n" + BotHelpers.trim(MCPingUtil.stripColors(pong.getMotd()), 100) + (pong.getSubMotd() != null ? "\n" + BotHelpers.trim(MCPingUtil.stripColors(pong.getSubMotd()), 100) : "") + "\n```\n" +
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
                .setTitle("Pinging server " + ip)
                .addField("Java (" + jePort + ")", javaInfo, false)
                .addField("Bedrock (" + bePort + ")", bedrockInfo, false)
                .setColor(success ? BotColors.SUCCESS.getColor() : BotColors.FAILURE.getColor())
                .build();
    }
}
