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
        Log.info("average currentTimeMillis step: " + 1.0 * total / runs);
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
