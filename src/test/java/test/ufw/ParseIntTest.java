package test.ufw;

import org.junit.Ignore;
import org.junit.Test;
import ufw.Log;
import ufw.Timer;
import ufw.Validate;

public class ParseIntTest {

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() throws Exception {
        int count = 10000000;

        Timer t;

        t = new Timer("parseInt " + count + " times", true);
        for (int i = 0; i < count; i++) {
            int val = parseInt("1999");
            // Validate.isTrue(1999 == val);
        }
        t.stop(true);  // no logging output?
        t = new Timer("Integer.parseInt " + count + " times", true);
        for (int i = 0; i < count; i++) {
            int val = Integer.parseInt("1999");
            // Validate.isTrue(1999 == val);
        }
        t.stop(true);

        Log.info("done");

        Thread.sleep(200); // fixes logging output. TODO: WTF?
    }

    private static int parseInt(String string) {
        int val = 0;
        int mul = 1;
        for (int pos = string.length() - 1; pos >= 0; pos--) {
            int charNr = (int) string.charAt(pos); // total 105ms
            // Validate.isTrue(charNr >= 48 && charNr <= 57, "invalid int: " + string); // increases to total 4200ms!!!
            // Validate.isTrue(charNr >= 48 && charNr <= 57); // increases to total 300ms
            if (charNr < 48 || charNr > 57) {     // increases to total 140ms
                throw new RuntimeException("invalid number: " + string);
            }
            val += (charNr - 48) * mul;
            mul *= 10;
        }
        return val;
    }

}