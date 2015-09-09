package test.ufw;

import org.junit.Assert;
import org.junit.Test;
import ufw.Log;
import ufw.RandomBytes;
import ufw.RandomInputStream;
import ufw.RandomOutputStream;
import ufw.StreamTool;
import ufw.Validate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class StreamTest {

    @Test
    public void testCopyAll() throws IOException {
        doCopyAll(256, RandomBytes.create(1024, 4711));
        doCopyAll(255, RandomBytes.create(1024, 4711));
        doCopyAll(256, RandomBytes.create(1023, 4711));
        doCopyAll(1, RandomBytes.create(1024, 4711));
        doCopyAll(13, RandomBytes.create(4095, 4711));
        doCopyAll(13, RandomBytes.create(4096, 4711));

        doCopyAll(256, RandomBytes.create(1024 * 1024, 4711));
        doCopyAll(257, RandomBytes.create(1024 * 1024, 4711));
    }

    private void doCopyAll(int blockSize, byte[] randomBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamTool.copyAll(bais, baos, blockSize);
        byte[] copyRandomBytes = baos.toByteArray();
        Assert.assertTrue(Arrays.equals(randomBytes, copyRandomBytes));
    }

    @Test
    public void testCopyParts() throws IOException {
        int size1 = 232;
        int size2 = 177;
        byte[] randomBytes = RandomBytes.create(size1 + size2, 4711);
        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferSize = 13;
        StreamTool.copy(bais, baos, bufferSize, size1);
        StreamTool.copy(bais, baos, bufferSize, size2);
        byte[] copyRandomBytes = baos.toByteArray();
        Assert.assertTrue(Arrays.equals(randomBytes, copyRandomBytes));
    }

    @Test
    public void testCopyPartsAligned() throws IOException {
        int size1 = 256;
        int size2 = 64;
        byte[] randomBytes = RandomBytes.create(size1 + size2, 4712);
        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferSize = 16;
        StreamTool.copy(bais, baos, bufferSize, size1);
        StreamTool.copy(bais, baos, bufferSize, size2);
        byte[] copyRandomBytes = baos.toByteArray();
        Assert.assertTrue(Arrays.equals(randomBytes, copyRandomBytes));
    }

    @Test
    public void testCopyError() throws IOException {
        int size = 201;
        byte[] randomBytes = RandomBytes.create(size, 4711);
        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bufferSize = 13;
        try {
            StreamTool.copy(bais, baos, bufferSize, size + 1);  // demand one extra byte
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("Expected. " + e.toString());
        }
    }


    @Test
    public void testRandomOutputStream() throws IOException {
        int length = 333;
        int seed = 3;
        byte[] randomBytes = RandomBytes.create(length, seed);

        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        RandomOutputStream ros = new RandomOutputStream(seed, length);

        StreamTool.copyAll(bais, ros, 13);
        Validate.isTrue(ros.getMissingByteCount() == 0);  // no more bytes missing
    }

    @Test
    public void testRandomOutputStreamErrors() throws IOException {
        int length = 333;
        int seed = 3;
        byte[] randomBytes = RandomBytes.create(length, seed);
        randomBytes[length - 1] ^= 0x80;  // flip last bit

        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        RandomOutputStream ros = new RandomOutputStream(seed, length);

        try {
            StreamTool.copyAll(bais, ros, 13);
        }
        catch (IOException ioe) {
            Log.info("expected. " + ioe.toString());
        }
    }

    @Test
    public void testRandomOutputStreamSurplus() throws IOException {
        int length = 333;
        int seed = 3;
        byte[] randomBytes = RandomBytes.create(length + 1, seed);

        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        RandomOutputStream ros = new RandomOutputStream(seed, length);

        try {
            StreamTool.copyAll(bais, ros, 13);
            Assert.fail();
        }
        catch (IOException ioe) {
            Log.info("expected. " + ioe.toString());
        }
    }

    @Test
    public void testRandomOutputStreamMissing() throws IOException {
        int length = 333;
        int seed = 3;
        byte[] randomBytes = RandomBytes.create(length - 1, seed);

        ByteArrayInputStream bais = new ByteArrayInputStream(randomBytes);
        RandomOutputStream ros = new RandomOutputStream(seed, length);
        StreamTool.copyAll(bais, ros, 13);
        Assert.assertEquals(ros.getMissingByteCount(), 1);
    }


    @Test
    public void testRandomInputOutput() throws IOException {
        int length = 64 * 1024;
        int seed = 33;

        RandomOutputStream ros = new RandomOutputStream(seed, length);
        RandomInputStream ris = new RandomInputStream(seed, length);
        StreamTool.copyAll(ris, ros, 33);
    }

    @Test
    public void testRandomInputStream() throws IOException {

        int length = 777;
        int seed = 5;

        byte[] rand = RandomBytes.create(length, seed);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RandomInputStream ris = new RandomInputStream(seed, length);
        StreamTool.copyAll(ris, baos, 13);
        byte[] osBytes = baos.toByteArray();
        Assert.assertTrue(Arrays.equals(rand, osBytes));

    }

    @Test
    public void testRandomInputStreamEOF() throws IOException {

        RandomInputStream ris = new RandomInputStream(55, 5);
        Assert.assertTrue(ris.read() >= 0);
        Assert.assertTrue(ris.read() >= 0);
        Assert.assertTrue(ris.read() >= 0);
        Assert.assertTrue(ris.read() >= 0);
        Assert.assertTrue(ris.read() >= 0);
        Assert.assertTrue(ris.read() == -1);  // expect EOF after 5 (non EOF) bytes


        int bytes = 3333;
        int bufferSize = 256;
        ris = new RandomInputStream(55, bytes);
        // read all bytes
        StreamTool.copy(ris, new ByteArrayOutputStream(), bufferSize, bytes);
        // expect EOF
        Assert.assertTrue(ris.read() == -1);
    }

}
