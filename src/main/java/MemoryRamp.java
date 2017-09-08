import ufw.Log;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.Random;

public class MemoryRamp {

    /**
     * allocate and hold specific amount of memory.
     * <p>
     * function will sleep endless after success to lock memory. terminate with ctrl-c.
     *
     * @param args arg[0] memory limit in MiB, arg[1] != null: random fill
     * @throws Exception in case of problems
     */
    public static void main(String[] args) throws Exception {

        int bytesPerMiB = 1024 * 1024;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Log.info("maximum heap: " + memoryBean.getHeapMemoryUsage().getMax() / bytesPerMiB + "MiB");

        ArrayList<byte[]> megs = new ArrayList<byte[]>();
        long seed = 4711;
        int limit = Integer.MAX_VALUE;
        if (args.length > 0) {
            limit = Integer.parseInt(args[0]);
        }
        boolean fillRandom = false;
        if (args.length > 1) {
            fillRandom = true;
        }

        int megas = 0;  // number of allocated megabyte arrays
        Random rand = new Random(seed);
        long start = System.currentTimeMillis();
        int logEvery = 50;

        while (megas < limit) {
            try {
                byte[] mega = new byte[bytesPerMiB];
                if (fillRandom) {
                    rand.nextBytes(mega);
                }
                megas++;
                if (megas % logEvery == 0) {
                    long now = System.currentTimeMillis();
                    long took = now - start;
                    Log.info("so far: " + megas + "MiB. last " + logEvery + "MiB took " + took + "ms");
                    start = now;
                }
                megs.add(mega); // just "pin" it to avoid GC
            }
            catch (OutOfMemoryError oom) {
                Log.info("finished with OutOfMemoryError after MiB count: " + megas);
                return;
            }
        }
        Thread.sleep(Long.MAX_VALUE);  // almost forever
    }
}
