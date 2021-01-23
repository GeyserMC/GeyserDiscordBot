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


import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
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
            Statement getPreferenceValue = connection.createStatement();
            ResultSet rs = getPreferenceValue.executeQuery("SELECT `role` FROM `persistent_roles` WHERE `server`=" + member.getGuild().getId() + " AND " + member.getId() + ";");

            while (rs.next()) {
                roles.add(member.getGuild().getRoleById(rs.getString("role")));
            }

            getPreferenceValue.close();
        } catch (SQLException ignored) { }

        return roles;
    }

    @Override
    public void addLog(Member user, String action, User target, String reason) {
        try {
            Statement addPersistentRole = connection.createStatement();
            addPersistentRole.executeUpdate("INSERT INTO `mod_log` (`server`, `time`, `user`, `action`, `target`, `reason`) VALUES (" + user.getGuild().getId() + ", " + Instant.now().getEpochSecond() + ", " + user.getId() + ", '" + action + "', " + target.getId() + ", '" + reason + "');");
            addPersistentRole.close();
        } catch (SQLException ignored) { }

    }
}
