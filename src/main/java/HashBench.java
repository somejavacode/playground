import ufw.Log;
import ufw.SystemInfo;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

/**
 * hash speed test
 */

public class HashBench {

    public static void main(String[] args) throws Exception {

        String algorithm = "SHA-256";
        if (args.length > 0) {
            algorithm = args[0];
        }

        int blockSize = 16384;
        if (args.length > 1) {
            blockSize = Integer.parseInt(args[1]);
        }

        long timeLimit = 5; // seconds test time
        if (args.length > 2) {
            timeLimit = Integer.parseInt(args[2]);
        }

        String providerName = "SUN";
        if (args.length > 3) {
            providerName = args[3];
        }

        long seed = 4234234; // fixed seed to get repeatable random values.
        if (args.length > 4) {
            seed = Long.parseLong(args[4]);
        }

        SystemInfo.show();

        Log.info("Start hash test. algorithm=" + algorithm + ", provider=" + providerName +
                ", blockSize=" + blockSize + "bytes, hashTime=" + timeLimit + "s" + ", seed=" + seed);

        doHash(algorithm, blockSize, timeLimit, providerName, seed, true);
        doHash(algorithm, blockSize, timeLimit, providerName, seed, false);
    }

    private static void doHash(String algorithm, int blockSize, long timeLimit,
                               String providerName, long seed, boolean warmUp) throws Exception {

        MessageDigest md = MessageDigest.getInstance(algorithm, providerName);

        Random rand = seed == 0 ? new Random() : new Random(seed);
        byte[] input = getRandomBytes(rand, blockSize);
        long start = System.nanoTime();
        long end = start + (timeLimit * 1000000000L);
        long time = 0;
        long count = 0;
        do {
            md.update(input);
            count++;
            time = System.nanoTime();
        }
        while (time < end);
        byte[] hash = md.digest();
        int bits = hash.length * 8;

        double megaBytes = blockSize * count / (1024 * 1024);
        double seconds = (time - start) / 1E9;
        long millis = (time - start) / 1000000;
        DecimalFormat speedFormat = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        String mpsString = speedFormat.format(megaBytes / seconds);

        String logPrefix = warmUp ? "(warmUp) " : "";

        Log.info(logPrefix + "hashed " + count + " blocks in " + millis + "ms. speed=" +
                 mpsString + "MiByte/s. " + bits + "bit");

    }

    private static byte[] getRandomBytes(Random rand, int count) {
        byte[] ret = new byte[count];
        rand.nextBytes(ret);
        return ret;
    }

}
