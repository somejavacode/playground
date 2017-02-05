import ufw.Timer;

import java.io.File;

/**
 * runner controls execution of java processes or threads based on command file.
 */
public class Runner {

    // runner command file format ...
    private static final String SAMPLE =
            // P launch process, T run thread, M run in main,
            // W blocking wait (obvious for M), B branch execution, T wait for time
            // count: parallel for P/T, repeats for M
            // optional checks, stdout? return code
            // TODO: socket vs stdout:
            // TODO: common logging (send back to runner)
            // invocation: args for main
            // hand over listen port? communicate via stdout/in, return created process id..
            // problem "singletons" e.g. system properties, stdout ..
            // T/P, id, when, how often parallel (thread/process), main class/command, args

            "P:T1:0:1:java -version" +
            "P:T2:1000:1:java nonExistingClass";

    // test example "java Launcher java ShellTest"
    public static void main(String[] args) throws Exception {

        // executionId: date + command file


        // new variant
        ProcessBuilder pb = new ProcessBuilder(args);
        // this is default: "Indicates that subprocess I/O will be connected to the current Java process over a pipe."
        // pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectOutput(new File("output.log"));
//        pb.redirectErrorStream(true);
        pb.redirectError(new File("error.log"));
        Timer t = new Timer("launcher", false);
        Process p1 = pb.start();

        p1.waitFor();
        t.stop(false);

    }

}
