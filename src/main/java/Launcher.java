import ufw.Log;

import java.lang.management.ManagementFactory;

public class Launcher {

    public static void main(String[] args) throws Exception {
        // just messing around for now, need to test with JDK9


        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        Log.info("current pid " + pid);


        // new variant
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/e echo hallo");
        pb.redirectErrorStream(true);
        Process p1 = pb.start();  // process is created but no window?

        // Log.info("started with pid=" + p.getPid());  // TODO: cannot get pid, fixed with JDK9
        // http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id

        p1.waitFor();


        // old variant
        //Process p = Runtime.getRuntime().exec("cmd.exe");


    }
}
