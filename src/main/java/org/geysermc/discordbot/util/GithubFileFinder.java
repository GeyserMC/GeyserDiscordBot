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

package org.geysermc.discordbot.util;

import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.Optional;

public class GithubFileFinder {
    private final String branch;

    private GitHub github;
    private GHTree geyserTree;
    private GHTree floodgateTree;

    public GithubFileFinder() {
        this.branch = "master";
        setup();
    }

    public GithubFileFinder(String branch) {
        this.branch = branch;
        setup();
    }

    private void setup() {
        github = null;
        geyserTree = null;
        floodgateTree = null;
        try {
            github = GitHub.connect();
            geyserTree = github.getRepository("GeyserMC/Geyser").getTreeRecursive(branch, 1);
        } catch (IOException e) { }

        try {
            floodgateTree = github.getRepository("GeyserMC/Floodgate").getTreeRecursive(branch, 1);
        } catch (IOException e) { }
    }

    public String getFileUrl(String file) {
        return getFileUrl(file, -1);
    }

    public String getFileUrl(String file, int line) {
        // Find the entry
        GHTreeEntry foundEntry = null;
        if (geyserTree != null) {
            Optional<GHTreeEntry> entry = geyserTree.getTree().stream().filter(ghTreeEntry -> ghTreeEntry.getPath().endsWith("/" + file) || ghTreeEntry.getPath().equals(file)).findFirst();
            if (entry.isPresent()) {
                foundEntry = entry.get();
            }
        }
        if (floodgateTree != null && foundEntry != null) {
            Optional<GHTreeEntry> entry = floodgateTree.getTree().stream().filter(ghTreeEntry -> ghTreeEntry.getPath().endsWith("/" + file) || ghTreeEntry.getPath().equals(file)).findFirst();
            if (entry.isPresent()) {
                foundEntry = entry.get();
            }
        }

        // Build the url from the entry
        String lineUrl = "";
        if (foundEntry != null) {
            lineUrl = foundEntry.getUrl().toString()
                    .replace("https://api.github.com/repos/", "https://github.com/")
                    .replaceFirst("git/blobs/.*", "blob/" + branch + "/" + foundEntry.getPath());

            if (line != -1) {
                lineUrl += "#L" + line;
            }
        }

        return lineUrl;
    }
}
