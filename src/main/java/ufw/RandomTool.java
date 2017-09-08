package ufw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

// not deleted to compare benchmark results. RandomBytes is the fast and clean solution
@Deprecated
public class RandomTool {

    /**
     * this is modified version of Random.nextBytes().
     * It allows to write to certain range of byte array (similar to InputStream.read(byte[], int, int)).
     *
     * @param rand generator to use
     * @param bytes array to write to
     * @param from start index
     * @param len number of bytes
     */
    public static void nextBytes(Random rand, byte[] bytes, int from, int len) {
        int end = from + len;
        int pos = from;
        int rInt;

        while (end - pos > 4) {   // while more than 4 bytes open
            // get random int
            rInt = rand.nextInt();
            // fill according bytes in array
            bytes[pos++] = (byte) (rInt & 0xFF);
            bytes[pos++] = (byte) (rInt >> 8 & 0xFF);
            bytes[pos++] = (byte) (rInt >> 16 & 0xFF);
            bytes[pos++] = (byte) (rInt >> 24 & 0xFF);
        }
        // last 0-3 bytes
        rInt = rand.nextInt();
        int shift = 0;
        while (pos < end) {
            bytes[pos++] = (byte) (rInt >> shift & 0xFF);
            shift += 8;
        }
    }


    public static void createRandomFile(String fileName, int size, int seed) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        Random rand = new Random(seed);
        int bufferSize = 8192;
        byte[] buffer = new byte[bufferSize];
        int remaining = size;
        while (remaining > bufferSize) {
            rand.nextBytes(buffer);
            fos.write(buffer);
            remaining -= bufferSize;
        }
        if (remaining > 0) {
            buffer = new byte[remaining];
            rand.nextBytes(buffer);
            fos.write(buffer);
        }
        fos.close(); // todo: this should be "finally"
    }

    public static void validateRandomFile(String fileName, int size, int seed) throws IOException {
        File f = new File(fileName);
        Validate.isTrue(f.length() == size);
        FileInputStream fos = new FileInputStream(f);
        Random rand = new Random(seed);
        int bufferSize = 8192;
        byte[] readBytes = new byte[bufferSize];
        byte[] randBytes = new byte[bufferSize];
        int remaining = size;
        while (remaining > bufferSize) {
            rand.nextBytes(randBytes);
            int bytes = fos.read(readBytes);
            Validate.isTrue(bytes == bufferSize);
            Validate.isTrue(Arrays.equals(randBytes, readBytes));
            remaining -= bufferSize;
        }
        if (remaining > 0) {
            readBytes = new byte[remaining];
            randBytes = new byte[remaining];
            rand.nextBytes(randBytes);
            int bytes = fos.read(readBytes);
            Validate.isTrue(bytes == bufferSize);
            Validate.isTrue(Arrays.equals(randBytes, readBytes));
        }
        fos.close(); // todo: this should be "finally"
    }

    /**
     * write random bytes to output stream (without flush)
     *
     * @param rand random generator
     * @param os the stream to write to
     * @param length number of bytes to write
     * @param blockSize size of block buffer
     * @param sleep sleep time after each block
     * @throws Exception in case of io problems
     */
    public void writeToStream(Random rand, OutputStream os, int length, int blockSize, int sleep) throws Exception {
        int remaining = length;
        byte[] bodyPart = new byte[blockSize];
        while (remaining > blockSize) {
            rand.nextBytes(bodyPart);
            os.write(bodyPart);
            remaining -= blockSize;
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
        }
        // final block
        if (remaining > 0) {
            byte[] last = new byte[remaining];
            rand.nextBytes(last);
            os.write(last);
        }
    }

    /**
     * write random bytes to output stream
     *
     * @param rand random generator
     * @param os the stream to write to
     * @param length number of bytes to write
     * @throws Exception in case of io problems
     */
    public void writeToStream(Random rand, OutputStream os, int length) throws Exception {
        writeToStream(rand, os, length, 8192, 0);
    }

    /**
     * read from stream and validate bytes against random bytes
     *
     * @param rand random generator
     * @param is the stream to read from
     * @param length number of bytes to read
     */
    public void validateFromStream(Random rand, InputStream is, int length) {

    }

}
