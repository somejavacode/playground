import ufw.Log;
import ufw.Timer;

import java.security.SecureRandom;

public class SecureRandomTest {

    public static void main(String[] args) {
        SecureRandom sr = new SecureRandom();
        Log.info("algorithm=" + sr.getAlgorithm());  // windows: SHA1PRNG, linux: NativePRNG
        int size = 8;
        int maxSize = 1024 * 1024;
        while (true) {
            Timer t = new Timer("generate " + size + " random bytes", false);
            sr.nextBytes(new byte[size]);
            t.stop(true);
            size *= 2;
            if (size > maxSize) {
                break;
            }
        }
    }
}
