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


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.geysermc.discordbot.util.PropertiesManager;

public class MySQLStorageManager extends AbstractStorageManager {

    protected HikariDataSource dataSource;

    @Override
    public void setupStorage() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + PropertiesManager.getHost() + "/" + PropertiesManager.getDatabase());
        config.setUsername(PropertiesManager.getUser());
        config.setPassword(PropertiesManager.getPass());

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement createTables = connection.createStatement()) {
                createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `pref_constraint` (`server`,`key`));");
                createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `persistent_roles` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `role` BIGINT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `role_constraint` (`server`,`user`,`role`));");
                createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `mod_log` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `time` BIGINT NOT NULL, `user` BIGINT NOT NULL, `action` VARCHAR(32) NOT NULL, `target` BIGINT NOT NULL, `reason` TEXT NOT NULL, PRIMARY KEY(`id`));");
                createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `levels` (`id` INT NOT NULL AUTO_INCREMENT, `server` BIGINT NOT NULL, `user` BIGINT NOT NULL, `level` INT NOT NULL, `xp` INT NOT NULL, `messages` INT NOT NULL, PRIMARY KEY(`id`), UNIQUE KEY `level_constraint` (`server`,`user`));");
                createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `slow_mode` (`channel` BIGINT NOT NULL, `server` BIGINT NOT NULL, `delay` INT NOT NULL, PRIMARY KEY(`channel`));");
            }
        }
    }

    @Override
    public void closeStorage() {
        dataSource.close();
    }

    @Override
    public String getServerPreference(long serverID, String preference) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT value FROM preferences WHERE server = ? AND `key` = ?")) {
                statement.setLong(1, serverID);
                statement.setString(2, preference);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString(1);
                    }
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    @Override
    public void setServerPreference(long serverID, String preference, String value) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO preferences (server, key, value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
                statement.setLong(1, serverID);
                statement.setString(2, preference);
                statement.setString(3, value);
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    @Override
    public void addPersistentRole(Member member, Role role) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO persistent_roles (server, user, role) VALUES (?, ?, ?)")) {
                statement.setLong(1, member.getGuild().getIdLong());
                statement.setLong(2, member.getIdLong());
                statement.setLong(3, role.getIdLong());
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    @Override
    public void removePersistentRole(Member member, Role role) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM persistent_roles WHERE server = ? AND user = ? AND role = ?")) {
                statement.setLong(1, member.getGuild().getIdLong());
                statement.setLong(2, member.getIdLong());
                statement.setLong(3, role.getIdLong());
                statement.executeUpdate();
            }
        } catch (SQLException ignored) { }
    }

    @Override
    public List<Role> getPersistentRoles(Member member) {
        List<Role> roles = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT role FROM persistent_roles WHERE server = ? AND user = ?")) {
                statement.setLong(1, member.getGuild().getIdLong());
                statement.setLong(2, member.getIdLong());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        roles.add(member.getGuild().getRoleById(rs.getString(1)));
                    }
                }
            }
        } catch (SQLException ignored) {}

        return roles;
    }

    @Override
    public int addLog(Member user, String action, UserSnowflake target, String reason) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO mod_log (server, time, user, action, target, reason) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
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
            }
        } catch (SQLException ignored) {}

        return -1;
    }

    @Override
    public List<ModLog> getLogs(Guild guild, UserSnowflake target, int limit) {
        List<ModLog> logs = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT id, time, user, action, reason FROM mod_log WHERE server = ? AND target = ? ORDER BY time LIMIT ?")) {
                statement.setLong(1, guild.getIdLong());
                statement.setLong(2, target.getIdLong());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        Instant time = Instant.ofEpochSecond(rs.getLong(2));
                        Member user = guild.getMemberById(rs.getLong(3));

                        logs.add(new ModLog(rs.getInt(1), time, user, rs.getString(4), target, rs.getString(5)));
                    }
                }
            }
        } catch (SQLException ignored) {}

        return logs;
    }

    @Override
    public ModLog getLog(Guild guild, int id) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM mod_log WHERE server = ? AND id = ?")) {
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
            }
        } catch (SQLException ignored) {}

        return null;
    }

    @Override
    public void updateLog(Guild guild, int id, String reason) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE mod_log SET reason = ? WHERE id = ?")) {
                statement.setString(1, reason);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    @Override
    public LevelInfo getLevel(Member user) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT level, xp, messages FROM levels WHERE server = ? AND user = ?")) {
                statement.setLong(1, user.getGuild().getIdLong());
                statement.setLong(2, user.getIdLong());

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return new LevelInfo(user.getIdLong(), rs.getInt("level"), rs.getInt("xp"), rs.getInt("messages"));
                    }
                }

                return new LevelInfo(0, 0, 0, 0);
            }
        } catch (SQLException ignored) {}

        return null;
    }

    @Override
    public void setLevel(Member user, LevelInfo levelInfo) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO levels (server, user, level, xp, messages) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level), xp = VALUES(xp), messages = VALUES(messages)")) {
                statement.setLong(1, user.getGuild().getIdLong());
                statement.setLong(2, user.getIdLong());
                statement.setInt(3, levelInfo.getLevel());
                statement.setInt(4, levelInfo.getXp());
                statement.setInt(5, levelInfo.getMessages());
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    @Override
    public List<LevelInfo> getLevels(long guild) {
        List<LevelInfo> levels = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT user, level, xp, messages FROM levels WHERE server = ? ORDER BY xp DESC LIMIT 100")) {
                statement.setLong(1, guild);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        levels.add(new LevelInfo(rs.getLong(1), rs.getInt(2), rs.getInt(3), rs.getInt(4)));
                    }
                }
            }
        } catch (SQLException ignored) {}

        return levels;
    }

    @Override
    public List<SlowModeInfo> getSlowModeChannels(Guild guild) {
        List<SlowModeInfo> infos = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT channel, server, delay FROM slow_mode WHERE server = ?")) {
                statement.setLong(1, guild.getIdLong());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        infos.add(new SlowModeInfo(rs.getLong(2), rs.getLong(1), rs.getInt(3)));
                    }
                }
            }
        } catch (SQLException ignored) {}

        return infos;
    }

    @Override
    public void setSlowModeChannel(TextChannel channel, int delay) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO slow_mode (channel, server, delay) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE delay = VALUES(delay)")) {
                statement.setLong(1, channel.getIdLong());
                statement.setLong(2, channel.getGuild().getIdLong());
                statement.setInt(3, delay);
                statement.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }
}
