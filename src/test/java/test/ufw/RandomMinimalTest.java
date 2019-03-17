package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.RandomMinimal;
import ufw.Timer;

import java.util.Random;

public class RandomMinimalTest {

    @Test
    public void testSamples() {
        Random r = new Random();
        int seedCount = 1000;  // test 1000 random seed values
        for (int s = 0; s < seedCount; s++) {
            long seed = r.nextLong();
            Random test = new Random(seed);
            RandomMinimal testMin = new RandomMinimal(seed);

            int samples = 1000; // compare 1000 random values
            for (int i = 0; i < samples; i++) {
                int r1 = test.nextInt();
                int r2 = testMin.nextInt();
                Assert.assertEquals(r1, r2);
            }
        }
    }

    @Test
    public void testSpeed() {
        long seed = 4711;
        Random test = new Random(seed);
        RandomMinimal testMin = new RandomMinimal(seed);
        int repeats = 1000000;
        Timer t = new Timer("Test Random " + repeats + " times", false);
        for (int i = 0; i < repeats; i++) {
            test.nextInt();
        }
        t.stop(true);
        t = new Timer("Test RandomMinimal " + repeats + " times", false);
        for (int i = 0; i < repeats; i++) {
            testMin.nextInt();
        }
        t.stop(true);
    }
}
