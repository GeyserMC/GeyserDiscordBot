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

package org.geysermc.discordbot.storage;


import net.dv8tion.jda.api.entities.*;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.PropertiesManager;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MySQLStorageManager extends AbstractStorageManager {

    private Connection connection;

    @Override
    public void setupStorage() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + PropertiesManager.getHost() + "/" + PropertiesManager.getDatabase(), PropertiesManager.getUser(), PropertiesManager.getPass());

            Statement createTables = connection.createStatement();
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `pref_constraint` (`server`,`key`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `persistent_roles` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `role` BIGINT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `role_constraint` (`server`,`user`,`role`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `mod_log` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `time` BIGINT NOT NULL, `user` BIGINT NOT NULL, `action` VARCHAR(32) NOT NULL, `target` BIGINT NOT NULL, `reason` TEXT NOT NULL, PRIMARY KEY(`id`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `levels` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `level` INT NOT NULL, `xp` INT NOT NULL, `messages` INT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `level_constraint` (`server`,`user`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `slow_mode` (`channel` BIGINT NOT NULL, `server` BIGINT NOT NULL, `delay` INT NOT NULL, PRIMARY KEY(`channel`));");
            createTables.close();
        } catch (ClassNotFoundException | SQLException e) {
            GeyserBot.LOGGER.error("Unable to connect to MySQL database!", e);
        }
    }

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public String getServerPreference(long serverID, String preference) {
        try {
            Statement getPreferenceValue = connection.createStatement();
            ResultSet rs = getPreferenceValue.executeQuery("SELECT `value` FROM `preferences` WHERE `server`=" + serverID + " AND `key`='" + preference + "';");

            if (rs.next()) {
                return rs.getString("value");
            }

            getPreferenceValue.close();
        } catch (SQLException ignored) { }

        return null;
    }

    @Override
    public void setServerPreference(long serverID, String preference, String value) {
        try {
            Statement updatePreferenceValue = connection.createStatement();
            updatePreferenceValue.executeUpdate("INSERT INTO `preferences` (`server`, `key`, `value`) VALUES (" + serverID + ", '" + preference + "', '" + value + "') ON DUPLICATE KEY UPDATE `value`='" + value + "';");
            updatePreferenceValue.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public void addPersistentRole(Member member, Role role) {
        try {
            Statement addPersistentRole = connection.createStatement();
            addPersistentRole.executeUpdate("INSERT INTO `persistent_roles` (`server`, `user`, `role`) VALUES (" + member.getGuild().getId() + ", " + member.getId() + ", " + role.getId() + ");");
            addPersistentRole.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public void removePersistentRole(Member member, Role role) {
        try {
            Statement removePersistentRole = connection.createStatement();
            removePersistentRole.executeUpdate("DELETE FROM `persistent_roles` WHERE `server`=" + member.getGuild().getId() + " AND `user`=" + member.getId() + " AND `role`=" + role.getId() + ");");
            removePersistentRole.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public List<Role> getPersistentRoles(Member member) {
        List<Role> roles = new ArrayList<>();

        try {
            Statement getPersistentRoles = connection.createStatement();
            ResultSet rs = getPersistentRoles.executeQuery("SELECT `role` FROM `persistent_roles` WHERE `server`=" + member.getGuild().getId() + " AND `user`=" + member.getId() + ";");

            while (rs.next()) {
                roles.add(member.getGuild().getRoleById(rs.getString("role")));
            }

            getPersistentRoles.close();
        } catch (SQLException ignored) { }

        return roles;
    }

    @Override
    public void addLog(Member user, String action, User target, String reason) {
        try {
            Statement addLogEntry = connection.createStatement();
            addLogEntry.executeUpdate("INSERT INTO `mod_log` (`server`, `time`, `user`, `action`, `target`, `reason`) VALUES (" + user.getGuild().getId() + ", " + Instant.now().getEpochSecond() + ", " + user.getId() + ", '" + action + "', " + target.getId() + ", '" + reason + "');");
            addLogEntry.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public List<ModLog> getLog(Guild guild, User target, int limit) {
        List<ModLog> logs = new ArrayList<>();

        try {
            Statement getLogEntry = connection.createStatement();
            ResultSet rs = getLogEntry.executeQuery("SELECT `time`, `user`, `action`, `reason` FROM `mod_log` WHERE `server`=" + guild.getId() + " AND `target`=" + target.getId() + " LIMIT " + limit + ";");

            while (rs.next()) {
                Instant time = Instant.ofEpochSecond(rs.getLong("time"));
                Member user = guild.getMemberById(rs.getLong("user"));

                logs.add(new ModLog(time, user, rs.getString("action"), target, rs.getString("reason")));
            }

            getLogEntry.close();
        } catch (SQLException ignored) { }

        return logs;
    }

    @Override
    public LevelInfo getLevel(Member user) {
        try {
            Statement getLevelValue = connection.createStatement();
            ResultSet rs = getLevelValue.executeQuery("SELECT `level`, `xp`, `messages` FROM `levels` WHERE `server`=" + user.getGuild().getId() + " AND `user`=" + user.getId() + ";");

            if (rs.next()) {
                return new LevelInfo(rs.getInt("level"), rs.getInt("xp"), rs.getInt("messages"));
            }

            getLevelValue.close();

            return new LevelInfo(0, 0, 0);
        } catch (SQLException ignored) { }

        return null;
    }

    @Override
    public void setLevel(Member user, LevelInfo levelInfo) {
        try {
            Statement updateLevelValue = connection.createStatement();
            updateLevelValue.executeUpdate("INSERT INTO `levels` (`server`, `user`, `level`, `xp`, `messages`) VALUES (" + user.getGuild().getId() + ", " + user.getId() + ", " + levelInfo.getLevel() + ", " + levelInfo.getXp() + ", " + levelInfo.getMessages() + ") ON DUPLICATE KEY UPDATE `level`=" + levelInfo.getLevel() + ", `xp`=" + levelInfo.getXp() + ", `messages`=" + levelInfo.getMessages() + ";");
            updateLevelValue.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public List<SlowModeInfo> getSlowModeChannels(Guild guild) {
        List<SlowModeInfo> infos = new ArrayList<>();

        try {
            Statement getLogEntry = connection.createStatement();
            ResultSet rs = getLogEntry.executeQuery("SELECT `channel`, `server`, `delay` FROM `slow_mode` WHERE `server`=" + guild.getId() + ";");

            while (rs.next()) {
                infos.add(new SlowModeInfo(rs.getLong("server"), rs.getLong("channel"), rs.getInt("delay")));
            }

            getLogEntry.close();
        } catch (SQLException ignored) { }

        return infos;
    }

    @Override
    public void setSlowModeChannel(TextChannel channel, int delay) {
        try {
            Statement updateLevelValue = connection.createStatement();
            updateLevelValue.executeUpdate("INSERT INTO `slow_mode` (`channel`, `server`, `delay`) VALUES (" + channel.getId() + ", " + channel.getGuild().getId() + ", " + delay + ") ON DUPLICATE KEY UPDATE `delay`=" + delay + ";");
            updateLevelValue.close();
        } catch (SQLException ignored) { }
    }
}
