import ufw.Log;
import ufw.Timer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogTest {

    public static void main(String[] args) throws Exception {
        Log.info("info text");
        Log.debug("info text");
        Log.warn("warn text");
        Log.error("error text");
        Log.error("exception text", new RuntimeException("oops"));

        int repeats = 100000;

        Timer t;

        t = new Timer("log " + repeats + " times", true);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        t.stop(true);

        t = new Timer("log " + repeats + " times (below level)", false);
        Log.setLevel(Log.Level.INFO);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        t.stop(true);

        Log.setPs(System.err);
        Log.error("using stderr");

        t = new Timer("log " + repeats + " times (write to file)", false);
        Log.setLevel(Log.Level.DEBUG);
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test.log")));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        Log.setPs(System.out);
        t.stop(true);  // 400ms / 10000 lines

        t = new Timer("log " + repeats + " times (write to file, buffered)", false);
        Log.setLevel(Log.Level.DEBUG);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/tmp/testb.log"));
        Log.setPs(new PrintStream(bos));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        // must flush buffer, loosing some lines otherwise....
        bos.close();

        Log.setPs(System.out);
        t.stop(true); // 160ms / 10000 lines (16us/line)

    }

}
