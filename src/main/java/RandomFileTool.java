import ufw.Log;
import ufw.RandomInputStream;
import ufw.RandomOutputStream;
import ufw.StreamTool;
import ufw.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RandomFileTool {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("create, verify delete random file(s).\nSyntax: RandomFileTool C|V|D Filename Seed Size [count] [dir]");
            return;
        }
        String file = args[1];
        long seed = Long.parseLong(args[2]);
        int size = Integer.parseInt(args[3]);
        int count = 1;
        if (args.length > 4) {
            count = Integer.parseInt(args[4]);
        }
        String dir = "";
        if (args.length > 5) {
            dir = args[5] + "/";
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                System.out.println("creating dir: " + dir);
                dirFile.mkdirs();
            }
        }

        Timer timer = new Timer("RandomFileTool: " + args[0] + " file=" + file +
                                " seed=" + seed + " size=" + size + " count=" + count, true);
        if (count == 1) {
            processFile(args[0], file, seed, size, dir);
        }
        else {
            int digits = Integer.toString(count).length();
            for (int i = 0; i < count; i++) {
                // format sequence with enough leading zeros
                String fileSeq =  file + String.format("-%0" + digits + "d", i + 1);
                try {
                    processFile(args[0], fileSeq, seed + i, size, dir);
                }
                catch (Exception e) {
                    throw new RuntimeException("problem with file " + fileSeq, e);
                }
            }
        }
        timer.stop(true);
    }

    private static void processFile(String arg, String file, long seed, int size, String dir) throws IOException {
        final String command = arg.toLowerCase();
        final int bufferSize = 8192;
        if (command.equals("c")) {
            try (FileOutputStream fos = new FileOutputStream(dir + file)) {
                RandomInputStream ris = new RandomInputStream(seed, size);
                StreamTool.copyAll(ris, fos, bufferSize);
            }
        }
        else if (command.equals("v")) {
            try (FileInputStream fis = new FileInputStream(dir + file)) {
                RandomOutputStream ros = new RandomOutputStream(seed, size);
                StreamTool.copyAll(fis, ros, bufferSize);
                int missing = ros.getMissingByteCount();
                if (missing > 0) {
                    throw new RuntimeException("missing bytes: " + missing);
                }
            }
        }
        else if (command.equals("d")) {
            if (!new File(dir + file).delete()) { // no hard exit, just warning
                Log.warn("Failed to delete: " + file);
            }
        }
    }

}
