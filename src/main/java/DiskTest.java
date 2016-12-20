import ufw.Timer;
import ufw.Validate;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * tool to perform disk read write tests
 */
public class DiskTest {

    public static void main(String[] args) throws Exception {
        int seedOffset = 2351116;

        int mega = 10;
        if (args.length > 0) {
            mega = Integer.parseInt(args[0]);
        }
        int size = mega * 1024 * 1024;

        int loops = 1;
        if (args.length > 1) {
            loops = Integer.parseInt(args[1]);
            if (loops == 0) {
                loops = Integer.MAX_VALUE;
            }
        }

        int blockSizeKilo = 8;
        if (args.length > 2) {
            blockSizeKilo = Integer.parseInt(args[2]);
        }
        int blockSize = blockSizeKilo * 1024;

        String mode = "rw";  // crappy magic Strings
        if (args.length > 3) {
            mode = args[3];
        }

        String fileName = "testfile_" + mega + "MOV";

        // clean up
        File file = new File(fileName);
        if (file.exists()) {
            Validate.isTrue(file.delete(), "failed to delete existing file " + fileName);
        }

        // todo: is it thread safe?
        RandomAccessFile raf = new RandomAccessFile(fileName, mode);
        raf.setLength(size);

        int count = size / blockSize; // assume "int"

        Timer t = new Timer("generator speed for " + mega + "MB", false);
        for (int bl = 0; bl < count; bl++) {
            getBlock(bl, blockSize, seedOffset);
        }
        t.stop(true);

        for (int loop = 0; loop < loops; loop++) {

            // linear write
            t = new Timer("linear write " + mega + "MB", true);
            for (int bl = 0; bl < count; bl++) {
                writeBlock(raf, bl, blockSize, seedOffset + loop);
            }
            t.stop(true);

            // linear read
            t = new Timer("linear read " + mega + "MB", true);
            for (int bl = 0; bl < count; bl++) {
                validateBlock(raf, bl, blockSize, seedOffset + loop);
            }
            t.stop(true);
        }

        raf.close();
        // MappedByteBuffer might be an alternative

    }

    private static void writeBlock(RandomAccessFile raf, int bl, int blockSize, int seedOffset) throws Exception {
        byte[] block = getBlock(bl, blockSize, seedOffset);
        raf.seek(bl * blockSize);  // todo: check overhead in case of linear read
        raf.write(block);
    }

    private static void validateBlock(RandomAccessFile raf, int bl, int blockSize, int seedOffset) throws Exception {
        byte[] block = getBlock(bl, blockSize, seedOffset);
        raf.seek(bl * blockSize);  // todo: check overhead in case of linear read
        byte[] readBytes = new byte[blockSize];
        raf.read(readBytes);
        Validate.isTrue(Arrays.equals(block, readBytes));
    }

    /**  create unique content for each block */
    private static byte[] getBlock(int bl, int size, int seedOffset) {
        // too slow. return RandomBytes.create(size, bl + seedOffset);
        byte[] bytes = new byte[size];
        byte fillByte = (byte) ((bl + seedOffset) % 256);
        Arrays.fill(bytes, fillByte);

        // add something "significant" content
        int pos = bl % (size / 2);
        int value = bl + seedOffset;
        bytes[pos++] = (byte) (value & 0xFF);
        bytes[pos++] = (byte) (value >> 8 & 0xFF);
        bytes[pos++] = (byte) (value >> 16 & 0xFF);
        bytes[pos++] = (byte) (value >> 24 & 0xFF);

        return bytes;
    }

}
