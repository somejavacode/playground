import ufw.RandomInputStream;
import ufw.RandomOutputStream;
import ufw.StreamTool;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class RandomFileTool {

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.println("syntax: create or verify RandomFileTool C|V Filename Seed Size");
            return;
        }
        String file = args[1];
        long seed = Long.parseLong(args[2]);
        int size = Integer.parseInt(args[3]);

        if (args[0].toLowerCase().equals("c")) {
            FileOutputStream fos = new FileOutputStream(file);
            RandomInputStream ris = new RandomInputStream(seed, size);
            StreamTool.copyAll(ris, fos, 8192);
        }
        else if (args[0].toLowerCase().equals("v")) {
            FileInputStream fis = new FileInputStream(file);
            RandomOutputStream ros = new RandomOutputStream(seed, size);
            StreamTool.copyAll(fis, ros, 8192);
            int missing = ros.getMissingByteCount();
            if (missing > 0) {
                throw new RuntimeException("missing bytes: " + missing);
            }
        }
    }

}
