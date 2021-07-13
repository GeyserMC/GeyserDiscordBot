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

import org.geysermc.discordbot.GeyserBot;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GithubFileFinder {
    private static final List<String> REPOS = new ArrayList<String>()  {
        {
            add("GeyserMC/Geyser");
            add("GeyserMC/Floodgate");
            add("GeyserMC/Geyser-Fabric");
            add("GeyserMC/GeyserConnect");
            add("GeyserMC/Cumulus");
            add("GeyserMC/geyser-adapters");
        }
    };

    private final String branch;

    private List<GHTree> trees;

    public GithubFileFinder() {
        this.branch = "master";
        setup();
    }

    public GithubFileFinder(String branch) {
        this.branch = branch;
        setup();
    }

    private void setup() {
        trees = new ArrayList<>();

        // Loop the repos and load in the trees for them
        for (String repo : REPOS) {
            try {
                GHRepository ghRepo = GeyserBot.getGithub().getRepository(repo);

                try {
                    trees.add(ghRepo.getTreeRecursive(branch, 1));
                } catch (GHFileNotFoundException e) {
                    // Fallback to default branch
                    try {
                        trees.add(ghRepo.getTreeRecursive(ghRepo.getDefaultBranch(), 1));
                    } catch (IOException ignored) { }
                }
            } catch (IOException ignored) { }
        }
    }

    public String getFileUrl(String file) {
        return getFileUrl(file, -1);
    }

    public String getFileUrl(String file, int line) {
        // Find the entry
        GHTreeEntry foundEntry = null;
        for (GHTree tree : trees) {
            Optional<GHTreeEntry> entry = tree.getTree().stream().filter(ghTreeEntry -> ghTreeEntry.getPath().endsWith("/" + file) || ghTreeEntry.getPath().equals(file)).findFirst();
            if (entry.isPresent()) {
                foundEntry = entry.get();
                break;
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
