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


import org.geysermc.discordbot.GeyserBot;
import org.geysermc.discordbot.util.PropertiesManager;

import java.sql.*;

public class MySQLStorageManager extends AbstractStorageManager {

    private Connection connection;

    @Override
    public void setupStorage() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + PropertiesManager.getHost() + "/" + PropertiesManager.getDatabase(), PropertiesManager.getUser(), PropertiesManager.getPass());

            Statement createPreferencesTable = connection.createStatement();
            createPreferencesTable.executeUpdate("CREATE TABLE IF NOT EXISTS `preferences` (`id` INT NOT NULL AUTO_INCREMENT, `server` INT NOT NULL, `key` VARCHAR(32), `value` TEXT NOT NULL, PRIMARY KEY(`id`));");
            createPreferencesTable.close();
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
    String getServerPreference(int serverID, String preference) {
        return null;
    }

    @Override
    void setServerPreference(int serverID, String preference, String value) {

    }
}
