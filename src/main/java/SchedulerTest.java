import ufw.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerTest {

    public static void main(String[] args) throws Exception {

        Log.info("start");
        final long start = System.currentTimeMillis();

        Runnable runnable = new Runnable() {
            public void run() {
                Log.info("run start " + (System.currentTimeMillis() - start));
                try {
                    Thread.sleep(1000);
                    // scheduleAtFixedRate: Thread.sleep(6000); more than period: interval will be longer (no warning).
                }
                catch (InterruptedException e) {
                    Log.info("interrupted");
                }
                Log.info("run done " + (System.currentTimeMillis() - start));
            }
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        long initial = 5000 - start % 5000; // try to align to 5s
        exec.scheduleAtFixedRate(runnable , initial, 5000, TimeUnit.MILLISECONDS);
        // pointless, interval depends on execution time.
        // exec.scheduleWithFixedDelay(runnable , initial, 5000, TimeUnit.MILLISECONDS);
        Thread.sleep(16000);
        // exec.shutdownNow(); // will interrupt runnable
        exec.shutdown();
        Log.info("finish " + (System.currentTimeMillis() - start));

        // Timer timer = new Timer(); // similar old variant
        // timer.scheduleAtFixedRate(); // can specify date "firstTime" (not delay)
    }
}
