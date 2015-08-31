package ufw;

public class Validate {

    public static void isTrue(boolean condition, Object... messages) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed: " + getMessage(messages));
        }
    }

    public static void isTrue(boolean condition) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed.");
        }
    }

    public static void notNull(Object object, Object... message) {
        if (object == null) {
            throw new RuntimeException("Validate.notNull failed: " + getMessage(message));
        }
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw new RuntimeException("Validate.notNull failed.");
        }
    }

    private static String getMessage(Object... messages) {
        StringBuilder sb = new StringBuilder();
        for (Object message : messages) {
            sb.append(message);
        }
        return sb.toString();
    }

}
