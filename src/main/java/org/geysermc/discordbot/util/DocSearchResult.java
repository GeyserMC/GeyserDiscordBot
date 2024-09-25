/*
 * Copyright (c) 2020-2024 GeyserMC. http://geysermc.org
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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a search result from the Geyser documentation.
 */
public class DocSearchResult {
    @JsonProperty("version")
    private List<String> version;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("url")
    private String url;

    @JsonProperty("content")
    private String content;
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("hierarchy")
    private Hierarchy hierarchy;

    @JsonProperty("objectID")
    private String objectID;

    @JsonProperty("_snippetResult")
    private SnippetResult _snippetResult;

    @JsonProperty("_highlightResult")
    private HighlightResult _highlightResult;

    /**
     * Default constructor for Jackson deserialization.
     */
    public DocSearchResult() {}

    /**
     * Gets the version of the result.
     * 
     * @return The version of the result.
     */
    public List<String> getVersion() {
        return version;
    }

    /**
     * Sets the version of the result.
     * 
     * @param version The version of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setVersion(List<String> version) {
        this.version = version;
        return this;
    }

    /**
     * Gets the tags of the result.
     * 
     * @return The tags of the result.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags of the result.
     * 
     * @param tags The tags of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Gets the URL of the result.
     * 
     * @return The URL of the result.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the result.
     * 
     * @param url The URL of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets the content of the result.
     * 
     * @return The content of the result.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the result.
     * 
     * @param content The content of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Gets the type of the result.
     * 
     * @return The type of the result.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the result.
     * 
     * @param type The type of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the hierarchy of the result.
     * 
     * @return The hierarchy of the result.
     */
    public Hierarchy getHierarchy() {
        return hierarchy;
    }

    /**
     * Sets the hierarchy of the result.
     * 
     * @param hierarchy The hierarchy of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setHierarchy(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
        return this;
    }

    /**
     * Gets the object ID of the result.
     * 
     * @return The object ID of the result.
     */
    public String getObjectID() {
        return objectID;
    }

    /**
     * Sets the object ID of the result.
     * 
     * @param objectID The object ID of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult setObjectID(String objectID) {
        this.objectID = objectID;
        return this;
    }

    /**
     * Gets the snippet result of the result.
     * 
     * @return The snippet result of the result.
     */
    public SnippetResult get_snippetResult() {
        return _snippetResult;
    }

    /**
     * Sets the snippet result of the result.
     * 
     * @param _snippetResult The snippet result of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult set_snippetResult(SnippetResult _snippetResult) {
        this._snippetResult = _snippetResult;
        return this;
    }

    /**
     * Gets the highlight result of the result.
     * 
     * @return The highlight result of the result.
     */
    public HighlightResult get_highlightResult() {
        return _highlightResult;
    }

    /**
     * Sets the highlight result of the result.
     * 
     * @param _highlightResult The highlight result of the result.
     * @return The current instance of the result.
     */
    public DocSearchResult set_highlightResult(HighlightResult _highlightResult) {
        this._highlightResult = _highlightResult;
        return this;
    }

    /**
     * Represents the hierarchy of a search result.
     */
    public static class Hierarchy {
        @JsonProperty("lvl0")
        private String lvl0;

        @JsonProperty("lvl1")
        private String lvl1;

        @JsonProperty("lvl2")
        private String lvl2;

        @JsonProperty("lvl3")
        private String lvl3;

        @JsonProperty("lvl4")
        private String lvl4;

        @JsonProperty("lvl5")
        private String lvl5;

        @JsonProperty("lvl6")
        private String lvl6;

        /**
         * Default constructor for Jackson deserialization.
         */
        public Hierarchy() {}

        /**
         * Gets the zeroth level of the hierarchy.
         * 
         * @return The zeroth level of the hierarchy.
         */
        public String getLvl0() {
            return lvl0;
        }

        /**
         * Sets the zeroth level of the hierarchy.
         * 
         * @param lvl0 The zeroth level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl0(String lvl0) {
            this.lvl0 = lvl0;
            return this;
        }

        /**
         * Gets the first level of the hierarchy.
         * 
         * @return The first level of the hierarchy.
         */
        public String getLvl1() {
            return lvl1;
        }

        /**
         * Sets the first level of the hierarchy.
         * 
         * @param lvl1 The first level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl1(String lvl1) {
            this.lvl1 = lvl1;
            return this;
        }

        /**
         * Gets the second level of the hierarchy.
         * 
         * @return The second level of the hierarchy.
         */
        public String getLvl2() {
            return lvl2;
        }

        /**
         * Sets the second level of the hierarchy.
         * 
         * @param lvl2 The second level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl2(String lvl2) {
            this.lvl2 = lvl2;
            return this;
        }

        /**
         * Gets the third level of the hierarchy.
         * 
         * @return The third level of the hierarchy.
         */
        public String getLvl3() {
            return lvl3;
        }

        /**
         * Sets the third level of the hierarchy.
         * 
         * @param lvl3 The third level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl3(String lvl3) {
            this.lvl3 = lvl3;
            return this;
        }

        /**
         * Gets the fourth level of the hierarchy.
         * 
         * @return The fourth level of the hierarchy.
         */
        public String getLvl4() {
            return lvl4;
        }

        /**
         * Sets the fourth level of the hierarchy.
         * 
         * @param lvl4 The fourth level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl4(String lvl4) {
            this.lvl4 = lvl4;
            return this;
        }

        /**
         * Gets the fifth level of the hierarchy.
         * 
         * @return The fifth level of the hierarchy.
         */
        public String getLvl5() {
            return lvl5;
        }

        /**
         * Sets the fifth level of the hierarchy.
         * 
         * @param lvl5 The fifth level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl5(String lvl5) {
            this.lvl5 = lvl5;
            return this;
        }

        /**
         * Gets the sixth level of the hierarchy.
         * 
         * @return The sixth level of the hierarchy.
         */
        public String getLvl6() {
            return lvl6;
        }

        /**
         * Sets the sixth level of the hierarchy.
         * 
         * @param lvl6 The sixth level of the hierarchy.
         * @return The current instance of the hierarchy.
         */
        public Hierarchy setLvl6(String lvl6) {
            this.lvl6 = lvl6;
            return this;
        }
    }

    public static class SnippetResult {
        @JsonProperty("content")
        private Match content;

        @JsonProperty("hierarchy")
        private Hierarchy hierarchy;

        /**
         * Default constructor for Jackson deserialization.
         */
        public SnippetResult() {}

        /**
         * Gets the content of the snippet result.
         * 
         * @return The content of the snippet result.
         */
        public Match getContent() {
            return content;
        }

        /**
         * Sets the content of the snippet result.
         * 
         * @param content The content of the snippet result.
         * @return The current instance of the snippet result.
         */
        public SnippetResult setContent(Match content) {
            this.content = content;
            return this;
        }

        /**
         * Gets the hierarchy of the snippet result.
         * 
         * @return The hierarchy of the snippet result.
         */
        public Hierarchy getHierarchy() {
            return hierarchy;
        }

        /**
         * Sets the hierarchy of the snippet result.
         * 
         * @param hierarchy The hierarchy of the snippet result.
         * @return The current instance of the snippet result.
         */
        public SnippetResult setHierarchy(Hierarchy hierarchy) {
            this.hierarchy = hierarchy;
            return this;
        }

        /**
         * Represents a match in a snippet result.
         */
        public static class Match {
            @JsonProperty("value")
            private String value;

            @JsonProperty("matchLevel")
            private String matchLevel;

            /**
             * Default constructor for Jackson deserialization.
             */
            public Match() {}

            /**
             * Gets the value of the match.
             * 
             * @return The value of the match.
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the match.
             * 
             * @param value The value of the match.
             * @return The current instance of the match.
             */
            public Match setValue(String value) {
                this.value = value;
                return this;
            }

            /**
             * Gets the match level of the match.
             * 
             * @return The match level of the match.
             */
            public String getMatchLevel() {
                return matchLevel;
            }

            /**
             * Sets the match level of the match.
             * 
             * @param matchLevel The match level of the match.
             * @return The current instance of the match.
             */
            public Match setMatchLevel(String matchLevel) {
                this.matchLevel = matchLevel;
                return this;
            }
        }

        /**
         * Represents the hierarchy of a snippet result.
         */
        public static class Hierarchy {
            @JsonProperty("lvl0")
            private Match lvl0;

            @JsonProperty("lvl1")
            private Match lvl1;

            @JsonProperty("lvl2")
            private Match lvl2;

            @JsonProperty("lvl3")
            private Match lvl3;

            @JsonProperty("lvl4")
            private Match lvl4;

            @JsonProperty("lvl5")
            private Match lvl5;

            @JsonProperty("lvl6")
            private Match lvl6;

            /**
             * Default constructor for Jackson deserialization.
             */
            public Hierarchy() {}

            /**
             * Gets the zeroth level of the hierarchy.
             * 
             * @return The zeroth level of the hierarchy.
             */
            public Match getLvl0() {
                return lvl0;
            }

            /**
             * Sets the zeroth level of the hierarchy.
             * 
             * @param lvl0 The zeroth level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl0(Match lvl0) {
                this.lvl0 = lvl0;
                return this;
            }

            /**
             * Gets the first level of the hierarchy.
             * 
             * @return The first level of the hierarchy.
             */
            public Match getLvl1() {
                return lvl1;
            }

            /**
             * Sets the first level of the hierarchy.
             * 
             * @param lvl1 The first level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl1(Match lvl1) {
                this.lvl1 = lvl1;
                return this;
            }

            /**
             * Gets the second level of the hierarchy.
             * 
             * @return The second level of the hierarchy.
             */
            public Match getLvl2() {
                return lvl2;
            }

            /**
             * Sets the second level of the hierarchy.
             * 
             * @param lvl2 The second level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl2(Match lvl2) {
                this.lvl2 = lvl2;
                return this;
            }

            /**
             * Gets the third level of the hierarchy.
             * 
             * @return The third level of the hierarchy.
             */
            public Match getLvl3() {
                return lvl3;
            }

            /**
             * Sets the third level of the hierarchy.
             * 
             * @param lvl3 The third level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl3(Match lvl3) {
                this.lvl3 = lvl3;
                return this;
            }

            /**
             * Gets the fourth level of the hierarchy.
             * 
             * @return The fourth level of the hierarchy.
             */
            public Match getLvl4() {
                return lvl4;
            }

            /**
             * Sets the fourth level of the hierarchy.
             * 
             * @param lvl4 The fourth level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl4(Match lvl4) {
                this.lvl4 = lvl4;
                return this;
            }

            /**
             * Gets the fifth level of the hierarchy.
             * 
             * @return The fifth level of the hierarchy.
             */
            public Match getLvl5() {
                return lvl5;
            }

            /**
             * Sets the fifth level of the hierarchy.
             * 
             * @param lvl5 The fifth level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl5(Match lvl5) {
                this.lvl5 = lvl5;
                return this;
            }

            /**
             * Gets the sixth level of the hierarchy.
             * 
             * @return The sixth level of the hierarchy.
             */
            public Match getLvl6() {
                return lvl6;
            }

            /**
             * Sets the sixth level of the hierarchy.
             * 
             * @param lvl6 The sixth level of the hierarchy.
             * @return The current instance of the hierarchy.
             */
            public Hierarchy setLvl6(Match lvl6) {
                this.lvl6 = lvl6;
                return this;
            }
        }
    }

    /**
     * Represents the highlight result of a search result.
     */
    public static class HighlightResult {
        @JsonProperty("content")
        private Content content;

        /**
         * Default constructor for Jackson deserialization.
         */
        public HighlightResult() {}

        /**
         * Gets the content of the highlight result.
         * 
         * @return The content of the highlight result.
         */
        public Content getContent() {
            return content;
        }

        /**
         * Sets the content of the highlight result.
         * 
         * @param content The content of the highlight result.
         * @return The current instance of the highlight result.
         */
        public HighlightResult setContent(Content content) {
            this.content = content;
            return this;
        }
        
        /**
         * Represents the content of a highlight result.
         */
        public static class Content {
            @JsonProperty("value")
            private String value;

            @JsonProperty("matchLevel")
            private String matchLevel;

            @JsonProperty("fullyHighlighted")
            private boolean fullyHighlighted;

            @JsonProperty("matchedWords")
            private List<String> matchedWords;

            /**
             * Default constructor for Jackson deserialization.
             */
            public Content() {}

            /**
             * Gets the value of the content.
             * 
             * @return The value of the content.
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the content.
             * 
             * @param value The value of the content.
             * @return The current instance of the content.
             */
            public Content setValue(String value) {
                this.value = value;
                return this;
            }

            /**
             * Gets the match level of the content.
             * 
             * @return The match level of the content.
             */
            public String getMatchLevel() {
                return matchLevel;
            }

            /**
             * Sets the match level of the content.
             * 
             * @param matchLevel The match level of the content.
             * @return The current instance of the content.
             */
            public Content setMatchLevel(String matchLevel) {
                this.matchLevel = matchLevel;
                return this;
            }

            /**
             * Checks if the content is fully highlighted.
             * 
             * @return {@code true} if the content is fully highlighted, {@code false} otherwise.
             */
            public boolean isFullyHighlighted() {
                return fullyHighlighted;
            }

            /**
             * Sets if the content is fully highlighted.
             * 
             * @param fullyHighlighted {@code true} if the content is fully highlighted, {@code false} otherwise.
             * @return The current instance of the content.
             */
            public Content setFullyHighlighted(boolean fullyHighlighted) {
                this.fullyHighlighted = fullyHighlighted;
                return this;
            }

            /**
             * Gets the matched words of the content.
             * 
             * @return The matched words of the content.
             */
            public List<String> getMatchedWords() {
                return matchedWords;
            }

            /**
             * Sets the matched words of the content.
             * 
             * @param matchedWords The matched words of the content.
             * @return The current instance of the content.
             */
            public Content setMatchedWords(List<String> matchedWords) {
                this.matchedWords = matchedWords;
                return this;
            }
        }
    }
}
