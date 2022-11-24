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

import org.apache.commons.net.util.SubnetUtils;

public class NetworkUtils {

    private static final SubnetUtils.SubnetInfo[] INTERNAL_IP_RANGES = new SubnetUtils.SubnetInfo[] {
            new SubnetUtils("0.0.0.0/8").getInfo(),
            new SubnetUtils("10.0.0.0/8").getInfo(),
            new SubnetUtils("100.64.0.0/10").getInfo(),
            new SubnetUtils("127.0.0.0/8").getInfo(),
            new SubnetUtils("169.254.0.0/16").getInfo(),
            new SubnetUtils("172.16.0.0/12").getInfo(),
            new SubnetUtils("192.0.0.0/24").getInfo(),
            new SubnetUtils("192.0.2.0/24").getInfo(),
            new SubnetUtils("192.88.99.0/24").getInfo(),
            new SubnetUtils("192.168.0.0/16").getInfo(),
            new SubnetUtils("198.18.0.0/15").getInfo(),
            new SubnetUtils("198.51.100.0/24").getInfo(),
            new SubnetUtils("203.0.113.0/24").getInfo(),
            new SubnetUtils("224.0.0.0/4").getInfo(),
            new SubnetUtils("240.0.0.0/4").getInfo(),
            new SubnetUtils("255.255.255.255/32").getInfo()
    };

    /**
     * Check if an IP is internal/reserved as defined by the IETF and IANA
     * https://en.wikipedia.org/wiki/Reserved_IP_addresses
     *
     * @param address IP address to check
     * @return True if the IP is internal/reserved
     */
    public static boolean isInternalIP(String address) {
        if ("localhost".equalsIgnoreCase(address)) {
            return true;
        }

        try {
            for (SubnetUtils.SubnetInfo subnetInfo : INTERNAL_IP_RANGES) {
                if (subnetInfo.isInRange(address) || subnetInfo.getAddress().equals(address)) {
                    return true;
                }
            }
        } catch (IllegalArgumentException ignored) { } // If we get this then its likely a domain

        return false;
    }
}
