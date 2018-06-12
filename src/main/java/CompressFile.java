import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressFile {

    private enum Method {

        GZIP("g", ".gz"),
        XZ("z", ".xz");

        private String argument;
        private String extension;

        Method(String name, String extension) {
            this.argument = name;
            this.extension = extension;
        }

        public static Method getByArgs(String argument) {
            for (Method m : Method.values()) {
                if (m.argument.equalsIgnoreCase(argument)) {
                    return m;
                }
            }
            throw new RuntimeException("unknown method with arg: " + argument);
        }
    }

    public static void main(String[] args) throws Exception {
        // CompressFile
        // c compress, x extract,
        // g gzip, z zx
        if (args.length < 3) {
            System.out.println("CompressFile usage:");
            System.out.println("CompressFile [g|z] [c|x] file[.gz|.xz]");
            return;
        }
        boolean compress = false;
        String command = args[1];
        if (command.equalsIgnoreCase("c")) {
            compress = true;
        }
        // must be x for extract
        else if (!command.equalsIgnoreCase("x")) {
            throw new RuntimeException("invalid command " + command);
        }

        Method method = Method.getByArgs(args[0]);

        // check source file (assume no extension)
        String source = args[2];

        if (!compress && !source.endsWith(method.extension)) {
            source += method.extension;
        }

        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            throw new RuntimeException("file not found: " + source);
        }
        FileInputStream fis = new FileInputStream(sourceFile);


        String destination = args[2];
        if (compress && !destination.endsWith(method.extension)) {
            destination += method.extension;
        }
        FileOutputStream fos = new FileOutputStream(destination);
        OutputStream output = null;
        InputStream input = null;
        if (compress) {
            input = fis;
            switch (method) {
                case XZ:
                    output = new XZOutputStream(fos, new LZMA2Options(LZMA2Options.PRESET_MAX));
                    break;
                case GZIP:
                    output = new GZIPOutputStream(fos);  // todo: compression level?
                    break;
            }
        }
        else {
            output = fos;
            switch (method) {
                case XZ:
                    input = new XZInputStream(fis);
                    break;
                case GZIP:
                    input = new GZIPInputStream(fis);
                    break;
            }
        }

        // finally copy input->output
        byte[] buffer = new byte[8192];
        int bytes = 0;
        while ((bytes = input.read(buffer))!= -1) {
            output.write(buffer, 0, bytes);
        }
        input.close();
        output.close();
    }
}
