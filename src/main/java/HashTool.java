import ufw.Hex;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * tool similar to md5sum, sha1sum, etc.
 * <p>
 * may add later support for hash in filename (mircosoft distribution files like e.g. )
 */
public class HashTool {

    public static void main(String[] args) throws Exception {
        String algorithm = "SHA-256";
        if (args.length > 0) {
            algorithm = args[0];
        }
        InputStream is = null;

        // second argument file name
        if (args.length > 1) {
            is = new FileInputStream(args[1]);
        }
        // use stdin if no file argument
        else {
            is = System.in;
        }
        MessageDigest md = MessageDigest.getInstance(algorithm);

        byte[] buffer = new byte[4096];
        int bytes = 0;
        while ((bytes = is.read(buffer)) > 0) {
            md.update(buffer, 0, bytes);
        }
        is.close();

        byte[] hash = md.digest();
        System.out.println(Hex.toString(hash));
    }

}
