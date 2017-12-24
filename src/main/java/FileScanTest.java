import java.io.File;

public class FileScanTest {

    public static void main(String[] args) throws Exception {
        int nr = 100000;
        String name = "test1k";
        int size = 1000;
        int seed = 1;
        String workdir = System.getProperty("user.dir");
        String dir = workdir + "/tmp-" + System.currentTimeMillis();
        new File(dir).mkdir();

        RandomFileTool.main(new String[] {"c", name, "" + seed, "" + size, "" + nr, dir});
        FileScan.main(new String[] {"u", dir});
        FileScan.main(new String[] {"u", dir});
        FileScan.main(new String[] {"h", dir});
        RandomFileTool.main(new String[] {"d", name, "" + seed, "" + size, "" + nr, dir});

        new File(dir + "/.scan-index").delete();
        new File(dir).delete();

    }

}
