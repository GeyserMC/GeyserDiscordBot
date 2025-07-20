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

public enum StorageType {
    SQLITE("sqlite", SqliteStorageManager.class),
    MYSQL("mysql", MySQLStorageManager.class),
    UNKNOWN("unknown", AbstractStorageManager.class);

    private final String name;

    private final Class<? extends AbstractStorageManager> storageManager;

    StorageType(String name, Class<? extends AbstractStorageManager> storageManager) {
        this.name = name;
        this.storageManager = storageManager;
    }

    public static final StorageType[] VALUES = values();

    /**
     * Convert the StorageType string (from properties) to the enum, UNKNOWN on fail
     *
     * @param name StorageType string
     *
     * @return The converted StorageType
     */
    public static StorageType getByName(String name) {
        String upperCase = name.toUpperCase();
        for (StorageType type : VALUES) {
            if (type.name().equals(upperCase)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    public Class<? extends AbstractStorageManager> getStorageManager() {
        return storageManager;
    }
}
