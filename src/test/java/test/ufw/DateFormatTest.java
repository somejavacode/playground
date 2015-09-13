package test.ufw;

import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ufw.FixDateFormat;
import ufw.Timer;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateFormatTest {

    private static final String FMT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";

    @Test
    public void testFormat() {

        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING);
        FastDateFormat fdf = FastDateFormat.getInstance(FMT_STRING);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(FMT_STRING);

        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            long time = start + 342531 * i;  // just a few time samples
            String simple = sdf.format(new Date(time));

            String fix = FixDateFormat.format(time);
            // compare result with FixDateFormat
            Assert.assertEquals(simple, fix);

            String fast = fdf.format(time);
            Assert.assertEquals(simple, fast);

            String date8 = dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
            Assert.assertEquals(simple, date8);
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(FMT_STRING);
        t = new Timer("DateTimeFormatter " + count + " times", true);
        ZoneId zid = ZoneId.systemDefault();
        for (int i = 0; i < count; i++) {
            dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(start + i), zid));
        }
        t.stop(true);

        // this is 2-3 times faster than SimpleDateFormat
        FastDateFormat fdf = FastDateFormat.getInstance(FMT_STRING);
        t = new Timer("FastDateFormat " + count + " times", true);
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
