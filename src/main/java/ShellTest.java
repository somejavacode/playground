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

}
