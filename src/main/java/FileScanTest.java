import ufw.Timer;

import java.io.File;

public class FileScanTest {

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            // default values 10k
            args = new String[] {"test10k", "1", "10000", "10000"};
        }
        else if (args[0].equals("5k")) {
            args = new String[] {"test5k", "1", "5000", "20000"};
        }
        else if (args[0].equals("1k")) {
            args = new String[] {"test1k", "1", "1000", "100000"};
        }
        String name = args[0];
        long seed = Long.parseLong(args[1]);
        int size = Integer.parseInt(args[2]);
        int nr = Integer.parseInt(args[3]);

        String workDir = System.getProperty("user.dir");
        String dir = workDir + "/tmp-" + System.currentTimeMillis();
        new File(dir).mkdir();

        RandomFileTool.main(new String[] {"c", name, "" + seed, "" + size, "" + nr, dir});
        Timer t = new Timer("FileScan update: " + nr + " files", false);
        FileScan.main(new String[] {"u", dir});
        t.stop(true);
        t = new Timer("FileScan update: " + nr + " files", false);
        FileScan.main(new String[] {"u", dir});
        t.stop(true);
        t = new Timer("FileScan summary hash: " + nr + " files", false);
        FileScan.main(new String[] {"h", dir});
        t.stop(true);
        RandomFileTool.main(new String[] {"d", name, "" + seed, "" + size, "" + nr, dir});

        new File(dir + "/.scan-index").delete();
        new File(dir).delete();

    }

}
