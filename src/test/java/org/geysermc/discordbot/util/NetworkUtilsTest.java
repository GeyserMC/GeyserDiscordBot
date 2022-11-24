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

package org.geysermc.discordbot.util;

import org.junit.jupiter.api.Test;

import static org.geysermc.discordbot.util.NetworkUtils.isInternalIP;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NetworkUtilsTest {

    private static final String[] INTERNAL_ADDRESSES = {"0.0.0.0", "localhost", "127.0.0.1", "192.168.1.7", "10.0.0.8"};
    private static final String[] PUBLIC_ADDRESSES = {"1.1.1.1", "link.geysermc.org", "mc.hypixel.net", "123.456.789.101"};

    @Test
    public void testLocalAddresses() {
        for (String address : INTERNAL_ADDRESSES) {
            assertTrue(isInternalIP(address), address + " should be detected as internal IP");
        }
    }

    @Test
    public void testPublicAddresses() {
        for (String address : PUBLIC_ADDRESSES) {
            assertFalse(isInternalIP(address), address + " should be detected as public IP");
        }
    }
}
