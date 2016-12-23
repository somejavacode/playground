package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Timer;

public class TimerTest {

    @Test
    public void testTimerValue() throws Exception {
        long duration = 333;
        Timer t = new Timer("delay " + duration, false);
        Thread.sleep(duration);
        t.stop(true);

        long tolerance = 3;  // allow 30ms error (test with windows 8.1: V1: 348..351, V2: 333)
        long time = t.getDuration() / 1000000;
        if (System.getProperty("os.name").contains("Windows XP")) {
            tolerance = 20; // Thread.sleep() has limited precision in XP. assume 15.625ms (real time clock 64Hz)
        }
        Assert.assertTrue("sleep duration tolerance of " + tolerance + "ms exceeded. sleep " + duration +
                          " took " + time, time > duration - tolerance && time < duration + tolerance);
    }

    @Test
    public void testTimerFast() {
        // the "fastest" timer case
        Timer t = new Timer("fast", false);
        t.stop(true);    // approx 1ms. far to slow. assume hotspot warm-up effect
    }

    @Test
    public void testTimerSplit() throws Exception {
        Timer t = new Timer("split test", "lap1", true);
        Thread.sleep(22);
        t.split("lap2", true);
        Thread.sleep(44);
        t.stop(true);
    }

    @Test
    public void testTimerSplitMessage() throws Exception {
        Timer t = new Timer("split test", "lap1", true);
        Thread.sleep(22);
        t.split("lap2", "did some things", true);
        Thread.sleep(44);
        t.split("lap3", "did more things", true);
        Thread.sleep(55);
        t.stop("did final things", true);
    }

    @Test
    public void testTimer() throws Exception {
        Timer t = new Timer("test", true);
        Thread.sleep(33);
        t.stop(true);
    }

}
