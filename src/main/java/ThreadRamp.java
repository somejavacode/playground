import ufw.Log;

import java.util.ArrayList;

/**
 * note: with JDK8 will not use non-heap memory for threads (i.e. stacks?).
 * <p>
 * limiting thread based memory is only possibly by limiting threads.
 * <p>
 * changing stack size (e.g. -Xss256k) does not change initial memory behavior.
 * <p>
 * no way to find out (default) stack size. (could estimate by depth of infinite recursion).
 */
public class ThreadRamp {

    public static void main(String[] args) throws Exception {
//        int limit = Integer.MAX_VALUE; // "no" limit
        int limit = 10000;
        if (args.length > 0) {
            limit = Integer.parseInt(args[0]);
        }
        int wait = 10000; // 10s default
        if (args.length > 1) {
            wait = Integer.parseInt(args[1]) * 1000;
        }
        int rampSleep = 0;
        if (args.length > 2) {
            rampSleep = Integer.parseInt(args[2]);
        }


        // Thread t = new Thread(new Runner("runnerx", 500, 5, true), "runnerx");
        // t.start();
        //// Thread.sleep(1200);
        //// t.interrupt();
        // t.join();

        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int i = 1; i <= limit; i++) {
            String name = "Runner-" + i;
            Thread t = new Thread(new Runner(name, 5000, Integer.MAX_VALUE, false), name);
            threads.add(t);
            t.start();
            if (rampSleep > 0) {
                Thread.sleep(rampSleep);
            }
            if (i % 1000 == 0) {
                Log.info("started " + i);
            }
        }
        Log.info("crated " + limit + " threads");

        Thread.sleep(wait);

        Log.info("interrupting");
        for (Thread t : threads) {
            t.interrupt();
        }
        Log.info("joining");
        for (Thread t : threads) {
            t.join();
        }
        Log.info("done");
    }

    private static class Runner implements Runnable {

        private String name;
        private int sleepTime;
        private int loops;
        private boolean log;

        Runner(String name, int sleepTime, int loops, boolean log) {
            this.name = name;
            this.sleepTime = sleepTime;
            this.loops = loops;
            this.log = log;
        }

        @Override
        public void run() {
            if (log) {
                Log.info(name + " started.");
            }
            int round = 0;
            try {
                while (round < loops) {
                    Thread.sleep(sleepTime);
                    round++;
                    if (log) {
                        Log.info(name + " round " + round);
                    }
                }
            }
            catch (InterruptedException ie) {
                if (log) {
                    Log.warn(name + " was interrupted after rounds=" + round);
                }
            }
            catch (Throwable t) {
                Log.error(name + " throws after rounds=" + round, t);
            }
            if (log) {
                Log.info(name + " finished. rounds=" + round);
            }
        }
    }
}
