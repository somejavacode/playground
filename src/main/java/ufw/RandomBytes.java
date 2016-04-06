package ufw;

import java.util.Arrays;
import java.util.Random;

/**
 * this class fixes three "problems" in Random.nextBytes()<br/>
 * a) missing signature nextBytes(byte[], offset, size)<br/>
 * b) missing signature nextByte()<br/>
 * c) problems if array sizes not aligned to integer (4 bytes): e.g. two calls nextBytes(byte[1]) does not match one call nextBytes(byte[2])<br/>
 */
public class RandomBytes {

    private Random random;
    private int lastRand;
    private int lastRandPos;

    /**
     * create RandomBytes with given seed
     * @param seed seed for Random
     */
    public RandomBytes(long seed) {
        this.random = new Random(seed);
        this.lastRand = random.nextInt();
        this.lastRandPos = 0;
    }

    /**
     * create RandomBytes with given Random
     * @param rand Random to use
     */
    public RandomBytes(Random rand) {
        this.random = rand;
        this.lastRand = random.nextInt();
        this.lastRandPos = 0;
    }

    /**
     * @return next random byte
     */
    public byte nextByte() {
        if (lastRandPos < 4) {
            return (byte) (lastRand >> (8 * lastRandPos++) & 0xFF);
        }
        // all bytes consumed, fetch next integer
        this.lastRand = random.nextInt();
        lastRandPos = 1;
        return (byte) (lastRand & 0xFF);
    }

    /**
     * copy random bytes to array
     *
     * @param bytes array to fill with random bytes
     */
    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = nextByte();
        }
    }

    /**
     * copy random bytes to range of array
     *
     * @param bytes array to fill (partially) with random bytes
     * @param from start index of bytes
     * @param len number of bytes
     */
    public void nextBytes(byte[] bytes, int from, int len) {
        for (int i = from; i < from + len; i++) {
            bytes[i] = nextByte();
        }
    }

    /**
     * create random byte array
     *
     * @param size length of array
     * @param seed seed for Random
     * @return random array
     */
    public static byte[] create(int size, long seed) {
        RandomBytes rand = new RandomBytes(seed);
        byte[] buffer = new byte[size];
        rand.nextBytes(buffer);
        return buffer;
    }

    /**
     * validate random array
     *
     * @param random array to verify
     * @param size expected length
     * @param seed seed for Random
     */
    public static void verify(byte[] random, int size, long seed) {
        Validate.isTrue(random.length == size, "wrong size of random bytes.");
        RandomBytes rand = new RandomBytes(seed);
        byte[] buffer = new byte[size];
        rand.nextBytes(buffer);
        Validate.isTrue(Arrays.equals(random, buffer), "validation of random bytes failed.");
    }

}