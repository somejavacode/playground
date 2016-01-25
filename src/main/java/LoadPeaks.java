import ufw.Log;
import ufw.SystemInfo;

/**
 * program to generate short sequential load peaks to test speedstep/P-state behavior
 */
public class LoadPeaks {

    public static void main(String[] args) throws Exception {
        SystemInfo.show();

        int fib = 22; // short run.. few ms.
        int sleep = 1000; // one second

        if (args.length > 0) {
            fib = Integer.parseInt(args[0]);
        }
        if (args.length > 1) {
            sleep = Integer.parseInt(args[1]);
        }

        while (true) {
            long start = System.nanoTime();
            fibonacci(fib);
            long time = System.nanoTime() - start;
            Log.info("fibonacci(" + fib + ") took " + time / 1000 + "us");
            Thread.sleep(sleep);
        }
    }

    /**
     * fibonacci recursion to burn cpu
     */
    private static long fibonacci(long n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}