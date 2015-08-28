package test.ufw;

import org.junit.Ignore;
import org.junit.Test;
import ufw.Log;
import ufw.Timer;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LogTest {

    @Test
    public void testLevels() {
        Log.info("info text");
        Log.debug("info text");
        Log.warn("warn text");
        Log.error("error text");
        Log.error("exception text", new RuntimeException("oops"));
    }

    @Test
    public void testUmlauts() throws Exception {
        String message = "Hell\u00f6 \u00dcmlauts.";
        Log.info(message);  // this fails in command shell with default encoding. see ConsoleEncodingTest

        // enforce UTF-8 when writing to file
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_uml.log"), true, "UTF-8"));
        Log.info(message);
        Log.setPs(System.out); // need to reset this static stuff
    }
    @Test
    public void testStdErr() {
        Log.setPs(System.err);
        Log.error("using stderr");
        Log.setPs(System.out);  // need to reset this static stuff
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() throws Exception {
        int repeats = 1000;

        Timer t = new Timer("log " + repeats + " times", true);
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
        Log.setLevel(Log.Level.DEBUG);
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkFile() throws Exception {

        int repeats = 100000;

        Timer t = new Timer("log " + repeats + " times (write to file)", false);
        Log.setLevel(Log.Level.DEBUG);
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test.log")));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        Log.setPs(System.out);
        t.stop(true);  // 800ms / 100000 lines (8us/line)

        t = new Timer("log " + repeats + " times (write to file, buffered)", false);
        Log.setLevel(Log.Level.DEBUG);
        int bufferSize = 16384;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/tmp/testb.log"), bufferSize);
        Log.setPs(new PrintStream(bos));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr " + i);
        }
        // must flush buffer, loosing some lines otherwise....
        bos.close();

        // TODO: check if buffered file leads to delays.
        // TODO: if there are delays add thread that flushes after 1s if too less logs were written in last second

        Log.setPs(System.out);
        t.stop(true); // 120ms / 100000 lines (1.2us/line)
    }

}
