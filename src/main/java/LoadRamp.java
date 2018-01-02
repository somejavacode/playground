import ufw.Log;
import ufw.SystemInfo;

/**
 * simple load ramp that burns cpu with thread ramp
 */
public class LoadRamp {

    private enum FibType {
        LONG,
        INT,
        DOUBLE
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            Log.info("Start with Ramp type as first argument: 'F[I|D]': Fibonacci (Long,Int,Double) 'M': Memory");
            System.exit(1);
        }

        SystemInfo.show();

        String rampType = args[0].toUpperCase();

        if (rampType.startsWith("F")) {
            int fibRun = 0;
            FibType type = FibType.LONG; // default
            if (rampType.equals("FD")) {
                type = FibType.DOUBLE;
            }
            if (rampType.equals("FI")) {
                type = FibType.INT;
            }

            if (args.length > 1) {
                fibRun = Integer.parseInt(args[1]);
            }
            if (fibRun == 0) {
                int fibStart = 35;
                fibonacciLong(fibStart); // warm-up
                long start = System.currentTimeMillis();
                fibonacciLong(fibStart); // get time for initial run
                long time = System.currentTimeMillis() - start;
                // estimate fibonacci value for 10s burn time.
                double desiredTime = 10 * 1000;
                double extraFib = Math.log(desiredTime / time) / Math.log(2);
                fibRun = fibStart + 1 + (int) Math.floor(extraFib);
                Log.info("estimate fibonacci argument: " + fibRun);
            }

            long start = System.currentTimeMillis();
            fibonacciType(fibRun, type);
            long time = System.currentTimeMillis() - start;
            Log.info("fibonacci(" + type + "," + fibRun + ") time: " + time + "ms");

            if (args.length > 2) {
                // third argument: number of burn threads
                int burnThreads = Integer.parseInt(args[2]);
                while (true) {
                    fibonacciMulti(fibRun, burnThreads, time, type);
                }
            }

            // do the "ramp"
            fibonacciMulti(fibRun, 2, time, type);
            fibonacciMulti(fibRun, 4, time, type);
            fibonacciMulti(fibRun, 8, time, type);
        }
        else if ("M".equals(rampType)) {

            int arraySizeInMB = 100; //100MB in byte
            if (args.length > 1) {
                arraySizeInMB = Integer.parseInt(args[1]);
            }
            long start = System.currentTimeMillis();
            mem(arraySizeInMB);
            long time = System.currentTimeMillis() - start;
            Log.info("mem(" + arraySizeInMB + ") time: " + time + "ms");

            memMulti(arraySizeInMB, 2, time);
            memMulti(arraySizeInMB, 4, time);
            memMulti(arraySizeInMB, 8, time);
        }
        else {
            Log.info("Start with Ramp type as first argument: 'F': Fibonacci, 'M': Memory");
            System.exit(1);
        }
    }

    private static void fibonacciMulti(int n, int threads, long singleTime, FibType type) {
        Thread[] runners = new Thread[threads];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            runners[i] = new FibRunner("fib-" + i, n, type);
            runners[i].start();
        }
        for (int i = 0; i < threads; i++) {
            try {
                runners[i].join();
            }
            catch (InterruptedException e) {
                Log.warn("interrupted");
            }
        }
        long time = System.currentTimeMillis() - start;
        double speedUp = 1.0 * singleTime / time * threads;
        Log.info("fibonacci(" + type + "," + n + ") x" + threads + " time: " + time + "ms. speedUp=" + speedUp);

    }

    private static void memMulti(int memArraySize, int threads, long singleTime) {
        Thread[] runners = new Thread[threads];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            runners[i] = new MemRunner("mem-" + i, memArraySize);
            runners[i].start();
        }
        for (int i = 0; i < threads; i++) {
            try {
                runners[i].join();
            }
            catch (InterruptedException e) {
                Log.warn("interrupted");
            }
        }
        long time = System.currentTimeMillis() - start;
        double speedUp = 1.0 * singleTime / time * threads;
        Log.info("mem(" + memArraySize + ") x" + threads + " time: " + time + "ms. speedUp=" + speedUp);
    }


    /**
     * fibonacci recursion to burn cpu (long)
     */
    private static long fibonacciLong(long n) {
        if (n <= 1) {
            return n;
        }
        return fibonacciLong(n - 1) + fibonacciLong(n - 2);
    }

    /**
     * fibonacci recursion to burn cpu (integer)
     */
    private static int fibonacciInt(int n) {
        if (n <= 1) {
            return n;
        }
        return fibonacciInt(n - 1) + fibonacciInt(n - 2);
    }

    /**
     * fibonacci recursion to burn cpu (double)
     */
    private static double fibonacciDouble(double n) {
        if (n <= 1) {
            return n;
        }
        return fibonacciDouble(n - 1) + fibonacciDouble(n - 2);
    }

    private static void fibonacciType(int i, FibType type) {
        switch (type) {
            case INT:
                fibonacciInt(i);
                break;
            case LONG:
                fibonacciLong(i);
                break;
            case DOUBLE:
                fibonacciDouble(i);
                break;
        }
    }

    /**
     * initialize and fill a byte[n] array
     */
    private static void mem(int n) {
        byte[] array = new byte[n * 1024 * 1024];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) 170; //10101010
        }
    }

    private static class FibRunner extends Thread {
        private int i;
        private FibType type;

        public FibRunner(String name, int i, FibType type) {
            super(name);
            this.i = i;
            this.type = type;
        }

        public void run() {
            fibonacciType(i, type);
            // log("done");
        }
    }

    private static class MemRunner extends Thread {
        private int arraySize;

        private MemRunner(String name, int arraySize) {
            super(name);
            this.arraySize = arraySize;
        }

        public void run() {
            mem(arraySize);
        }
    }

}
