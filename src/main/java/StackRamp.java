/**
 * find out stack size. run with different values of -Xss
 * <p>
 * NOTE: values are aligned to next 64k, e.g. 65 to 128, 129 to 192
 *
 * windows JDK 1.8.0_60 x64:
 * -Xss128k (min): 858
 * -Xss192k: 1541
 * -Xss256k: 2223
 * -Xss512k: 4954..5076
 * -Xss1m (default): 11296..11697
 *
 *  windows JDK 1.8.0_60 x32:
 *  -Xss64k (mín): 623
 *  -Xss128k: 1988
 *  -Xss256k: 4719
 *  -Xss320k (default): 6084
 *  -Xss512k: 10136
 */
public class StackRamp {

    public static void main(String[] args) {
        fillStack(0);
    }

    private static void fillStack(int depth) {
        try {
            fillStack(depth + 1);
        }
        catch (StackOverflowError e) {
            System.out.println("gotcha. " + depth);
            // Log fails in this situation. "java.lang.NoClassDefFoundError: Could not initialize class ufw.Log"
            // Log.info("does this work? depth=" + depth, e);  // not sure if the stack overflow will be caught here?
        }
    }

}
