import ufw.Log;

/**
 * static main version of LogTestMain.<br/>
 * Used to test encoding in console as this once was different from unit testing with maven via surefire and junit.<br/>
 * see also: LogTest
 */
public class LogTestMain {
    public static void main(String[] args) {
        String message = "Hell\u00f6 \u00dcmlauts.";
        Log.info(message);
    }
}
