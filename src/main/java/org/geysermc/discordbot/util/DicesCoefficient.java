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

import java.util.Arrays;

/**
 * Dice's coefficient measures how similar two strings are.
 *
 * Pulled from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Dice%27s_coefficient#Java
 * 19/01/2021
 */
public class DicesCoefficient {

    /**
     * Here's an optimized version of the dice coefficient calculation. It takes
     * advantage of the fact that a bigram of 2 chars can be stored in 1 int, and
     * applies a matching algorithm of O(n*log(n)) instead of O(n*n).
     *
     * <p>Note that, at the time of writing, this implementation differs from the
     * other implementations on this page. Where the other algorithms incorrectly
     * store the generated bigrams in a set (discarding duplicates), this
     * implementation actually treats multiple occurrences of a bigram as unique.
     * The correctness of this behavior is most easily seen when getting the
     * similarity between "GG" and "GGGGGGGG", which should obviously not be 1.
     *
     * @param s1 The first string
     * @param s2 The second String
     * @return The dice coefficient between the two input strings. Returns 0 if one
     *         or both of the strings are {@code null}. Also returns 0 if one or both
     *         of the strings contain less than 2 characters and are not equal.
     * @author Jelle Fresen
     */
    @SuppressWarnings("StringEquality")
    public static double diceCoefficientOptimized(String s1, String s2)
    {
        // Verifying the input:
        if (s1 == null || s2 == null)
            return 0;
        // Quick check to catch identical objects:
        if (s1 == s2)
            return 1;
        // avoid exception for single character searches
        if (s1.length() < 2 || s2.length() < 2)
            return 0;

        // Create the bigrams for string s1:
        final int n = s1.length()-1;
        final int[] sPairs = new int[n];
        for (int i = 0; i <= n; i++)
            if (i == 0)
                sPairs[i] = s1.charAt(i) << 16;
            else if (i == n)
                sPairs[i-1] |= s1.charAt(i);
            else
                sPairs[i] = (sPairs[i-1] |= s1.charAt(i)) << 16;

        // Create the bigrams for string s2:
        final int m = s2.length()-1;
        final int[] tPairs = new int[m];
        for (int i = 0; i <= m; i++)
            if (i == 0)
                tPairs[i] = s2.charAt(i) << 16;
            else if (i == m)
                tPairs[i-1] |= s2.charAt(i);
            else
                tPairs[i] = (tPairs[i-1] |= s2.charAt(i)) << 16;

        // Sort the bigram lists:
        Arrays.sort(sPairs);
        Arrays.sort(tPairs);

        // Count the matches:
        int matches = 0, i = 0, j = 0;
        while (i < n && j < m)
        {
            if (sPairs[i] == tPairs[j])
            {
                matches += 2;
                i++;
                j++;
            }
            else if (sPairs[i] < tPairs[j])
                i++;
            else
                j++;
        }
        return (double)matches/(n+m);
    }
}
