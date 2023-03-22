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

package org.geysermc.discordbot.storage;


import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.PropertiesManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MySQLStorageManager extends AbstractStorageManager {

    protected Connection connection;

    @Override
    public void setupStorage() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        connection = DriverManager.getConnection("jdbc:mysql://" + PropertiesManager.getHost() + "/" + PropertiesManager.getDatabase(), PropertiesManager.getUser(), PropertiesManager.getPass());

        Statement createTables = connection.createStatement();
        createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `pref_constraint` (`server`,`key`));");
        createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `persistent_roles` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `role` BIGINT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `role_constraint` (`server`,`user`,`role`));");
        createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `mod_log` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `time` BIGINT NOT NULL, `user` BIGINT NOT NULL, `action` VARCHAR(32) NOT NULL, `target` BIGINT NOT NULL, `reason` TEXT NOT NULL, PRIMARY KEY(`id`));");
        createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `levels` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `level` INT NOT NULL, `xp` INT NOT NULL, `messages` INT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `level_constraint` (`server`,`user`));");
        createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `slow_mode` (`channel` BIGINT NOT NULL, `server` BIGINT NOT NULL, `delay` INT NOT NULL, PRIMARY KEY(`channel`));");
        createTables.close();
    }

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException ignored) { }
    }

    private void checkConnection() {
        try {
            if (connection.isValid(0)) return;
        } catch (SQLException e) { }

        try {
            setupStorage();
        } catch (Exception e) {
            GeyserBot.LOGGER.error("Failed to reconnect to MySQL database", e);
        }
    }

    @Override
    public String getServerPreference(long serverID, String preference) {
        checkConnection();
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
        checkConnection();
        try {
            Statement updatePreferenceValue = connection.createStatement();
            updatePreferenceValue.executeUpdate("INSERT INTO `preferences` (`server`, `key`, `value`) VALUES (" + serverID + ", '" + preference + "', '" + value + "') ON DUPLICATE KEY UPDATE `value`='" + value + "';");
            updatePreferenceValue.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public void addPersistentRole(Member member, Role role) {
        checkConnection();
        try {
            Statement addPersistentRole = connection.createStatement();
            addPersistentRole.executeUpdate("INSERT INTO `persistent_roles` (`server`, `user`, `role`) VALUES (" + member.getGuild().getId() + ", " + member.getId() + ", " + role.getId() + ");");
            addPersistentRole.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public void removePersistentRole(Member member, Role role) {
        checkConnection();
        try {
            Statement removePersistentRole = connection.createStatement();
            removePersistentRole.executeUpdate("DELETE FROM `persistent_roles` WHERE `server`=" + member.getGuild().getId() + " AND `user`=" + member.getId() + " AND `role`=" + role.getId() + ");");
            removePersistentRole.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public List<Role> getPersistentRoles(Member member) {
        checkConnection();
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
    public int addLog(Member user, String action, User target, String reason) {
        checkConnection();
        try {
            Statement addLogEntry = connection.createStatement();
            long time = Instant.now().getEpochSecond();
            addLogEntry.executeUpdate("INSERT INTO `mod_log` (`server`, `time`, `user`, `action`, `target`, `reason`) VALUES (" + user.getGuild().getId() + ", " + time + ", " + user.getId() + ", '" + action + "', " + target.getId() + ", '" + reason + "');");
            addLogEntry.close();

            Statement getLogEntry = connection.createStatement();
            ResultSet rs = getLogEntry.executeQuery("SELECT `id` FROM `mod_log` WHERE `server`=" + user.getGuild().getId() + " AND `time`=" + time + " AND `user`=" + user.getId() + " AND `action`='" + action + "' AND `target`=" + target.getId() + " AND `reason`='" + reason + "' LIMIT 1;");

            if (rs.next()) {
                return rs.getInt("id");
            }

            getLogEntry.close();
        } catch (SQLException ignored) { }

        return -1;
    }

    @Override
    public List<ModLog> getLogs(Guild guild, User target, int limit) {
        checkConnection();
        List<ModLog> logs = new ArrayList<>();

        try {
            Statement getLogEntry = connection.createStatement();
            ResultSet rs = getLogEntry.executeQuery("SELECT `id`, `time`, `user`, `action`, `reason` FROM `mod_log` WHERE `server`=" + guild.getId() + " AND `target`=" + target.getId() + " ORDER BY `time` ASC LIMIT " + limit + ";");

            while (rs.next()) {
                Instant time = Instant.ofEpochSecond(rs.getLong("time"));
                Member user = guild.getMemberById(rs.getLong("user"));

                logs.add(new ModLog(rs.getInt("id"), time, user, rs.getString("action"), target, rs.getString("reason")));
            }

            getLogEntry.close();
        } catch (SQLException ignored) { }

        return logs;
    }

    @Override
    public ModLog getLog(Guild guild, int id) {
        checkConnection();
        try {
            Statement getLogEntry = connection.createStatement();
            ResultSet rs = getLogEntry.executeQuery("SELECT `id`, `time`, `user`, `action`, `target`, `reason` FROM `mod_log` WHERE `server`=" + guild.getId() + " AND `id`=" + id + ";");

            if (rs.next()) {
                Instant time = Instant.ofEpochSecond(rs.getLong("time"));
                Member user = guild.getMemberById(rs.getLong("user"));
                UserSnowflake target = guild.getJDA().getUserById(rs.getLong("target"));

                // Construct a user from the id
                if (target == null) {
                    target = User.fromId(rs.getLong("target"));
                }

                return new ModLog(rs.getInt("id"), time, user, rs.getString("action"), target, rs.getString("reason"));
            }

            getLogEntry.close();
        } catch (SQLException ignored) { }

        return null;
    }

    @Override
    public void updateLog(Guild guild, int id, String reason) {
        checkConnection();
        try {
            Statement updateLevelValue = connection.createStatement();
            updateLevelValue.executeUpdate("UPDATE `mod_log` SET `reason`='" + reason + "' WHERE `id`=" + id + ";");
            updateLevelValue.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public LevelInfo getLevel(Member user) {
        checkConnection();
        try {
            Statement getLevelValue = connection.createStatement();
            ResultSet rs = getLevelValue.executeQuery("SELECT `level`, `xp`, `messages` FROM `levels` WHERE `server`=" + user.getGuild().getId() + " AND `user`=" + user.getId() + ";");

            if (rs.next()) {
                return new LevelInfo(user.getIdLong(), rs.getInt("level"), rs.getInt("xp"), rs.getInt("messages"));
            }

            getLevelValue.close();

            return new LevelInfo(0, 0, 0, 0);
        } catch (SQLException ignored) { }

        return null;
    }

    @Override
    public void setLevel(Member user, LevelInfo levelInfo) {
        checkConnection();
        try {
            Statement updateLevelValue = connection.createStatement();
            updateLevelValue.executeUpdate("INSERT INTO `levels` (`server`, `user`, `level`, `xp`, `messages`) VALUES (" + user.getGuild().getId() + ", " + user.getId() + ", " + levelInfo.getLevel() + ", " + levelInfo.getXp() + ", " + levelInfo.getMessages() + ") ON DUPLICATE KEY UPDATE `level`=" + levelInfo.getLevel() + ", `xp`=" + levelInfo.getXp() + ", `messages`=" + levelInfo.getMessages() + ";");
            updateLevelValue.close();
        } catch (SQLException ignored) { }
    }

    @Override
    public List<LevelInfo> getLevels(long guild) {
        checkConnection();
        List<LevelInfo> levels = new ArrayList<>();
        try {
            Statement getLevelValue = connection.createStatement();
            ResultSet rs = getLevelValue.executeQuery("SELECT `user`, `level`, `xp`, `messages` FROM `levels` WHERE `server`=" + guild + " ORDER BY xp DESC LIMIT 100;");

            while (rs.next()) {
                levels.add(new LevelInfo(rs.getLong("user"), rs.getInt("level"), rs.getInt("xp"), rs.getInt("messages")));
            }

            getLevelValue.close();
        } catch (SQLException ignored) { }

        return levels;
    }

    @Override
    public List<SlowModeInfo> getSlowModeChannels(Guild guild) {
        checkConnection();
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
        checkConnection();
        try {
            Statement updateLevelValue = connection.createStatement();
            updateLevelValue.executeUpdate("INSERT INTO `slow_mode` (`channel`, `server`, `delay`) VALUES (" + channel.getId() + ", " + channel.getGuild().getId() + ", " + delay + ") ON DUPLICATE KEY UPDATE `delay`=" + delay + ";");
            updateLevelValue.close();
        } catch (SQLException ignored) { }
    }
}
