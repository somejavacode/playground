package ufw;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamTool {

    /**
     * copy input stream to output stream till input stream is "empty" (i.e. EOF)
     * @param is source
     * @param os destination
     * @param buffSize buffer size for read/write
     * @throws IOException in case of stream problems
     */
    public static void copyAll(InputStream is, OutputStream os, int buffSize) throws IOException {
        byte[] buffer = new byte[buffSize];
        int readBytes;
        while ((readBytes = is.read(buffer)) > 0) {
            os.write(buffer, 0, readBytes);
        }
    }

    /**
     * copy defined number of bytes from input stream to output stream. byte number is enforced.
     *
     * @param is source
     * @param os destination
     * @param buffSize buffer size for read/write
     * @param bytes number of bytes to copy
     * @throws IOException if not all bytes could be copied or in case of stream problems
     */
    public static void copy(InputStream is, OutputStream os, int buffSize, int bytes) throws IOException {
        if (bytes < 1) {
            return;
        }
        int remaining = bytes;
        byte[] buffer = new byte[buffSize];

        while (remaining > 0) {
            // number of bytes that could be read
            int readSize = remaining > buffSize ? buffSize : remaining;
            int readBytes = is.read(buffer, 0, readSize);
            if (readBytes == -1) {
                throw new IOException("got early EOF. bytes missing=" + remaining);
            }
            os.write(buffer, 0, readBytes);
            remaining -= readBytes;
        }
    }
}
