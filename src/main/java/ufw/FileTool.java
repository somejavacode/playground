package ufw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class FileTool {

    /**
     * test file for certain attributes. any "true" parameter will be checked to be "true".
     * first failed test throws RuntimeException.
     * <p>
     * Note: pointless combination of tests are not rejected (e.g. isFile AND isDirectory will always fail)
     *
     * @param file the file to check
     * @param exists  fails if it does not exists
     * @param existsNot fails if it exists
     * @param isFile fails if it is not a file
     * @param isDirectory fails if it is not directory
     * @param canRead fails if cannot read
     * @param canWrite fails if cannot write
     */
    public static void testProperties(File file, boolean exists, boolean existsNot, boolean isFile, boolean isDirectory, boolean canRead, boolean canWrite) {
        Validate.notNull(file, "File is null.");
        if (exists && !file.exists()) {  // rely on "short circuit"
            throw new RuntimeException("File does not exist. " + file);
        }
        if (existsNot && file.exists()) {  // rely on "short circuit"
            throw new RuntimeException("File exists. " + file);
        }
        if (isFile && !file.isFile()) {
            throw new RuntimeException("File is not a File. " + file);  // sounds confusing
        }
        if (isDirectory && !file.isDirectory()) {
            throw new RuntimeException("File is not a Directory. " + file);
        }
        if (canRead && !file.canRead()) {
            throw new RuntimeException("cannot read File. " + file);
        }
        if (canWrite && !file.canWrite()) {
            throw new RuntimeException("cannot write File. " + file);
        }
    }

    /**
     * create file. assume success. assume can write.
     *
     * @param file the file to create
     * @throws IOException in case of io problems
     */
    public static void createOK(File file) throws IOException {
        boolean success = file.createNewFile();
        if (!success) {
            throw new RuntimeException("file create failed: " + file);
        }
        if (!file.canWrite()) {
            throw new RuntimeException("cannot write to crated file:" + file);
        }
    }

    /**
     * create directory. assume success. assume can write.
     *
     * @param file the file to create
     * @throws IOException in case of io problems
     */
    public static void mkDirsOK(File file) throws IOException {
        boolean success = file.mkdirs();
        if (!success) {
            throw new RuntimeException("directory create failed: " + file);
        }
        if (!file.canWrite()) {
            throw new RuntimeException("cannot write to created directory: " + file);
        }
    }

    /**
     * copy defined number of bytes from RandomAccessFile source to destination File
     *
     * @param randomAccessFile the source file
     * @param offset offset in source file
     * @param dest the destination file
     * @param byteCount the number of bytes to transfer
     * @throws IOException in case of io problems
     */
    public static void readFileFromRAF(RandomAccessFile randomAccessFile, int offset, File dest, int byteCount) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buffer = new byte[16384];
            int remaining = byteCount;
            int pos = offset;
            while (remaining > 0) {
                int bytes = remaining > buffer.length ? buffer.length : remaining;
                randomAccessFile.seek(pos);
                int readBytes = randomAccessFile.read(buffer, 0, bytes);
                Validate.isTrue(readBytes == bytes, "missing bytes from file");
                fos.write(buffer, 0, bytes);
                remaining -= bytes;
                pos += bytes;
            }
        }
    }

    /**
     * write full content of file to output stream
     *
     * @param file file to read
     * @param outputStream stream to write to (NOTE: stream will be left open)
     * @throws IOException in case of io problems
     */
    public static void writeFileToStream(File file, OutputStream outputStream) throws IOException {
        // try with resources, using AutoClosable
        try (FileInputStream fis = new FileInputStream(file)) {
            // append full content
            byte[] buffer = new byte[16384];
            int remaining = (int) file.length();
            while (remaining > 0) {
                int bytes = remaining > buffer.length ? buffer.length : remaining;
                int readBytes = fis.read(buffer, 0, bytes);  // todo: read may be smarter. no need to calculate bytes?
                Validate.isTrue(bytes == readBytes, "missing bytes from input steam");
                outputStream.write(buffer, 0, bytes);
                remaining -= bytes;
            }
        }
    }
}
