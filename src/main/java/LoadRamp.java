import ufw.Log;
import ufw.SystemInfo;

/**
 * simple load ramp that burns cpu with thread ramp
 */
public class LoadRamp {

    public static void main(String[] args) {

        if (args.length < 1) {
            Log.info("Start with Ramp type as first argument: 'F': Fibonacci, 'M': Memory");
            System.exit(1);
        }

        SystemInfo.show();

        String rampType = args[0].toUpperCase();

        if ("F".equals(rampType)) {
            long fibRun = 0;
            if (args.length > 1) {
                fibRun = Long.parseLong(args[1]);
            }
            if (fibRun == 0) {
                int fibStart = 35;
                fibonacci(fibStart); // warm-up
                long start = System.currentTimeMillis();
                fibonacci(fibStart); // get time for initial run
                long time = System.currentTimeMillis() - start;
                // estimate fibonacci value for 10s burn time.
                double desiredTime = 10 * 1000;
                double extraFib = Math.log(desiredTime / time) / Math.log(2);
                fibRun = fibStart + 1 + (long) Math.floor(extraFib);
                Log.info("estimate fibonacci argument: " + fibRun);
            }

            long start = System.currentTimeMillis();
            fibonacci(fibRun);
            long time = System.currentTimeMillis() - start;
            Log.info("fibonacci(" + fibRun + ") time: " + time + "ms");

            fibonacciMulti(fibRun, 2, time);
            fibonacciMulti(fibRun, 4, time);
            fibonacciMulti(fibRun, 8, time);
            // fibonacciMulti(fibRun, 16, time);
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

    private static void fibonacciMulti(long n, int threads, long singleTime) {
        Thread[] runners = new Thread[threads];
        long start = System.currentTimeMillis();
        for (int i = 0; i < threads; i++) {
            runners[i] = new FibRunner("fib-" + i, n);
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
        Log.info("fibonacci(" + n + ") x" + threads + " time: " + time + "ms. speedUp=" + speedUp);

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
     * fibonacci recursion to burn cpu
     */
    private static long fibonacci(long n) {
        if (n <= 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
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
        private long i;

        public FibRunner(String name, long i) {
            super(name);
            this.i = i;
        }

        public void run() {
            fibonacci(i);
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
