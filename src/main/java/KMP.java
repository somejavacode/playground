/**
 * how to "find" byte array in a byte array?
 *
 * some answers:
 * http://stackoverflow.com/questions/21341027/find-indexof-a-byte-array-within-another-byte-array
 * http://helpdesk.objects.com.au/java/search-a-byte-array-for-a-byte-sequence
 *  The Knuth-Morris-Pratt Pattern Matching Algorithm can be used to search a byte array.
 *
 * TODO: verify function with unit test and and benchmark against guava
 *
 */
public class KMP {
    /**
     * Search the data byte array for the first occurrence
     * of the byte array pattern.
     *
     * @param data input data
     * @param pattern the pattern to find
     * @return position of pattern, -1 if not found
     */
    public static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     *
     * @param pattern input pattern
     * @return failure values
     */
    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

}
