package test.ufw;

import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ufw.FixDateFormat;
import ufw.Timer;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateFormatTest {

    private static final String FMT_STRING = "yyyy-MM-dd HH:mm:ss.SSS ";

    @Test
    public void testFormat() {

        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            long time = start + 342531 * i;  // just a few time stamps
            String simple = sdf.format(new Date(time));
            String fix = FixDateFormat.format(time);
            // check result of FixDateFormat
            Assert.assertEquals(simple, fix);
        }
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() {

        long start = System.currentTimeMillis();
        int count = 1000000;

        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING);
        Timer t = new Timer("SimpleDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            sdf.format(new Date(start + i));
        }
        t.stop(true);

        // this is 2-3 times faster than SimpleDateFormat
        FastDateFormat fdf = FastDateFormat.getInstance(FMT_STRING);
        t = new Timer("FastDateFormmat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            fdf.format(new Date(start + i));
        }
        t.stop(true);

        // this is 2-3 times faster than FastDateFormat
        t = new Timer("FixDateFormat (sync) " + count + " times", true);
        for (int i = 0; i < count; i++) {
            FixDateFormat.formatSync(start + i);
        }
        t.stop(true);

        t = new Timer("FixDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            FixDateFormat.format(start + i);
        }
        t.stop(true);

    }

}
