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

package org.geysermc.discordbot;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.geysermc.discordbot.listeners.*;
import org.geysermc.discordbot.storage.AbstractStorageManager;
import org.geysermc.discordbot.storage.SlowModeInfo;
import org.geysermc.discordbot.storage.StorageType;
import org.geysermc.discordbot.tags.TagsListener;
import org.geysermc.discordbot.tags.TagsManager;
import org.geysermc.discordbot.updates.UpdateManager;
import org.geysermc.discordbot.util.BotHelpers;
import org.geysermc.discordbot.util.PropertiesManager;
import org.json.JSONArray;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.chew.chewbotcca.util.RestClient;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GeyserBot {
    // Instance Variables
    public static final Logger LOGGER = LoggerFactory.getLogger(GeyserBot.class);
    public static final List<Command> COMMANDS;

    public static AbstractStorageManager storageManager;

    private static ScheduledExecutorService generalThreadPool;

    private static JDA jda;
    private static GitHub github;

    static {
        // Gathers all commands from "commands" package.
        List<Command> commands = new ArrayList<>();
        try {
            Reflections reflections = new Reflections("org.geysermc.discordbot.commands");
            Set<Class<? extends Command>> subTypes = reflections.getSubTypesOf(Command.class);

            for (Class<? extends Command> theClass : subTypes) {
                // Don't load SubCommands
                if (theClass.getName().contains("SubCommand"))
                    continue;
                commands.add(theClass.getDeclaredConstructor().newInstance());
                LoggerFactory.getLogger(theClass).debug("Loaded Successfully!");
            }

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("Unable to load commands", e);
        }
        COMMANDS = commands;
    }

    public static void main(String[] args) throws IOException, LoginException {
        // Load properties into the PropertiesManager
        Properties prop = new Properties();
        prop.load(new FileInputStream("bot.properties"));
        PropertiesManager.loadProperties(prop);

        // Connect to github
        github = new GitHubBuilder().withOAuthToken(PropertiesManager.getGithubToken()).build();

        // Initialize the waiter
        EventWaiter waiter = new EventWaiter();

        // Load filters
        SwearHandler.loadFilters();

        // Load the db
        StorageType storageType = StorageType.getByName(PropertiesManager.getDatabaseType());
        if (storageType == StorageType.UNKNOWN) {
            LOGGER.error("Invalid database type! '" + PropertiesManager.getDatabaseType() + "'");
            System.exit(0);
        }

        try {
            storageManager = storageType.getStorageManager().newInstance();
            storageManager.setupStorage();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Unable to create database link!");
            System.exit(0);
        }

        // Setup the main client
        CommandClientBuilder client = new CommandClientBuilder();
        client.setActivity(null);
        client.setOwnerId("0"); // No owner
        client.setPrefix(PropertiesManager.getPrefix());
        client.useHelpBuilder(false);
        client.addCommands(COMMANDS.toArray(new Command[0]));
        client.setListener(new CommandErrorHandler());
        client.setCommandPreProcessFunction(event -> !SwearHandler.filteredMessages.contains(event.getMessage().getIdLong()));

        // Setup the tag client
        CommandClientBuilder tagClient = new CommandClientBuilder();
        tagClient.setActivity(null);
        tagClient.setOwnerId("0"); // No owner
        String tagPrefix = PropertiesManager.getPrefix() + PropertiesManager.getPrefix();
        tagClient.setPrefix(tagPrefix);
        tagClient.setPrefixes(new String[] {"!tag "});
        tagClient.useHelpBuilder(false);
        tagClient.addCommands(TagsManager.getTags().toArray(new Command[0]));
        tagClient.setListener(new TagsListener());
        tagClient.setCommandPreProcessFunction(event -> !SwearHandler.filteredMessages.contains(event.getMessage().getIdLong()));

        // Disable pings on replies
        MessageAction.setDefaultMentionRepliedUser(false);

        // Setup the thread pool
        generalThreadPool = Executors.newScheduledThreadPool(5);

        // Register JDA
        jda = JDABuilder.createDefault(PropertiesManager.getToken())
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .enableIntents(GatewayIntent.GUILD_PRESENCES)
            .enableCache(CacheFlag.ACTIVITY)
            .enableCache(CacheFlag.ROLE_TAGS)
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.playing("Booting..."))
            .addEventListeners(waiter,
                    new LogHandler(),
                    new SwearHandler(),
                    new PersistentRoleHandler(),
                    new FileHandler(),
                    new LevelHandler(),
                    new DumpHandler(),
                    new ErrorAnalyzer(),
                    client.build(),
                    tagClient.build())
            .build();

        // Register listeners
        jda.addEventListener();

        // Setup the update check scheduler
        UpdateManager.setup();

        // Setup all slow mode handlers
        generalThreadPool.schedule(() -> {
            for (Guild guild : jda.getGuilds()) {
                for (SlowModeInfo info : storageManager.getSlowModeChannels(guild)) {
                    jda.addEventListener(new SlowmodeHandler(info.getChannel(), info.getDelay()));
                }
            }
        }, 5, TimeUnit.SECONDS);

        // Start the bStats tracking thread
        generalThreadPool.scheduleAtFixedRate(() -> {
            JSONArray servers = new JSONArray(RestClient.get("https://bstats.org/api/v1/plugins/5273/charts/servers/data"));
            JSONArray players = new JSONArray(RestClient.get("https://bstats.org/api/v1/plugins/5273/charts/players/data"));
            int serverCount = servers.getJSONArray(servers.length() - 1).getInt(1);
            int playerCount = players.getJSONArray(players.length() - 1).getInt(1);
            jda.getPresence().setActivity(Activity.playing(BotHelpers.coolFormat(serverCount) + " servers, " + BotHelpers.coolFormat(playerCount) + " players"));
        }, 5, 60 * 5, TimeUnit.SECONDS);
    }

    public static JDA getJDA() {
        return jda;
    }

    public static GitHub getGithub() {
        return github;
    }

    public static ScheduledExecutorService getGeneralThreadPool() {
        return generalThreadPool;
    }

}
