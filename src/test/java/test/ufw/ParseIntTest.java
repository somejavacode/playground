package test.ufw;

import org.junit.Ignore;
import org.junit.Test;
import ufw.Timer;
import ufw.Validate;

public class ParseIntTest {

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() throws Exception {
        int count = 50000000;

        Timer t;

        t = new Timer("parseInt " + count + " times", true);
        for (int i = 0; i < count; i++) {
            int val = parseInt(Integer.toString(i));
            Validate.isTrue(i == val);
        }
        t.stop(true);  // no logging output?
        t = new Timer("Integer.parseInt " + count + " times", true);


        for (int i = 0; i < count; i++) {
//            int val = Integer.parseInt("1999");  // NOTE: this was too trivial, assume "aggressive loop optimization"
//            Validate.isTrue(1999 == val);
            int val = Integer.parseInt(Integer.toString(i));
            Validate.isTrue(i == val);
        }
        t.stop(true);

        //Thread.sleep(200); // fixes logging output. WFT? happens only with  "aggressive loop optimization"?
    }

    // this is a _failed attempt_ to create a fast alternative for  Integer.parseInt ....
    private static int parseInt(String string) {
        int val = 0;
        int mul = 1;
        for (int pos = string.length() - 1; pos >= 0; pos--) {
            int charNr = (int) string.charAt(pos); // total: 2600-2800ms (50000000)
            // Validate.isTrue(charNr >= 48 && charNr <= 57, "invalid int: " + string); // increases to total 9100ms
            // Validate.isTrue(charNr >= 48 && charNr <= 57, "invalid int"); // increases to total 2800-3000ms
            // if (charNr < 48 || charNr > 57) {     // increases to total 140ms
            //     throw new RuntimeException("invalid number: " + string);
            // }
            val += (charNr - 48) * mul;
            mul *= 10;
        }
        return val;
    }

}