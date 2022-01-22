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

import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.PropertiesManager;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteStorageManager extends MySQLStorageManager {

    @Override
    public void setupStorage() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + PropertiesManager.getDatabase());

            Statement createTables = connection.createStatement();
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, CONSTRAINT `pref_constraint` UNIQUE (`server`,`key`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `persistent_roles` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `user` INTEGER NOT NULL, `role` INTEGER NOT NULL, CONSTRAINT `role_constraint` UNIQUE (`server`,`user`,`role`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `mod_log` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `time` INTEGER NOT NULL, `user` INTEGER NOT NULL, `action` VARCHAR(32) NOT NULL, `target` INTEGER NOT NULL, `reason` TEXT NOT NULL);");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `levels` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `server` INTEGER NOT NULL, `user` INTEGER NOT NULL, `level` INT NOT NULL, `xp` INT NOT NULL, `messages` INT NOT NULL, CONSTRAINT `level_constraint` UNIQUE (`server`,`user`));");
            createTables.executeUpdate("CREATE TABLE IF NOT EXISTS `slow_mode` (`channel` INTEGER NOT NULL PRIMARY KEY, `server` INTEGER NOT NULL, `delay` INT NOT NULL);");
            createTables.close();
        } catch (ClassNotFoundException | SQLException e) {
            GeyserBot.LOGGER.error("Unable to create sqlite database!", e);
        }
    }
}
