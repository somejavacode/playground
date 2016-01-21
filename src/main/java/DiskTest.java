import ufw.RandomBytes;
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
        int mega = 10;
        int blockSize = 8 * 1024;
        int seedOffset = 2351116;

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

        String fileName = "testfile_" + mega + "M";

        // clean up
        File file = new File(fileName);
        if (file.exists()) {
            Validate.isTrue(file.delete(), "failed to delete existing file " + fileName);
        }

        // todo: is it threadsafe?
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");  // mode: crappy magic Strings
        raf.setLength(size);

        int count = size / blockSize; // assume "int"

        for (int loop = 0; loop < loops; loop++) {

            // linear write
            Timer t = new Timer("linear write " + mega + "MB", true);
            for (int bl = 0; bl < count; bl++) {
                writeBlock(raf, bl, blockSize, seedOffset);
            }
            t.stop(true);

            // linear read
            t = new Timer("linear read " + mega + "MB", true);
            for (int bl = 0; bl < count; bl++) {
                validateBlock(raf, bl, blockSize, seedOffset);
            }
            t.stop(true);
        }

        raf.close();



        // MappedByteBuffer might be an alternative

    }

    private static void writeBlock(RandomAccessFile raf, int bl, int blockSize, int seedOffset) throws Exception {
        byte[] block = RandomBytes.create(blockSize, bl + seedOffset);
        raf.seek(bl * blockSize);  // todo: check overhead in case of linear read
        raf.write(block);
    }

    private static void validateBlock(RandomAccessFile raf, int bl, int blockSize, int seedOffset) throws Exception {
        byte[] block = RandomBytes.create(blockSize, bl + seedOffset);
        raf.seek(bl * blockSize);  // todo: check overhead in case of linear read
        byte[] readBytes = new byte[blockSize];
        raf.read(readBytes);
        Validate.isTrue(Arrays.equals(block, readBytes));
    }
}
