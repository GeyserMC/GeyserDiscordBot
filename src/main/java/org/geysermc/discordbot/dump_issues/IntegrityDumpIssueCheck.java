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
import pw.chew.chewbotcca.util.RestClient;

import java.util.ArrayList;
import java.util.List;

public class IntegrityDumpIssueCheck extends AbstractDumpIssueCheck {
    //https://ci.opencollab.dev/fingerprint/d614e47bdf2914bf8c037497c7733090/api/json


    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        List<String> issues = new ArrayList<>();

        // Make sure this is an official build
        if (!dump.getJSONObject("gitInfo").getString("git.build.host").equals("nukkitx.com")) {
            return issues;
        }

        String md5Hash = dump.getJSONObject("hashInfo").getString("md5Hash");
        String response = RestClient.simpleGetString("https://ci.opencollab.dev/fingerprint/" + md5Hash + "/api/json");

        // Check if 404
        if (response.startsWith("<html>")) {
            issues.add("- Your Geyser jar is corrupt or has been tampered with. Please redownload it [from the CI](https://ci.opencollab.dev/job/GeyserMC/job/Geyser/job/master/).");
        }

        return issues;
    }
}
