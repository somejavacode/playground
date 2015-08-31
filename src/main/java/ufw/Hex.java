package ufw;

/**
 * Static methods for converting to and from hexadecimal strings.
 */
public class Hex {

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * static methods only, no instances
     */
    private Hex() {
    }

    /**
     * convert byte array to hexadecimal String (two characters per byte e.g. 3EF35A)
     *
     * @param ba the array to convert
     * @param offset byte offset
     * @param length number of bytes to dump
     * @return the String representation
     */
    public static String toString(byte[] ba, int offset, int length) {
        Validate.notNull(ba);
        Validate.isTrue(offset >= 0);
        Validate.isTrue(length > 0);
        Validate.isTrue(offset + length <= ba.length);

        char[] buf = new char[length * 2];
        int j = 0;
        int k;

        for (int i = offset; i < offset + length; i++) {
            k = ba[i];
            buf[j++] = HEX_DIGITS[(k >>> 4) & 0x0F];
            buf[j++] = HEX_DIGITS[k & 0x0F];
        }
        return new String(buf);
    }

    /**
     * convert byte array to hexadecimal String (two characters per byte e.g. 3EF35A)
     *
     * @param ba the array to convert
     * @return the String representation
     */
    public static String toString(byte[] ba) {
        return toString(ba, 0, ba.length);
    }

    /**
     * convert byte array to block of hexadecimal Strings (two characters per byte e.g. 3EF35A)
     *
     * @param ba the array to convert
     * @return the String block representation
     */
    public static String toStringBlock(byte[] ba) {
        return toStringBlock(ba, 32, 8, true);
    }

    /**
     * convert byte array to block of hexadecimal Strings (two characters per byte e.g. 3EF35A)
     *
     * @param ba the array to convert
     * @return the String block representation
     */
    public static String toStringBlock(byte[] ba, int bytesPerLine, int pipeEvery, boolean showChars) {
        StringBuilder result = new StringBuilder(ba.length); // not precisely
        int pos = 0;

        while (pos < ba.length) {
            // position as hex string
            result.append(String.format("%04X: ", pos));

            // number of bytes to print in this line
            int bytesRemaining = ba.length - pos;
            int bytes = bytesRemaining >= bytesPerLine ? bytesPerLine : bytesRemaining;
            for (int p = 0; p < bytes; p++) {
                result.append(toString(ba, pos + p, 1));
                if (p < bytes - 1) {  // not after last byte
                    result.append(" ");
                    if ((p + 1) % pipeEvery == 0) {
                        result.append("| ");
                    }
                }
            }
            if (showChars) {
                if (bytes < bytesPerLine) {
                    int blanks = 3 * (bytesPerLine - bytes);  // usual blanks
                    int missingPipes = (bytesPerLine -1) / pipeEvery - (bytes - 1) / pipeEvery;
                    blanks += missingPipes * 2;
                    for (int i = 0; i < blanks; i++) {
                        result.append(" "); // not that elegant
                    }
                }
                result.append(" - "); // separate hex from chars
                for (int p = 0; p < bytes; p++) {
                    result.append(printChar(ba[pos + p]));
                    if (p < bytes - 1) {  // not after last byte
                        if ((p + 1) % pipeEvery == 0) {
                            result.append("|");
                        }
                    }
                }
            }
            result.append("\n");

            pos += bytes;  // for next line
        }
        return result.toString();
    }

    /**
     * convert a hexadecimal String to byte array
     *
     * @param hex the hex String
     * @return the according byte array
     */
    public static byte[] fromString(String hex) {
        Validate.notNull(hex);
        int len = hex.length();
        Validate.isTrue(len % 2 == 0, "number of characters must be even. got ", len);

        byte[] buf = new byte[((len + 1) / 2)];
        int i = 0;
        int j = 0;
        if ((len % 2) == 1) {
            buf[j++] = (byte) fromDigit(hex.charAt(i++));
        }

        while (i < len) {
            buf[j++] = (byte) ((fromDigit(hex.charAt(i++)) << 4) | fromDigit(hex.charAt(i++)));
        }
        return buf;
    }

    /**
     * Returns the number from 0 to 15 corresponding to the hex digit <i>ch</i>.
     */
    private static int fromDigit(char ch) {
        if (ch >= '0' && ch <= '9')  {
            return ch - '0';
        }
        if (ch >= 'A' && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if (ch >= 'a' && ch <= 'f') {
            return ch - 'a' + 10;
        }
        throw new IllegalArgumentException("invalid hex digit '" + ch + "'");
    }

    // return printable character or ".", NOTE: currently strips also > 127
    private static char printChar(byte ch) {
        if (ch >= 32) {
            return (char)ch;
        }
        else {
            return '.';
        }
    }

}
