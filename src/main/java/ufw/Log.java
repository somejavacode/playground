package ufw;

import java.io.PrintStream;
import java.util.Date;

public class Log {

    public enum Level {
        DEBUG(10, "DEBUG "),
        INFO( 20, "INFO  "),
        WARN( 30, "WARN  "),
        ERROR(40, "ERROR ");

        private int levelNr;
        private String levelMsg;

        Level(int number, String msg) {
            this.levelNr = number;
            this.levelMsg = msg;
        }

        public int getLevelNr() {
            return levelNr;
        }

        public String getLevelMsg() {
            return levelMsg;
        }
    }

    // default level debug
    private static int levelNr = Level.DEBUG.getLevelNr();

    private static PrintStream ps = System.out;


    public static void setLevel(Level level) {
        levelNr = level.getLevelNr();
    }

    public static void setPs(PrintStream ps) {
        Log.ps = ps;
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    // TODO: synchronized is "cheap solution" to avoid that stack trace gets "separated" from log line
    // without stack there is no need for synchronization ("ps.println()" is synchronized)
    public static synchronized void log(Level level, String message, Throwable t) {
        if (level.getLevelNr() < levelNr) {
            return;
        }
        String time = FixDateFormat.formatSync(new Date());
        String thread = "[" + Thread.currentThread().getName() + "] ";
        ps.println(time + thread + level.getLevelMsg() + message);
        if (t != null) {
            t.printStackTrace(ps);
        }
    }

}
