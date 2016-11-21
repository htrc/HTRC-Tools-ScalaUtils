package edu.illinois.i3.java.utils.metrics;

public class StringMetrics {

    /**
     * The Levenshtein distance is a string metric for measuring the difference between two sequences.
     * Informally, the Levenshtein distance between two words is the minimum number of single-character edits
     * (i.e. insertions, deletions or substitutions) required to change one word into the other.
     *
     * This is a fast iterative algorithm that's also memory efficient. O(len1 * len2) time, O(len1) space
     * Source: https://en.wikipedia.org/wiki/Levenshtein_distance
     *
     * @param s0 The first string
     * @param s1 The second string
     * @return The edit distance
     */
    public static int LevenshteinDistance(String s0, String s1) {
        // degenerate cases
        if (s0.equals(s1)) return 0;

        int len0 = s0.length();
        int len1 = s1.length();

        if (len0 == 0) return len1;
        if (len1 == 0) return len0;

        // the array of distances
        int[] cost = new int[len0 + 1];
        int[] newcost = new int[len0 + 1];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < cost.length; i++) cost[i] = i;

        // dynamically compute the array of distances

        // transformation cost for each letter in s1
        for (int j = 0; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j + 1;

            // transformation cost for each letter in s0
            for(int i = 0; i < len0; i++) {
                // matching current letters in both strings
                int match = (s0.charAt(i) == s1.charAt(j)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i] + match;
                int cost_insert  = cost[i + 1] + 1;
                int cost_delete  = newcost[i] + 1;

                // keep minimum cost
                newcost[i + 1] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0];
    }

}
