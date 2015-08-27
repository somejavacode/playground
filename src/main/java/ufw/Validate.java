package ufw;

public class Validate {

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed. " + message);
        }
    }

    public static void isNotNull(Object object, String message) {
        if (object == null) {
            throw new RuntimeException("Validate.isNotNull failed. " + message);
        }
    }

}
