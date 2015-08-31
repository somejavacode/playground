package test.ufw;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ufw.FixDateParser;
import ufw.Log;
import ufw.Timer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class DateParseTest {

    private static final String FMT_STRING = "EEE, dd MMM yyyy HH:mm:ss z";

    @Test
    public void testFormat() throws Exception {

        checkDate("Fri, 31 Dec 1999 23:59:59 GMT");
        checkDate("Mon, 27 Jul 2009 12:28:53 GMT");

        long start = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        for (int i = 0; i < 1000; i++) {
            long time = start + 3442531 * i;  // just a few time stamps
            String simple = sdf.format(new Date(time));
            long tf = FixDateParser.parse(simple);
            // check result of FixDateParser
            Assert.assertEquals(time / 1000, tf / 1000); // no milli seconds
        }
    }

    private void checkDate(String t1) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long d1 = sdf.parse(t1).getTime();
        long d2 = FixDateParser.parse(t1);
        Assert.assertEquals(d1, d2);
    }

    @Test
    public void checkParser() throws Exception {
        checkParseFail("Tue, 27 Jul 2009 12:28:53 GMT");
        checkParseFail("Mox, 27 Jul 2009 12:28:53 GMT");
        checkParseFail("Mon, 27 Jxl 2009 12:28:53 GMT");
        checkParseFail("Mon, 27 Jul 2009 12:28:53 GWT");
        checkParseFail("Mon, 27 Jul 2009 12:2x:53 GMT");
    }

    private void checkParseFail(String t1) throws Exception {
        try {
            FixDateParser.parse(t1);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected: " + e);
        }
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmark() throws Exception {

        int count = 100000;
        SimpleDateFormat sdf = new SimpleDateFormat(FMT_STRING, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String t1 = "Fri, 31 Dec 1999 23:59:59 GMT";
        Timer t = new Timer("SimpleDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            sdf.parse(t1);
        }
        t.stop(true); // 2900ms

        count *= 10;

        t = new Timer("FixDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            FixDateParser.parseSync(t1);
        }
        t.stop(true);  // 1150ms (own parseInt), 1250ms (Integer.parseInt)  speedup: 25+
    }
}
