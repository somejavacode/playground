import ufw.Log;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;

public class Launcher {

    public static void main(String[] args) throws Exception {
        // just messing around for now, need to test with JDK9

        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Log.info("current pid " + pid);


        // new variant
        ProcessBuilder pb = new ProcessBuilder(args);
        // this is default: "Indicates that subprocess I/O will be connected to the current Java process over a pipe."
        // pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectOutput(new File("output.log"));
//        pb.redirectErrorStream(true);
        pb.redirectError(new File("error.log"));
        Process p1 = pb.start();

//        Streamer out = new Streamer("stdout", p1.getInputStream(), null);
//        Thread outThread = new Thread(out, "streamer-stdout");
//        outThread.setDaemon(true); // don't block VM shutdown? maybe
//        outThread.start();

        // Log.info("started with pid=" + p1.getPid());  //  JDK9 only
        // http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id

        Thread.sleep(3000);

        p1.destroy();

        p1.waitFor();

        Log.info("done");

        // old variant
        //Process p = Runtime.getRuntime().exec("cmd.exe");


    }

    /**
     * class that will copy input to output with a separate thread. finish with EOF
     */
    private static class StreamPipe implements Runnable {
        private String name;
        private InputStream is;
        private OutputStream os;

        public StreamPipe(String name, InputStream is, OutputStream os) {
            this.name = name;
            this.is = is;
            this.os = os;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[8192];
            int total = 0;
            try {
                int bytes;
                while ((bytes = is.read(buffer)) != -1) {
                    if (os != null) {  // enable "/dev/null" option
                        os.write(buffer, 0, bytes);
                    }
                    total += bytes;
                }
            }
            catch (Throwable t) {
                Log.error("exception during copy. after bytes=" + total, t);
                return;
            }
            Log.info("finished " + name + " after " + total + " bytes");
        }
    }
}
