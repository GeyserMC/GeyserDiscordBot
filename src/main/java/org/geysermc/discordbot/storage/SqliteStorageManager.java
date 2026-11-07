/*
 * Copyright (c) 2020-2026 GeyserMC. http://geysermc.org
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
import org.geysermc.discordbot.util.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteStorageManager extends AbstractStorageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteStorageManager.class);

    protected Connection connection;

    @Override
    public void setupStorage() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + PropertiesManager.getDatabase());

        try (Statement createTables = connection.createStatement()) {
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, CONSTRAINT `pref_constraint` UNIQUE (`server`,`key`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `persistent_roles` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `user` INTEGER NOT NULL, `role` INTEGER NOT NULL, CONSTRAINT `role_constraint` UNIQUE (`server`,`user`,`role`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `mod_log` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `time` INTEGER NOT NULL, `user` INTEGER NOT NULL, `action` VARCHAR(32) NOT NULL, `target` INTEGER NOT NULL, `reason` TEXT NOT NULL);");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `levels` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `user` INTEGER NOT NULL, `level` INT NOT NULL, `xp` INT NOT NULL, `messages` INT NOT NULL, CONSTRAINT `level_constraint` UNIQUE (`server`,`user`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `slow_mode` (`channel` INTEGER NOT NULL PRIMARY KEY, `server` INTEGER NOT NULL, `delay` INT NOT NULL);");
        }
    }

    @Override
    public void closeStorage() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Failed to close the database connection", e);
        }
    }

    @Override
    public String getServerPreference(long serverID, String preference) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT `value` FROM `preferences` WHERE `server` = ? AND `key` = ?")) {
            statement.setLong(1, serverID);
            statement.setString(2, preference);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get server preference {} for server {}", preference, serverID, e);
        }
        return null;
    }

    @Override
    public void setServerPreference(long serverID, String preference, String value) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `preferences` (`server`, `key`, `value`) VALUES (?, ?, ?)")) {
            statement.setLong(1, serverID);
            statement.setString(2, preference);
            statement.setString(3, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to set server preference {} for server {}", preference, serverID, e);
        }
    }

    @Override
    public void addPersistentRole(Member member, Role role) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `persistent_roles` (`server`, `user`, `role`) VALUES (?, ?, ?)")) {
            statement.setLong(1, member.getGuild().getIdLong());
            statement.setLong(2, member.getIdLong());
            statement.setLong(3, role.getIdLong());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to add persistent role {} for user {} in server {}", role.getIdLong(), member.getIdLong(), member.getGuild().getIdLong(), e);
        }
    }

    @Override
    public void removePersistentRole(Member member, Role role) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM `persistent_roles` WHERE `server` = ? AND `user` = ? AND `role` = ?")) {
            statement.setLong(1, member.getGuild().getIdLong());
            statement.setLong(2, member.getIdLong());
            statement.setLong(3, role.getIdLong());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to remove persistent role {} for user {} in server {}", role.getIdLong(), member.getIdLong(), member.getGuild().getIdLong(), e);
        }
    }

    @Override
    public List<Role> getPersistentRoles(Member member) {
        List<Role> roles = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT `role` FROM `persistent_roles` WHERE `server` = ? AND `user` = ?")) {
            statement.setLong(1, member.getGuild().getIdLong());
            statement.setLong(2, member.getIdLong());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Role role = member.getGuild().getRoleById(rs.getLong(1));
                    // Skip roles that have since been deleted from the guild
                    if (role != null) {
                        roles.add(role);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get persistent roles for user {} in server {}", member.getIdLong(), member.getGuild().getIdLong(), e);
        }

        return roles;
    }

    @Override
    public int addLog(Member user, String action, UserSnowflake target, String reason) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO `mod_log` (`server`, `time`, `user`, `action`, `target`, `reason`) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            long time = Instant.now().getEpochSecond();

            statement.setLong(1, user.getGuild().getIdLong());
            statement.setLong(2, time);
            statement.setLong(3, user.getIdLong());
            statement.setString(4, action);
            statement.setLong(5, target.getIdLong());
            statement.setString(6, reason);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to add mod log ({}) for target {} in server {}", action, target.getIdLong(), user.getGuild().getIdLong(), e);
        }

        return -1;
    }

    @Override
    public List<ModLog> getLogs(Guild guild, UserSnowflake target, int limit) {
        List<ModLog> logs = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT `id`, `time`, `user`, `action`, `reason` FROM `mod_log` WHERE `server` = ? AND `target` = ? ORDER BY `time`, `id` LIMIT ?")) {
            statement.setLong(1, guild.getIdLong());
            statement.setLong(2, target.getIdLong());
            statement.setInt(3, limit);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Instant time = Instant.ofEpochSecond(rs.getLong(2));
                    Member user = guild.getMemberById(rs.getLong(3));

                    logs.add(new ModLog(rs.getInt(1), time, user, rs.getString(4), target, rs.getString(5)));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get mod logs for target {} in server {}", target.getIdLong(), guild.getIdLong(), e);
        }

        return logs;
    }

    @Override
    public ModLog getLog(Guild guild, int id) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM `mod_log` WHERE `server` = ? AND `id` = ?")) {
            statement.setLong(1, guild.getIdLong());
            statement.setInt(2, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Instant time = Instant.ofEpochSecond(rs.getLong("time"));
                Member user = guild.getMemberById(rs.getLong("user"));
                UserSnowflake target = guild.getJDA().getUserById(rs.getLong("target"));

                // Construct a user from the id
                if (target == null) {
                    target = User.fromId(rs.getLong("target"));
                }

                return new ModLog(rs.getInt("id"), time, user, rs.getString("action"), target, rs.getString("reason"));
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get mod log {} in server {}", id, guild.getIdLong(), e);
        }

        return null;
    }

    @Override
    public void updateLog(Guild guild, int id, String reason) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE `mod_log` SET `reason` = ? WHERE `id` = ? AND `server` = ?")) {
            statement.setString(1, reason);
            statement.setInt(2, id);
            statement.setLong(3, guild.getIdLong());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to update mod log {} in server {}", id, guild.getIdLong(), e);
        }
    }

    @Override
    public LevelInfo getLevel(Member user) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT `level`, `xp`, `messages` FROM `levels` WHERE `server` = ? AND `user` = ?")) {
            statement.setLong(1, user.getGuild().getIdLong());
            statement.setLong(2, user.getIdLong());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new LevelInfo(user.getIdLong(), rs.getInt("level"), rs.getInt("xp"), rs.getInt("messages"));
                }
            }

            return new LevelInfo(user.getIdLong(), 0, 0, 0);
        } catch (SQLException e) {
            LOGGER.error("Failed to get level for user {} in server {}", user.getIdLong(), user.getGuild().getIdLong(), e);
        }

        return null;
    }

    @Override
    public void setLevel(Member user, LevelInfo levelInfo) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `levels` (`server`, `user`, `level`, `xp`, `messages`) VALUES (?, ?, ?, ?, ?)")) {
            statement.setLong(1, user.getGuild().getIdLong());
            statement.setLong(2, user.getIdLong());
            statement.setInt(3, levelInfo.getLevel());
            statement.setInt(4, levelInfo.getXp());
            statement.setInt(5, levelInfo.getMessages());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to set level for user {} in server {}", user.getIdLong(), user.getGuild().getIdLong(), e);
        }
    }

    @Override
    public List<LevelInfo> getLevels(long guild) {
        List<LevelInfo> levels = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT `user`, `level`, `xp`, `messages` FROM `levels` WHERE `server` = ? ORDER BY `xp` DESC LIMIT 100")) {
            statement.setLong(1, guild);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    levels.add(new LevelInfo(rs.getLong(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get levels for server {}", guild, e);
        }

        return levels;
    }

    @Override
    public List<SlowModeInfo> getSlowModeChannels(Guild guild) {
        List<SlowModeInfo> infos = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT `channel`, `server`, `delay` FROM `slow_mode` WHERE `server` = ?")) {
            statement.setLong(1, guild.getIdLong());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    infos.add(new SlowModeInfo(rs.getLong(2), rs.getLong(1), rs.getInt(3)));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get slow mode channels for server {}", guild.getIdLong(), e);
        }

        return infos;
    }

    @Override
    public void setSlowModeChannel(TextChannel channel, int delay) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `slow_mode` (`channel`, `server`, `delay`) VALUES (?, ?, ?)")) {
            statement.setLong(1, channel.getIdLong());
            statement.setLong(2, channel.getGuild().getIdLong());
            statement.setInt(3, delay);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to set slow mode for channel {} in server {}", channel.getIdLong(), channel.getGuild().getIdLong(), e);
        }
    }
}
