import ufw.Log;

import java.util.ArrayList;

public class ThreadRamp {

    public static void main(String[] args) throws Exception {
        int limit = Integer.MAX_VALUE; // "no" limit
        if (args.length > 0) {
            limit = Integer.parseInt(args[0]);
        }

        //Thread t = new Thread(new Runner("runnerx", 500, 5, true), "runnerx");
        //t.start();
        //// Thread.sleep(1200);
        //// t.interrupt();
        //t.join();
        ArrayList<Thread> threads = new ArrayList<Thread>();

        for (int i = 1; i <= limit; i++) {
            String name = "Runner-" + i;
            Thread t = new Thread(new Runner(name, 5000, Integer.MAX_VALUE, false), name);
            threads.add(t);
            t.start();
            if (i % 1000 == 0) {
                Log.info("started " + i);
            }
        }
    }

    private static class Runner implements Runnable {

        private String name;
        private int sleepTime;
        private int loops;
        private boolean log;

        public Runner(String name, int sleepTime, int loops, boolean log) {
            this.name = name;
            this.sleepTime = sleepTime;
            this.loops = loops;
            this.log = log;
        }

        @Override
        public void run() {
            if (log) {
                Log.info(name + " started");
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
                Log.warn(name + " was interrupted");
            }
            catch (Throwable t) {
                Log.error(name + " throws.", t);
            }
            if (log) {
                Log.info(name + " finished");
            }
        }
    }
}
