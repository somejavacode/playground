// access block device in java-
// sample 1: via http://stackoverflow.com/questions/2108313/how-to-access-specific-raw-data-on-disk-from-java

import ufw.Hex;
import ufw.Log;

import java.io.File;
import java.io.RandomAccessFile;

public class RawDisk {

    public static void main(String[] args) throws Exception {

        RandomAccessFile diskAccess;

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            // https://support.microsoft.com/en-us/kb/100027
            // TODO: check non-admin case
            File diskRoot = new File("\\\\.\\PhysicalDrive0");
            diskAccess = new RandomAccessFile(diskRoot, "r"); // open read only
        }
        else if (os.contains("linux")) {
            File diskRoot = new File("/dev/sda");
            diskAccess = new RandomAccessFile(diskRoot, "r"); // open read only
        }
        else {
            throw new RuntimeException("OS not supported: " + os);
        }


        byte[] content = readSector(diskAccess, 0);
        Log.info("sector 0 content:\n" + Hex.toStringBlock(content));

        content = readSector(diskAccess, 33333);
        Log.info("sector 33333 content:\n" + Hex.toStringBlock(content));


        int repeats = 5000;
        Log.info("start loading " +repeats + " sectors");
        long start = System.nanoTime();

        for (int i = 0; i < repeats; i++) {
            readSector(diskAccess, i);
        }
        long duration = System.nanoTime() - start;
        double megaBytePerSecond = 512.0 * repeats * 1024 / duration;
        Log.info("loading " + repeats + " sectors took " + duration / 1000000 + "ms. speed=" + megaBytePerSecond + "MiByte/s");

    }

    private static byte[] readSector(RandomAccessFile diskAccess, int nr) throws Exception {
        final int bytePerSector = 512;
        byte[] content = new byte[bytePerSector];
        diskAccess.seek(bytePerSector * nr);
        diskAccess.readFully(content);
        return content;
    }

}