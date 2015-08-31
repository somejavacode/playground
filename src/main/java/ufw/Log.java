package ufw;

import java.io.PrintStream;

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

    private static String newLine = "\n"; // LF = 0x0A .. unix line break

    public static void setNewLine(String newLine) {
        Log.newLine = newLine;
    }

    public static void setLevel(Level level) {
        levelNr = level.getLevelNr();
    }

    public static void setPs(PrintStream ps) {
        Log.ps = ps;
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

    public static void debug(Object... messages) {
        log(Level.DEBUG, null, null, messages);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void info(Object... messages) {
        log(Level.INFO, null, null, messages);
    }

    public static void warn(String message) {
        log(Level.WARN, message);
    }

    public static void warn(Object... messages) {
        log(Level.WARN, null, null, messages);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(Object... messages) {
        log(Level.ERROR, null, null, messages);
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }

    public static void log(Level level, String message) {
        log(level, message, null);
    }

    // TODO: synchronized is "cheap solution" to avoid that stack trace gets "separated" from log line
    // without stack there is no need for synchronization ("ps.println()" is synchronized)
    private static synchronized void log(Level level, String message, Throwable t, Object... messages) {
        if (level.getLevelNr() < levelNr) {
            return;
        }
        String time = FixDateFormat.formatSync(System.currentTimeMillis());
        String thread = "[" + Thread.currentThread().getName() + "] ";
        String finalMessage = message != null ? message : getMessage(messages);
        ps.print(time + thread + level.getLevelMsg() + finalMessage);
        ps.print(newLine);
        if (t != null) {
            t.printStackTrace(ps);
        }
    }

    // todo: cut and waste Validate
    private static String getMessage(Object... messages) {
        StringBuilder sb = new StringBuilder();
        for (Object message : messages) {
            sb.append(message);
        }
        return sb.toString();
    }
}
