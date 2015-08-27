import ufw.FixDateFormat;
import ufw.Log;
import ufw.Timer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * test speed of date formatSync variants
 */
public class DateFormatBench {

    public static void main(String[] args) {

        String fmtString = "yyyy-MM-dd HH:mm:ss.SSS ";

        SimpleDateFormat sdf = new SimpleDateFormat(fmtString);
        long start = System.currentTimeMillis();
        int count = 1000000;
        Timer t = new Timer("SimpleDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            String date = sdf.format(new Date(start + i));
            if (i == 0) {
                Log.info(date);
            }
        }
        t.stop(true);


        // this is 2-3 times faster than SimpleDateFormat
/*
        // removed to get rid of commons dependency ...
        FastDateFormat fdf = FastDateFormat.getInstance(fmtString);
        t = new Timer("FastDateFormmat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            String date = fdf.formatSync(new Date(start + i));
            if (i == 0) {
                Log.info(date);
            }
        }
        t.stop(true);
*/

        // this is 2-3 times faster than FastDateFormat

        t = new Timer("FixDateFormat (sync) " + count + " times", true);
        for (int i = 0; i < count; i++) {
            String date = FixDateFormat.formatSync(new Date(start + i));
            if (i == 0) {
                Log.info(date);
            }
        }
        t.stop(true);

        t = new Timer("FixDateFormat " + count + " times", true);
        for (int i = 0; i < count; i++) {
            String date = FixDateFormat.format(new Date(start + i));
            if (i == 0) {
                Log.info(date);
            }
        }
        t.stop(true);

    }


}
