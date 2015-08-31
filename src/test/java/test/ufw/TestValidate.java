package test.ufw;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ufw.Log;
import ufw.Timer;
import ufw.Validate;

public class TestValidate {

    private static final String MESSAGE = "test message";
    private static final String MESSAGE1 = " testing";

    @Test
    public void testValidateNotNull() {

        // does not fail
        Validate.notNull("notNullString");
        Validate.notNull("notNullString", MESSAGE);
        Validate.notNull("notNullString", MESSAGE, MESSAGE1);

        try {
            Validate.notNull(null); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

        try {
            Validate.notNull(null, MESSAGE); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

        try {
            Validate.notNull(null, MESSAGE, MESSAGE1); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

    }

    @Test
    public void testValidateTrue() {

        Validate.isTrue(true);
        Validate.isTrue(true, MESSAGE); // does not fail
        Validate.isTrue(true, MESSAGE, MESSAGE1); // does not fail

        try {
            Validate.isTrue(false); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

        try {
            Validate.isTrue(false, MESSAGE); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

        try {
            Validate.isTrue(false, MESSAGE, MESSAGE1); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() throws Exception {
        // test effect of varargs (to avoid surplus argument concatenation) vs simple argument

        int repeats = 10000000;
        Timer t;

        t = new Timer("test Validate.isTrue(boolean)", false);
        for (int i = 1; i < repeats; i++) {
            Validate.isTrue(i != Integer.MAX_VALUE);
        }
        t.stop(true);

        t = new Timer("test Validate.isTrue(boolean, String)", false);
        for (int i = 1; i < repeats; i++) {
            Validate.isTrue(i != Integer.MAX_VALUE, "this message will be concatenated " + i);
        }
        t.stop(true); // approx 50 times slower!

        t = new Timer("test Validate.isTrue(boolean, Object...)", false);
        for (int i = 1; i < repeats; i++) {
            Validate.isTrue(i != Integer.MAX_VALUE, "this message will be concatenated ", i);
        }
        t.stop(true);


    }

}
