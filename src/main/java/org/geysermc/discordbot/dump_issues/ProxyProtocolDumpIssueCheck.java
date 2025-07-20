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

import java.util.ArrayList;
import java.util.List;

public class ProxyProtocolDumpIssueCheck extends AbstractDumpIssueCheck {

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject configRemote = dump.getJSONObject("config").getJSONObject("remote");
        JSONObject configBedrock = dump.getJSONObject("config").getJSONObject("bedrock");

        List<String> warnings = new ArrayList<>();

        if (configBedrock.getBoolean("enable-proxy-protocol")) {
            warnings.add("- `enable-proxy-protocol` should ONLY be enabled if you run a reverse UDP proxy in front of Geyser.");
        }
        if (configRemote.getBoolean("use-proxy-protocol")) {
            warnings.add("- `use-proxy-protocol` should ONLY be enabled if either of these apply:\n\u00A0\u00A0\u00A0\u00A01. Your server supports PROXY protocol (this has nothing to do with if you're using BungeeCord or Velocity).\n\u00A0\u00A0\u00A0\u00A02. You have the exact same option enabled in your BungeeCord/Velocity config (it is off by default).");
        }

        return warnings;
    }
}
