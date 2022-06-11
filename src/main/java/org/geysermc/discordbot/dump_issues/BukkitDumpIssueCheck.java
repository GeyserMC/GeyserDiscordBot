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

package org.geysermc.discordbot.dump_issues;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class BukkitDumpIssueCheck extends AbstractDumpIssueCheck {

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject bootstrapInfo = dump.getJSONObject("bootstrapInfo");
        String platformName = bootstrapInfo.getString("platformName");

        if (platformName.equals("CraftBukkit") && !bootstrapInfo.getString("platformVersion").contains("Spigot")) {
            return Collections.singletonList("- Your server is running on CraftBukkit; please switch over to Spigot or Paper (run `!!bukkit` in <#613194762249437245> for more information).");
        }

        return Collections.emptyList();
    }
}
