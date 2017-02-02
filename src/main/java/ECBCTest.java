import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ufw.Hex;
import ufw.Log;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;

public class ECBCTest {

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider()); // add BC
        String provider  = "BC";

        String algorithm = "EC";
        String curve = "secp256k1";

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm, provider);
//        keyGen.initialize(new ECGenParameterSpec(curve)); // JDK spec
        keyGen.initialize(ECNamedCurveTable.getParameterSpec(curve)); // BouncyCastle only, might have more curves
        KeyPair keyPair = keyGen.generateKeyPair();

        Log.info("public " + Hex.toString(keyPair.getPublic().getEncoded()));
        Log.info("private " + Hex.toString(keyPair.getPrivate().getEncoded()));

    }

}
