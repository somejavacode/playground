package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Log;

import java.time.Clock;

public class TimeResolutionTest {

    @Test
    public void testCurrent() {
        int runs = 50;
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            long end;
            while (true) {
                end = System.currentTimeMillis();
                if (end != start) {
                    break;
                }
            }
            Assert.assertTrue(end > start);
            total += end - start;
        }
        // windows crap: 1/64s (15.625ms)
        Log.info("average currentTimeMillis step: " + 1.0 * total / runs);
    }

    @Test
    public void testCurrentSleeper() throws Exception {
        // this is insane. the sleeper thread will increase timer precision on windows.
        // for more "amusement" see
        // https://bugs.openjdk.java.net/browse/JDK-6435126 "This has been broken for too long. No use fixing it now."
        Runnable sleeper = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                }
                catch (InterruptedException e) {
                    Log.info("interrupted.");
                }
            }
        };
        Thread sleeperThread = new Thread(sleeper, "sleeper");
        sleeperThread.start();
        Thread.sleep(50); // wait a while

        int runs = 200;
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.currentTimeMillis();
            long end;
            while (true) {
                end = System.currentTimeMillis();
                if (end != start) {
                    break;
                }
            }
            Assert.assertTrue(end > start);
            total += end - start;
        }
        // improved to approx 1ms
        Log.info("average currentTimeMillis step (sleeper): " + 1.0 * total / runs);
        sleeperThread.interrupt();
    }

    @Test
    public void testTimeUTC() {
        int runs = 50;
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = Clock.systemUTC().millis();
            long end;
            while (true) {
                end = Clock.systemUTC().millis();
                if (end != start) {
                    break;
                }
            }
            Assert.assertTrue(end > start);
            total += end - start;
        }
        Log.info("average Clock millis step: " + 1.0 * total / runs);
    }

    @Test
    public void testNano() {
        int runs = 100;
        long total = 0;
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            long end;
            while (true) {
                end = System.nanoTime();
                if (end != start) {
                    break;
                }
            }
            Assert.assertTrue(end > start);
            total += end - start;
        }
        Log.info("average nano step: " + 1.0 * total / runs);  // approx 1000
    }

}
