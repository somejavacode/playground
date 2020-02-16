import ufw.Hex;
import ufw.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;

public class ECSunTest {

    public static void main(String[] args) throws Exception {

        String algorithm = "EC";
        String curve = "secp256k1";

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(new ECGenParameterSpec(curve));
        KeyPair keyPair = keyGen.generateKeyPair();

        Log.info("public " + Hex.toString(keyPair.getPublic().getEncoded()));
        Log.info("private " + Hex.toString(keyPair.getPrivate().getEncoded()));

    }

}
