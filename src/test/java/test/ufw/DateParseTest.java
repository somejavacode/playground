package test.ufw;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ufw.HttpDateTool;
import ufw.Log;
import ufw.Timer;

import java.text.SimpleDateFormat;
import java.util.*;


public class DateParseTest {

    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final String DATE_ZONE = "GMT";

    @Test
    public void testFormat() throws Exception {

        checkDateParse("Fri, 31 Dec 1999 23:59:59 GMT");
        checkDateParse("Mon, 27 Jul 2009 12:28:53 GMT");

        long start = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        for (int i = 0; i < 10000; i++) {
            long time = start + 3442531 * i;  // just a few time stamps
            String simple = sdf.format(new Date(time));
            String httpDate = HttpDateTool.format(time);
            // check formatter
            Assert.assertEquals(simple, httpDate);

            long tf = HttpDateTool.parse(simple);
            // check parser
            Assert.assertEquals(time / 1000, tf / 1000); // no milli seconds
        }
    }

    private void checkDateParse(String t1) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(DATE_ZONE));
        long d1 = sdf.parse(t1).getTime();
        long d2 = HttpDateTool.parse(t1);
        Assert.assertEquals(d1, d2);
    }

    @Test
    public void checkParser() throws Exception {
        checkParseFail("Tue, 27 Jul 2009 12:28:53 GMT");
        checkParseFail("Mox, 27 Jul 2009 12:28:53 GMT");
        checkParseFail("Mon, 27 Jxl 2009 12:28:53 GMT");
        checkParseFail("Mon, 27 Jul 2009 12:28:53 GWT");
        checkParseFail("Mon, 27 Jul 2009 12:2x:53 GMT");
        checkParseFail(" Mon, 27 Jul 2009 12:2x:53 GMT");
    }

    private void checkParseFail(String t1) throws Exception {
        try {
            HttpDateTool.parse(t1);
            Assert.fail();
        }
        catch (Exception e) {
            Log.info("expected: " + e);
        }
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkParse() throws Exception {

        int count = 100000;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(DATE_ZONE));

        String t1 = "Fri, 31 Dec 1999 23:59:59 GMT";
        Timer t = new Timer("SimpleDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            sdf.parse(t1);
        }
        t.stop(true); // 2900ms

        count *= 10;

        t = new Timer("HttpDateTool " + count + " times", true);
        for (int i = 0; i < count; i++) {
            HttpDateTool.parseSync(t1);
        }
        t.stop(true);  // 1150ms (own parseInt), 1250ms (Integer.parseInt)  speedup: 25+
    }

    @Test
    @Ignore  // do not run benchmark by default
    public void benchmarkFormat() throws Exception {

        int count = 1000000;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone(DATE_ZONE));

        long millis = System.currentTimeMillis();
        Date now = new Date(millis);
        Timer t = new Timer("SimpleDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            sdf.format(now);
        }
        t.stop(true); // 2900ms

        count *= 10;

        t = new Timer("HttpDateTool " + count + " times", true);
        for (int i = 0; i < count; i++) {
            HttpDateTool.formatSync(millis);
        }
        t.stop(true);  // 1150ms (own parseInt), 1250ms (Integer.parseInt)  speedup: 25+
    }

}
