package test.ufw;


import org.junit.Assert;
import org.junit.Test;
import ufw.Log;
import ufw.Validate;

public class TestValidate {

    private static final String MESSAGE = "test message";

    @Test
    public void testValidateNotNull() {

        Validate.notNull("notNullString", MESSAGE); // does not fail
        Validate.notNull("notNullString");

        try {
            Validate.notNull(null, MESSAGE); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }

        try {
            Validate.notNull(null); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }
    }

    @Test
    public void testValidateTrue() {

        Validate.isTrue(true, MESSAGE); // does not fail
        Validate.isTrue(true);

        try {
            Validate.isTrue(false, MESSAGE); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }
        try {
            Validate.isTrue(false); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected exception. " + e);
        }
    }

}
