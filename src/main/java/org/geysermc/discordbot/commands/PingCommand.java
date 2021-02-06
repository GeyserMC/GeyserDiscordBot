/*
 * Copyright (c) 2020-2021 GeyserMC. http://geysermc.org
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
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import net.dv8tion.jda.api.EmbedBuilder;
import org.geysermc.discordbot.listeners.SwearHandler;
import org.geysermc.discordbot.util.BotHelpers;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        this.name = "ping";
        this.aliases = new String[] {"status"};
        this.arguments = "<server>";
        this.help = "Ping a server to check if its accessible";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (SwearHandler.filteredMessages.contains(event.getMessage().getIdLong())) {
            return;
        }

        List<String> args = new ArrayList<>(Arrays.asList(event.getArgs().split(" ")));

        String[] ipParts = args.get(0).split(":");

        String hostname = ipParts[0];
        int jePort = 25565;
        int bePort = 19132;

        if (ipParts.length > 1) {
            jePort = Integer.parseInt(ipParts[1]);
            bePort = jePort;
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
                    "**Players:** " + data.getPlayers().getOnline() + "/" + data.getPlayers().getMax() + "\n" +
                    "**Version:** " + data.getVersion().getName() + " (" + data.getVersion().getProtocol() + ")";
            success = true;
        } catch (IOException ignored) { }

        try {
            InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", (int) (10000 + Math.round(Math.random() * 1000)));
            BedrockClient client = new BedrockClient(bindAddress);

            client.bind().join();

            InetSocketAddress addressToPing = new InetSocketAddress(hostname, bePort);
            BedrockPong pong = client.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            bedrockInfo = "**MOTD:** \n```\n" + MCPingUtil.stripColors(pong.getMotd()) + (pong.getSubMotd() != null ? "\n" + MCPingUtil.stripColors(pong.getSubMotd()) : "") + "\n```\n" +
                    "**Players:** " + pong.getPlayerCount() + "/" + pong.getMaximumPlayerCount() + "\n" +
                    "**Version:** " + BotHelpers.getBedrockVersionName(pong.getProtocolVersion()) + " (" + pong.getProtocolVersion() + ")";
            success = true;
        } catch (InterruptedException | ExecutionException ignored) { }

        event.getMessage().reply(new EmbedBuilder()
                .setTitle("Pinging server: " + args.get(0))
                .addField("Java", javaInfo, false)
                .addField("Bedrock", bedrockInfo, false)
                .setTimestamp(Instant.now())
                .setColor(success ? Color.green : Color.red)
                .build()).queue();
    }
}