import ufw.Log;
import ufw.RandomInputStream;
import ufw.RandomOutputStream;
import ufw.StreamTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RandomFileTool {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("syntax: create or verify RandomFileTool C|V|D Filename Seed Size [count]");
            return;
        }
        String file = args[1];
        long seed = Long.parseLong(args[2]);
        int size = Integer.parseInt(args[3]);
        int count = 1;
        if (args.length > 4) {
            count = Integer.parseInt(args[4]);
        }

        if (count == 1) {
            processFile(args[0], file, seed, size);
        }
        else {
            int digits = Integer.toString(count).length();
            for (int i = 0; i < count; i++) {
                // format sequence with enough leading zeros
                String fileSeq = file + String.format("-%0" + digits + "d", i + 1);
                try {
                    processFile(args[0], fileSeq, seed + i, size);
                }
                catch (Exception e) {
                    throw new RuntimeException("problem with file " + fileSeq, e);
                }
            }
        }
    }

    private static void processFile(String arg, String file, long seed, int size) throws IOException {
        final String command = arg.toLowerCase();
        final int bufferSize = 8192;
        if (command.equals("c")) {
            FileOutputStream fos = new FileOutputStream(file);
            RandomInputStream ris = new RandomInputStream(seed, size);
            StreamTool.copyAll(ris, fos, bufferSize);
        }
        else if (command.equals("v")) {
            FileInputStream fis = new FileInputStream(file);
            RandomOutputStream ros = new RandomOutputStream(seed, size);
            StreamTool.copyAll(fis, ros, bufferSize);
            int missing = ros.getMissingByteCount();
            if (missing > 0) {
                throw new RuntimeException("missing bytes: " + missing);
            }
        }
        else if (command.equals("d")) {
            if (!new File(file).delete()) { // no hard exit, just warning
                Log.warn("Failed to delete: " + file);
            }
        }
    }

}
