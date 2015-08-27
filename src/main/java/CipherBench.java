import ufw.Log;
import ufw.SystemInfo;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * single file cipher speed test
 */

public class CipherBench {

    public static void main(String[] args) throws Exception {

        String cipherMode = "AES/CBC/NoPadding";
        if (args.length > 0) {
            cipherMode = args[0];
        }
        String keyAlgorithm = cipherMode.substring(0, cipherMode.indexOf('/'));

        int blockSize = 16384;
        if (args.length > 1) {
            blockSize = Integer.parseInt(args[1]);
        }

        long timeLimit = 5; // seconds test time
        if (args.length > 2) {
            timeLimit = Integer.parseInt(args[2]);
        }

        int keySize = 16; // 128 bit
        if (args.length > 3) {
            keySize = Integer.parseInt(args[3]);
        }

        String providerName = "SunJCE";
        if (args.length > 4) {
            providerName = args[4];
        }

        long seed = 4234234; // fixed seed to get repeatable random values.
        if (args.length > 5) {
            seed = Long.parseLong(args[5]);
        }

        SystemInfo.show();

        Log.info("Start cipher test. mode=" + cipherMode + ", provider=" + providerName + ", keySize=" + (keySize * 8) +
                "bit, blockSize=" + blockSize + "bytes, encryptTime=" + timeLimit + "s" + ", seed=" + seed);

        doEncryptDecrypt(cipherMode, keyAlgorithm, blockSize, timeLimit, keySize, providerName, seed, true);
        doEncryptDecrypt(cipherMode, keyAlgorithm, blockSize, timeLimit, keySize, providerName, seed, false);
    }

    private static void doEncryptDecrypt(String cipherMode, String keyAlgorithm, int blockSize, long timeLimit,
                                         int keySize, String providerName, long seed, boolean warmUp) throws Exception {
        Random rand = seed == 0 ? new Random() : new Random(seed);
        byte[] input = getRandomBytes(rand, blockSize);
        byte[] key = getRandomBytes(rand, keySize);
        byte[] iv = getRandomBytes(rand, 16);
        long start = System.nanoTime();
        long end = start + (timeLimit * 1000000000L);
        Cipher cipher = getCipher(key, true, iv, keyAlgorithm, cipherMode, providerName);
        long time = 0;
        long count = 0;
        byte[] temp = input;
        do {
            temp = cipher.doFinal(temp);
            count++;
            time = System.nanoTime();
        }
        while (time < end);

        double megaBytes = blockSize * count / (1024 * 1024);
        double seconds = (time - start) / 1E9;
        long millis = (time - start) / 1000000;
        DecimalFormat speedFormat = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        String mpsString = speedFormat.format(megaBytes / seconds);

        String logPrefix = warmUp ? "(warmUp) " : "";

        Log.info(logPrefix + "encrypted " + count + " blocks in " + millis + "ms. speed=" + mpsString + "MiByte/s");

        // now decrypt same count.
        start = System.nanoTime();
        cipher = getCipher(key, false, iv, keyAlgorithm, cipherMode, providerName);
        for (int i = 0; i < count; i++) {
            temp = cipher.doFinal(temp);
        }
        long finish = System.nanoTime();

        seconds = (finish - start) / 1E9;
        millis = (finish - start) / 1000000;
        mpsString = speedFormat.format(megaBytes / seconds);
        Log.info(logPrefix + "decrypted " + count + " blocks in " + millis + "ms. speed=" + mpsString + "MiByte/s");

        if (!Arrays.equals(input, temp)) {
            throw new IllegalArgumentException("validation of decrypted message failed");
        }
    }

    private static byte[] getRandomBytes(Random rand, int count) {
        byte[] ret = new byte[count];
        rand.nextBytes(ret);
        return ret;
    }

    private static Cipher getCipher(byte[] key, boolean encrypt, byte[] iv, String keyAlgorithm,
                                    String cipherMode, String provider) throws Exception {

        SecretKeySpec keySpec = new SecretKeySpec(key, keyAlgorithm);
        AlgorithmParameterSpec parameterSpec = new IvParameterSpec(iv);
        Cipher cipher = provider == null ? Cipher.getInstance(cipherMode) : Cipher.getInstance(cipherMode, provider);
        int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
        cipher.init(mode, keySpec, parameterSpec);
        return cipher;
    }

}
