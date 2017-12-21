import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class CryptoLimitTest {

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            fixOld();
        }
        if (args.length == 2) {
            fixNew();
        }
        int maxKeySize = Cipher.getMaxAllowedKeyLength("AES/CBC/NoPadding");
        System.out.println("aes keySize=" + maxKeySize);

        testAes256();
    }

    // new "feature" with JDK 8u151
    public static void fixNew() throws Exception {
        java.security.Security.setProperty("crypto.policy", "unlimited");
    }

    // "HACK" fix java unlimited crypto.     
    // Note: this does not fix "getMaxAllowedKeyLength".
    public static void fixOld() throws Exception {
        Field isRestricted = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
        if (Modifier.isFinal(isRestricted.getModifiers())) {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(isRestricted, isRestricted.getModifiers() & ~Modifier.FINAL);
        }
        isRestricted.setAccessible(true);
        isRestricted.set(null, Boolean.FALSE);
    }

    // just do "any" 256bit AES operation (with pointless data)
    // throws "java.security.InvalidKeyException: Illegal key size" in case of limited crypto
    private static void testAes256() throws Exception {
        byte[] key = new byte[32]; // 256 bit
        byte[] iv = new byte[16];
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        AlgorithmParameterSpec parameterSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
        cipher.doFinal(new byte[16]);
    }
}