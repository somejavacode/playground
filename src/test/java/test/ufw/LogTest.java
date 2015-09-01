package test.ufw;

import org.junit.Ignore;
import org.junit.Test;
import ufw.FixDateFormat;
import ufw.Log;
import ufw.Timer;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TimeZone;

public class LogTest {

    @Test
    public void testLevels() {
        Log.debug("debug text");
        Log.info("info text");
        Log.warn("warn text");
        Log.error("error text");
        Log.error("exception text", new RuntimeException("oops"));
    }

    @Test
    public void testTimeZone() {
        Log.debug("local time log");
        FixDateFormat.setTimeZoneUTC();  // NOTE: this is ugly..
        Log.info("UTC time log");
        FixDateFormat.setTimeZone(TimeZone.getDefault());
        Log.debug("back to local time");
    }

    @Test
    public void testArgs() {
        Log.info("info", " text=", 3);
        long x =33;
        Log.info("info", " value=", x, "ms");
    }

    @Test
    public void testUmlauts() throws Exception {
        String message = "Hell\u00f6 \u00dcmlauts.";
        Log.info(message);  // this fails in windoze cmd.exe shell with default encoding. see ConsoleEncodingTest

        // enforce UTF-8 when writing to file
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_uml.log"), true, "UTF-8"));
        Log.info(message);
        Log.setPs(System.out); // need to reset this static stuff
    }
    @Test
    public void testStdErr() {
        Log.setPs(System.err);
        Log.error("using stderr");
        Log.setPs(System.out);
        Log.error("using stdout");
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkArgs() throws IOException {
        int repeats = 100000;
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_args.log")));

        Timer t = new Timer("log " + repeats + " varargs", false);
        for (int i = 0; i < repeats; i++) {
            // Log.debug("logging line: ", i, " two times ", 2 * i);  // 850ms
        }
        Log.setPs(System.out);
        t.stop(true);

        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_concat.log")));
        t = new Timer("log " + repeats + " times concat", false);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line: " +  i + " two times " + 2 * i);  // 520ms
        }
        Log.setPs(System.out);
        t.stop(true);
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkArgsShort() throws IOException {
        int repeats = 1000000;
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_args1.log")));

        Timer t = new Timer("log " + repeats + " varargs", false);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line: ", i);
        }
        Log.setPs(System.out);
        t.stop(true);

        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_concat1.log")));
        t = new Timer("log " + repeats + " times concat", false);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line: " +  i);
        }
        Log.setPs(System.out);
        t.stop(true);
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkArgsLong() throws IOException {
        int repeats = 100000;
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_args2.log")));

        Timer t = new Timer("log " + repeats + " varargs", false);
        for (int i = 0; i < repeats; i++) {
            // Log.debug("logging line: ", i, " two times ", 2 * i, " three times ", 3 * i, " four times ", 4 * i);
        }
        Log.setPs(System.out);
        t.stop(true);

        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test_concat2.log")));
        t = new Timer("log " + repeats + " times concat", false);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line: " +  i + " two times " + 2 * i + " three times " + 3 * i + " four times" + 4 * i);
        }
        Log.setPs(System.out);
        t.stop(true);
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkConsole() throws Exception {
        int repeats = 1000;

        Timer t = new Timer("log " + repeats + " times", true);
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr ", i);
        }
        t.stop(true);
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkBelow() throws Exception {
        int repeats = 100000;
        Timer t = new Timer("log " + repeats + " times (below level)", false);
        Log.setLevel(Log.Level.INFO);
        for (int i = 0; i < repeats; i++) {
//             Log.debug("logging line nr " + i); // 40ms
            Log.debug("logging line nr ", i);  // 18ms
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
        Log.setPs(new PrintStream(new FileOutputStream("/tmp/test" + repeats + ".log")));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr ", i);
        }
        Log.setPs(System.out);
        t.stop(true);  // 800ms / 100000 lines (8us/line)

        t = new Timer("log " + repeats + " times (write to file, buffered)", false);
        Log.setLevel(Log.Level.DEBUG);
        int bufferSize = 16384;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/tmp/test" + repeats + "_buff.log"), bufferSize);
        Log.setPs(new PrintStream(bos));
        for (int i = 0; i < repeats; i++) {
            Log.debug("logging line nr ", i);
        }
        // must flush buffer, loosing some lines otherwise....
        bos.close();

        // TODO: check if buffered file leads to delays.
        // TODO: if there are delays add thread that flushes after 1s if too less logs were written in last second

        Log.setPs(System.out);
        t.stop(true); // 120ms / 100000 lines (1.2us/line)
    }

}
