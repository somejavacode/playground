import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class DumpCert {
    public static void main(String[] args) throws Exception {
        InputStream stream = null;
        if (args.length > 0) {
            stream = new FileInputStream(args[0]);
        }
        else {
            stream = System.in;
        }

        X509Certificate certificate = getCertificate(stream);
        stream.close();
        // this is DER
        byte[] bytes = certificate.getEncoded();

        System.out.println(certificate.toString());
        // System.out.println(Base64.toBase64String(bytes));

        // fingerprints:
        printFingerPrint("md5", bytes);
        printFingerPrint("sha1", bytes);
        printFingerPrint("sha-256", bytes);
    }

    private static X509Certificate getCertificate(InputStream stream) throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(stream);
    }

    private static void printFingerPrint(String digest, byte[] certificate) throws Exception {
        MessageDigest md = MessageDigest.getInstance(digest);
        byte[] fp = md.digest(certificate);
        StringBuilder builder = new StringBuilder();
        builder.append(getHex(fp[0]));
        for (int i = 1; i < fp.length; i++) {
            builder.append(":");
            builder.append(getHex(fp[i]));
        }
        System.out.println(digest + ": " + builder.toString());
    }

    // get two digit hex string 00..FF
    private static String getHex(byte value) {
        String hex = Integer.toHexString(value & 0xFF).toUpperCase();
        return hex.length() == 2 ? hex : "0" + hex;
    }
}
