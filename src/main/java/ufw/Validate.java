package ufw;

public class Validate {

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed" + getMessage(message));
        }
    }

    public static void isTrue(boolean condition) {
        isTrue(condition, null);
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new RuntimeException("Validate.notNull failed" + getMessage(message));
        }
    }

    public static void notNull(Object object) {
        notNull(object, null);
    }

    private static String getMessage(String message) {
        return message == null ? "." : ": " + message;
    }

}
