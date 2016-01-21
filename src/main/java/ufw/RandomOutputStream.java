package ufw;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * validates bytes written to stream.
 */
public class RandomOutputStream extends OutputStream {

    private RandomBytes random;
    private int length;
    private int count; // number of bytes written to stream

    /**
     * create random stream "sink"
     * @param seed seed for random
     * @param length number of expected bytes
     */
    public RandomOutputStream(long seed, int length) {
        this.random = new RandomBytes(seed);
        this.length = length;
        this.count = 0;
    }

    @Override
    public void write(int b) throws IOException {
        int b8 = b & 0xFF;
        if (++count > length) {
            throw new IOException("got surplus byte. total length=" + count + ". value=" + b8);
        }
        int r8 = random.nextByte() & 0xFF;

        if (r8 != b8) {
            throw new IOException("wrong byte. got=" + b8 + " expect=" + r8);
        }
    }

    @Override
    public void write(byte[] b, int pos, int size) throws IOException {

        count += size;
        if (count > length) {
            throw new IOException("got too many bytes. extra= " + (count - length));
        }
        byte[] randomBytes = new byte[size];
        random.nextBytes(randomBytes);
        byte[] checkBytes = new byte[size];
        System.arraycopy(b, pos, checkBytes, 0, size);
        if (!Arrays.equals(randomBytes, checkBytes)) {
            throw new IOException("got wrong bytes."); // TODO. dump both arrays? might be a lot...
        }
    }

    public int getMissingByteCount() {
        return length - count;
    }
}
