import java.io.RandomAccessFile;

public class PePatch {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.out.println("PePatch <executable>  .. read OS version");
            System.out.println("PePatch <executable> <version>  .. write OS version");
            return;
        }
        int writeVersion = 0;
        String mode = "r";
        if (args.length > 1) {
            writeVersion = Integer.parseInt(args[1]);
            mode = "rw";
        }
        RandomAccessFile executable = new RandomAccessFile(args[0], mode);

        // check exe header
        short exeHeader = readShort(executable);
        if (exeHeader != 0x5a4d) {
            System.out.println("not an executable");
            return;
        }

        // see https://en.wikipedia.org/wiki/Portable_Executable
        // read PE header offset
        executable.seek(0x3C); // "pointer to PE header"
        int pEoffset = readInt(executable);

        // check exe header
        executable.seek(pEoffset);
        int pEheader = readInt(executable);
        if (pEheader != 0x4550) {
            System.out.println("pe header not found.");
            return;
        }

        // read major OS
        int offsetMajorOs = pEoffset + 0x40;
        executable.seek(offsetMajorOs);
        short osVersion =  readShort(executable);
        System.out.println("current os version: " + osVersion);

        // write major OS if necessary
        if (writeVersion > 0 && writeVersion != osVersion) {
            executable.seek(offsetMajorOs);
            writeShort(executable, (short)writeVersion);
            System.out.println("changed os version to: " + writeVersion);
        }

        // read major Image
        int offsetMajorImage = pEoffset + 0x48;
        executable.seek(offsetMajorImage);
        short imageVersion =  readShort(executable);
        System.out.println("current image version: " + imageVersion);

        // write major image if necessary
        if (writeVersion > 0 && writeVersion != imageVersion) {
            executable.seek(offsetMajorImage);
            writeShort(executable, (short)writeVersion);
            System.out.println("changed image version to: " + writeVersion);
        }
        executable.close();
    }

    /**
     * write short with little endian
     */
    private static void writeShort(RandomAccessFile file, short value) throws Exception {
        file.write(value & 0xFF);
        file.write((value >> 8) & 0xFF);
    }

    /**
     * read short with little endian<br/>
     * Note: file.readShort() is big endian
     */
    private static short readShort(RandomAccessFile file) throws Exception {
        return (short) (readByte(file) + (readByte(file) << 8));
    }

    /**
     * read int with little endian<br/>
     * Note: file.readInt() is big endian
     */
    private static int readInt(RandomAccessFile file) throws Exception {
        return readByte(file) + (readByte(file) << 8) + (readByte(file) << 16) + (readByte(file) << 24);
    }

    /**
     * read byte from file, convert to unsigned, fail in case of EOF
     */
    private static int readByte(RandomAccessFile file) throws Exception {
        int value = file.read();
        if (value == -1) {
            throw new RuntimeException("Unexpected EOF");
        }
        return value & 0xff; // convert to "unsigned" byte "0-255"
    }
}