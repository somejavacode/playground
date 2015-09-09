package ufw;

import java.util.Arrays;
import java.util.Random;

/**
 * this class fixes two "problems" in Random.nextBytes()<br/>
 * a) missing signature nextBytes(byte[], offset, size)<br/>
 * b) problems if array sizes not aligned to integer (4 bytes): e.g. two calls nextBytes(byte[1]) does not match one call nextBytes(byte[2]) <br/>
 */
public class RandomBytes {

    private Random random;

    public RandomBytes(int seed) {
        this.random = new Random(seed);
    }

    public byte nextByte() {
        return (byte) (random.nextInt() & 0xFF);
    }

    public void nextBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = nextByte();
        }
    }

    public void nextBytes(byte[] bytes, int from, int len) {
        for (int i = from; i < from + len; i++) {
            bytes[i] = nextByte();
        }
    }

    public static byte[] create(int size, int seed) {
        RandomBytes rand = new RandomBytes(seed);
        byte[] buffer = new byte[size];
        rand.nextBytes(buffer);
        return buffer;
    }

    public static void verify(byte[] random, int size, int seed) {
        RandomBytes rand = new RandomBytes(seed);
        byte[] buffer = new byte[size];
        rand.nextBytes(buffer);
        Validate.isTrue(Arrays.equals(random, buffer), "validation of random bytes failed.");
    }

}