package ufw;

import java.io.PrintStream;

public class Log {

    public enum Level {
        DEBUG(10, "DEBUG "),
        INFO(20,  "INFO  "),
        WARN(30,  "WARN  "),
        ERROR(40, "ERROR ");

        private int levelNr;
        private String levelMsg;

        Level(int number, String msg) {
            this.levelNr = number;
            this.levelMsg = msg;
        }
    }

    // default level debug
    private static int levelNr = Level.DEBUG.levelNr;

    private static PrintStream ps = System.out;

    private static String newLine = "\n"; // LF = 0x0A .. unix line break

    public static void setNewLine(String newLine) {
        Log.newLine = newLine;
    }

    public static void setLevel(Level level) {
        levelNr = level.levelNr;
    }

    public static void setPs(PrintStream ps) {
        Log.ps = ps;
    }

    public static void debug(String message) {
        log(Level.DEBUG, message);
    }

//    this is hardly faster than the varargs version
//    public static void debug(Object message, Object message1) {
//        log(Level.DEBUG, message.toString() + message1.toString());
//    }

    public static void debug(Object... messages) {
        log(Level.DEBUG, null, null, messages);
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void info(String message, String message1) {
        log(Level.INFO, message + message1);
    }

    public static void info(String message, String message1, String message2) {
        log(Level.INFO, message + message1 + message2);
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
        if (level.levelNr < levelNr) {
            return;
        }
        StringBuilder sb = new StringBuilder(100);  // estimated log line length as default
        FixDateFormat.formatSync(sb, System.currentTimeMillis());
        sb.append(" [").append(Thread.currentThread().getName()).append("] ");
        sb.append(level.levelMsg);
        if (message != null) {
            sb.append(message);
        }
        else {
            getMessage(sb, messages);
        }
        ps.print(sb.toString());
        ps.print(newLine);
        if (t != null) {
            t.printStackTrace(ps);
        }
    }

    // todo: almost cut and waste Validate
    private static void getMessage(StringBuilder sb, Object... messages) {
        for (Object message : messages) {
            sb.append(message);
        }
    }
}
