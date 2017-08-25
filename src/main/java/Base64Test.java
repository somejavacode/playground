import ufw.Hex;
import ufw.Log;
import ufw.Validate;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Base64Test {

    public static void main(String[] args) throws Exception {
        Random rand = new Random(4711);

        for (int size = 0; size < 20; size++) {
            byte[] bytes = new byte[size];
            rand.nextBytes(bytes);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            Log.info("enc: " + encoded);
            byte[] decoded = Base64.getDecoder().decode(encoded);
            Log.info("dec: " + Hex.toString(decoded));
            Validate.isTrue(Arrays.equals(bytes, decoded));
        }
    }
}
