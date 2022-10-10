package org.meteoinfo.common.io;

import javax.annotation.concurrent.Immutable;

/**
 * Knuth-Morris-Pratt Algorithm for Pattern Matching.
 *
 * @see <a href=
 *      "http://www.fmi.uni-sofia.bg/fmi/logic/vboutchkova/sources/KMPMatch_java.html">http://www.fmi.uni-sofia.bg/fmi/logic/vboutchkova/sources/KMPMatch_java.html</a>
 */
@Immutable
public class KMPMatch {

    private final byte[] match;
    private final int[] failure;

    /**
     * Constructor
     *
     * @param match search for this byte pattern
     */
    public KMPMatch(byte[] match) {
        this.match = match;
        failure = computeFailure(match);
    }

    public int getMatchLength() {
        return match.length;
    }

    /**
     * Finds the first occurrence of match in data.
     *
     * @param data search in this byte block
     * @param start start at data[start]
     * @param max end at data[start+max]
     * @return index into data[] of first match, else -1 if not found.
     */
    public int indexOf(byte[] data, int start, int max) {
        int j = 0;
        if (data.length == 0)
            return -1;

        for (int i = start; i < start + max; i++) {
            while (j > 0 && match[j] != data[i])
                j = failure[j - 1];

            if (match[j] == data[i])
                j++;

            if (j == match.length)
                return i - match.length + 1;

        }
        return -1;
    }

    /*
     * Finds the first occurrence of match in data.
     *
     * @param data search in this byte block
     *
     * @param start start at data[start]
     *
     * @param max end at data[start+max]
     *
     * @return index into block of first match, else -1 if not found.
     *
     * public int scan(InputStream is, int start, int max) {
     * int j = 0;
     * if (data.length == 0) return -1;
     *
     * for (int i = start; i < start + max; i++) {
     * while (j > 0 && match[j] != data[i])
     * j = failure[j - 1];
     *
     * if (match[j] == data[i])
     * j++;
     *
     * if (j == match.length)
     * return i - match.length + 1;
     *
     * }
     * return -1;
     * } //
     */


    private int[] computeFailure(byte[] match) {
        int[] result = new int[match.length];

        int j = 0;
        for (int i = 1; i < match.length; i++) {
            while (j > 0 && match[j] != match[i])
                j = result[j - 1];

            if (match[j] == match[i])
                j++;

            result[i] = j;
        }

        return result;
    }
}
