package test.ufw;


import org.junit.Assert;
import org.junit.Test;
import ufw.Log;
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

        Assert.assertTrue(time > duration - tolerance && time < duration + tolerance);
    }

    @Test
    public void testTimerFast() {
        // the "fastest" timer case
        Timer t = new Timer("fast", false);
        t.stop(true);    // approx 1ms. far to slow. assume hotspot warm-up effect
    }


    @Test
    public void testTimerSplit() throws Exception {
        Timer t = new Timer("splitting", true);
        Thread.sleep(22);
        t.split("lap1");
        Thread.sleep(44);
        t.split("lap2");
        t.stop(true);

    }

}
