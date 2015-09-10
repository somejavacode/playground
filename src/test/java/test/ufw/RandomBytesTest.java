package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Log;
import ufw.RandomBytes;

import java.util.Arrays;
import java.util.Random;

public class RandomBytesTest {

    @Test
    public void testSingleByte() {
        int seed = 12;
        RandomBytes r = new RandomBytes(seed);
        byte b1 = r.nextByte();
        byte b2 = r.nextByte();
        byte b3 = r.nextByte();
        byte b4 = r.nextByte();

        r = new RandomBytes(seed);
        byte[] four = new byte[4];
        r.nextBytes(four);

        // this is OK with RandomBytes
        Assert.assertEquals(b1, four[0]);
        Assert.assertEquals(b2, four[1]);
        Assert.assertEquals(b3, four[2]);
        Assert.assertEquals(b4, four[3]);
    }

    @Test
    public void testRanges() {
        int seed = 12;
        RandomBytes r = new RandomBytes(seed);
        byte[] bytes = new byte[15];
        r.nextBytes(bytes);

        r = new RandomBytes(seed);
        byte[] bytes2 = new byte[15];
        r.nextBytes(bytes2, 0, 3);
        r.nextBytes(bytes2, 3, 5);
        r.nextBytes(bytes2, 8, 7);

        Assert.assertTrue(Arrays.equals(bytes, bytes2));
    }

    @Test
    public void testSingleByteRange() {
        int seed = 12;
        RandomBytes r = new RandomBytes(seed);

        int count = 10000;  // heuristic "limit", OK with fixed seed.
        int min = 127;
        int max = -128;
        int sum = 0;
        for (int i = 0; i < count; i++) {
            byte b = r.nextByte();
            sum += b;
            if (b > max) {
                max = b;
            }
            if (b < min) {
                min = b;
            }
        }
        Assert.assertEquals(min, -128);
        Assert.assertEquals(max, 127);
        // expect -0.5 * count, test for range: -count .. 0
        Assert.assertTrue(sum < 0 && sum > -count);
    }

    @Test
    public void testSingleByteFail() {
        int seed = 12;
        Random r = new Random(seed);
        byte b1 = nextByte(r);
        byte b2 = nextByte(r);
        byte b3 = nextByte(r);
        byte b4 = nextByte(r);

        r = new Random(seed);
        byte[] four = new byte[4];
        r.nextBytes(four);

        // this test fails :-(
        try {
            Assert.assertEquals(b1, four[0]);
            Assert.assertEquals(b2, four[1]);
            Assert.assertEquals(b3, four[2]);
            Assert.assertEquals(b4, four[3]);
            Assert.fail();
        }
        catch (AssertionError e) {
            Log.info("expected to fail. " + e.toString());
        }
    }

    // this is the unlucky "simulation" of missing signature Random.nextByte().
    public byte nextByte(Random r) {
        byte[] single = new byte[1];
        r.nextBytes(single);
        return single[0];
    }

    @Test
    public void testRandomBytes() {

        int length = 333;
        int seed = 4;

        byte[] rand = RandomBytes.create(length, seed);
        RandomBytes.verify(rand, length, seed);

        rand[123] ^= 0x20;  // change one bit

        try {
            RandomBytes.verify(rand, length, seed);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("Expected. " + e.toString());
        }
    }

}
