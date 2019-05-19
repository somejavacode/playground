package test.ufw;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ufw.Log;
import ufw.RandomBytes;
import ufw.RandomMinimal;
import ufw.RandomTool;
import ufw.Timer;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class RandomToolTest {


    private static final int SEED1 = 13;
    private static final int SEED2 = 14;

    private static final String TEMP_DIR = "/tmp/playground/rand/";

    @Before
    public void init() {
        try {
            new File(TEMP_DIR).mkdirs();  // this returns false if directory exists. this case is irrelevant
        }
        catch (Exception ex) {
            Log.error("create problem.", ex);
        }
    }

    @Test
    @Ignore
    public void createFileTest() throws Exception {

        int mega = 1024 * 1024; //  MiByte
        RandomTool.createRandomFile(TEMP_DIR + "ra", mega, SEED1);
        RandomTool.createRandomFile(TEMP_DIR + "ra2", 2 * mega, SEED1);
        RandomTool.createRandomFile(TEMP_DIR + "rb", mega, SEED2);

        int mBytes = 256;
        int size = mega * mBytes;
        Timer t;
        t = new Timer("create " + size + " random bytes on disk", false);
        RandomTool.createRandomFile(TEMP_DIR + "r" + mBytes, size, SEED1);
        t.stop(true);

        t = new Timer("validate " + size + " random bytes on disk", false);
        RandomTool.validateRandomFile(TEMP_DIR + "r" + mBytes, size, SEED1);
        t.stop(true);
    }

    @Test
    @Ignore
    public void validateFileTest() throws Exception {

        int mega = 1024 * 1024; //  MiByte

        // OK case
        RandomTool.createRandomFile(TEMP_DIR + "rv", mega, SEED1);
        RandomTool.validateRandomFile(TEMP_DIR + "rv", mega, SEED1);

        // add one byte

        // remove one byte

        // change one byte

    }


    @Test
    @Ignore
    public void createMemoryTest() {
        int size = 1024 * 1024 * 256; // 256 MiByte
        byte[] bytes = new byte[size];
        Random rand = new Random(SEED1);
        Timer t = new Timer("create " + size + " random bytes in memory", false);
        rand.nextBytes(bytes);
        t.stop(true);
    }

    @Test
    @Ignore
    public void createMemoryAltTest() {
        int size = 1024 * 1024 * 256; // 256 MiByte
        byte[] bytes = new byte[size];
        Random rand = new Random(SEED1);
        Timer t = new Timer("create " + size + " random bytes in memory alt", false);
        nextBytes(rand, bytes);
        t.stop(true);
    }

    @Test
    @Ignore
    public void createMemoryAltV2Test() {
        int size = 1024 * 1024 * 256; // 256 MiByte
        byte[] bytes = new byte[size];
        Random rand = new Random(SEED1);
        Timer t = new Timer("create " + size + " random bytes in memory alt v2", false);
        nextBytesV2(rand, bytes);
        t.stop(true);
    }

    @Test
    @Ignore
    public void createMemoryAltV3Test() {
        int size = 1024 * 1024 * 256; // 256 MiByte
        byte[] bytes = new byte[size];
        RandomMinimal rand = new RandomMinimal(SEED1);
        Timer t = new Timer("create " + size + " random bytes in memory alt v3", false);
        nextBytesV3(rand, bytes);
        t.stop(true);
    }

    @Test
    public void testRandAltV2() {
        compareRandomBytesAltV2(1, 1);
        compareRandomBytesAltV2(1, 2);
        compareRandomBytesAltV2(1, 3);
        compareRandomBytesAltV2(1, 4);
        compareRandomBytesAltV2(1, 8);
        compareRandomBytesAltV2(1, 12);
        compareRandomBytesAltV2(1, 13);
        compareRandomBytesAltV2(1, 14);
        compareRandomBytesAltV2(1, 15);
        compareRandomBytesAltV2(1, 4999);
        compareRandomBytesAltV2(4711, 1024 * 1024);
        compareRandomBytesAltV2(4712, 1024 * 1024);
    }

    @Test
    public void testRandAltV3() {
        compareRandomBytesAltV3(1, 1);
        compareRandomBytesAltV3(1, 2);
        compareRandomBytesAltV3(1, 3);
        compareRandomBytesAltV3(1, 4);
        compareRandomBytesAltV3(1, 8);
        compareRandomBytesAltV3(1, 12);
        compareRandomBytesAltV3(1, 13);
        compareRandomBytesAltV3(1, 14);
        compareRandomBytesAltV3(1, 15);
        compareRandomBytesAltV3(1, 4999);
        compareRandomBytesAltV3(4711, 1024 * 1024);
        compareRandomBytesAltV3(4712, 1024 * 1024);
    }

    @Test
    public void testRandAltPart() {

        Random rand = new Random(11);
        byte[] bytes = new byte[2048];
        byte[] bytes2 = new byte[2048];
        RandomTool.nextBytes(rand, bytes, 0, 1024);
        RandomTool.nextBytes(rand, bytes, 1024, 1024);

        rand = new Random(11);
        nextBytesV2(rand, bytes2);
        Assert.assertTrue(Arrays.equals(bytes, bytes2));

    }

    private void compareRandomBytesAltV2(int seed, int size) {
        byte[] bytes = new byte[size];
        byte[] bytesAlt = new byte[size];
        Random rand = new Random(seed);
        rand.nextBytes(bytes);
        rand = new Random(seed);
        nextBytesV2(rand, bytesAlt);
        Assert.assertTrue(Arrays.equals(bytes, bytesAlt));
    }

    private void compareRandomBytesAltV3(int seed, int size) {
        byte[] bytes = new byte[size];
        byte[] bytesAlt = new byte[size];
        Random rand = new Random(seed);
        rand.nextBytes(bytes);
        RandomMinimal rand2 = new RandomMinimal(seed);
        nextBytesV3(rand2, bytesAlt);
        Assert.assertTrue(Arrays.equals(bytes, bytesAlt));
    }

    /**
     * this is a copy of Random.nextBytes
     */
    private static void nextBytes(Random rand, byte[] bytes) {
        for (int i = 0, len = bytes.length; i < len; ) {
            for (int rnd = rand.nextInt(), n = Math.min(len - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) {
                bytes[i++] = (byte) rnd;
            }
        }
    }

    private static void nextBytesV2(Random rand, byte[] bytes) {
        RandomTool.nextBytes(rand, bytes, 0, bytes.length);
    }

    private static void nextBytesV3(RandomMinimal rand, byte[] bytes) {
        RandomBytes rb = new RandomBytes(rand);
        rb.nextBytes(bytes);
    }

}
