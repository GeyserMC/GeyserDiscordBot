/*
 * Copyright (c) 2020-2023 GeyserMC. http://geysermc.org
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

package org.geysermc.discordbot.commands.moderation;

import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.geysermc.discordbot.commands.moderation.ForumPostCommand.filterPotentialTags;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForumPostTagsTest {

    private static final String[] ALL_TAGS = new String[] {
        "Unable To Connect",
        "Error On Startup",
        "Gameplay Error",
        "Other/Misc.",
        "Spigot/Paper",
        "Fabric",
        "BungeeCord",
        "Velocity",
        "Standalone",
        "Answered",
        "Resource Packs"
    };

    private List<ForumTag> tags;

    @BeforeEach
    public void beforeEach() {
        tags = tagsOf(ALL_TAGS);
    }

    // If any of these tests fail because the actual list is longer, consider instead using
    // assertTrue(tags.containsAll(tagsOf(...)))

    @Test
    public void testQueryStart() {
        filterPotentialTags(tags, "");
        assertEquals(tagsOf(ALL_TAGS), tags);
    }

    @Test
    public void testBasic() {
        filterPotentialTags(tags, "stand");
        assertEquals(tagsOf("Standalone"), tags);
    }

    @Test
    public void testExtraChars() {
        filterPotentialTags(tags, "velocityPOWERed");
        assertEquals(tagsOf("Velocity"), tags);
    }

    @Test
    public void testSuffix() {
        filterPotentialTags(tags, "Paper");
        assertEquals(tagsOf("Spigot/Paper"), tags);
    }

    @Test
    public void testError() {
        filterPotentialTags(tags, "Error");
        assertEquals(tagsOf("Error On Startup", "Gameplay Error"), tags);
    }

    @Test
    public void testGibberish() {
        filterPotentialTags(tags, "khtfNkkghbvgjn");
        assertEquals(tagsOf(), tags);
    }

    private static List<ForumTag> tagsOf(String... names) {
        return Arrays.stream(names)
            .map(FakeTag::new)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private record FakeTag(String name) implements ForumTag {

        @Override
        public int getPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getIdLong() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isModerated() {
            return false;
        }

        @Nullable
        @Override
        public EmojiUnion getEmoji() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ForumTag)) return false;
            FakeTag fakeTag = (FakeTag) o;
            return name.equals(fakeTag.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}