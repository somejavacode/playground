import ufw.Log;

/**
 * static main version of LogTestMain.
 * <p>
 * Used to test encoding in console as this once was different from unit testing with maven via surefire and junit.
 * <p>
 * see also: LogTest
 */
public class LogTestMain {
    public static void main(String[] args) {
        String message = "Hell\u00f6 \u00dcmlauts.";
        Log.info(message);
    }
}
