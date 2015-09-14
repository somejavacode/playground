package ufw;


import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream as source of random bytes
 */
public class RandomInputStream extends InputStream {

    private RandomBytes random;
    private int length;
    private int count; // number of bytes read

    /**
     * create new random stream "source"
     * @param seed seed for random
     * @param length number of bytes to deliver
     */
    public RandomInputStream(int seed, int length) {
        this.random = new RandomBytes(seed);
        this.length = length;
        this.count = 0;
    }

    @Override
    public int read() throws IOException {
        if (count >= length) {
            return -1; // EOF
        }
        int r = random.nextByte() & 0xFF; // get rid of sign
        count++;
        return r;
    }

    @Override
    public int read(byte[] b, int pos, int size) throws IOException {
        // Validate.isTrue(pos + size <= b.length); // check bounds
        int remaining = length - count;
        if (remaining <= 0) {  // no bytes left
            return -1;
        }
        // how many bytes to write?
        int bytes = remaining < size ? remaining : size;
        random.nextBytes(b, pos, bytes);
        count += bytes;
        return bytes;
    }

}
