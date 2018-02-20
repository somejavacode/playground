import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * a tool to test shell integration (cmd.exe in windows)
 * <p>
 * it will generate periodic output to stdout and stderr,
 * report shutdown if detected.
 *
 */
public class ShellTest {

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new HookThread("hook"));

        // option to redirect out/error to file
        if (args.length > 0) {
            System.setOut(new PrintStream(args[0]));
        }
        if (args.length > 1) {
            System.setErr(new PrintStream(args[1]));
        }

        Thread reader = new Thread(new InputReader(), "stdin_reader");
        reader.setDaemon(true);  // don't keep JVM alive
        reader.start();

        while (true) {
            log(false, "stdOut");
            log(true, "stdErr");
            try {
                Thread.sleep(4567);
            }
            catch (InterruptedException ie) {
                log(true, "interrupted");
            }
        }
    }

    private static void log(boolean error, String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ").format(new Date());
        PrintStream stream = error ? System.err : System.out;
        String thread = "[" + Thread.currentThread().getName() + "] ";
        stream.println(time + thread + message);
    }

    private static class HookThread extends Thread {

        public HookThread(String name) {
            super(name);
        }

        public void run() {
            log(true, "hoo hoo - hook was here.");
        }
    }

    private static class InputReader implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String line = reader.readLine();
                    log(false, "input: " + line);
                    if (line.equals("EXIT")) {
                        System.exit(0); // shutdown hook will run.
                    }
                }
            }
            catch (Exception e) {
                log(true, "exception in reader. " + e);
            }
        }
    }

}
