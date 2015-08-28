package ufw;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * this is a "fixed" date formatter. pattern: "yyyy-MM-dd HH:mm:ss.SSS " <br/>
 * it is 2-3 times faster (sync case) than org.apache.commons.lang3.time.FastDateFormat
 * FastDateFormat is 2-3 times faster than java.text.SimpleDateFormat
 * (total speedup 6-7)
 */
public class FixDateFormat {

    // NOTE: this is implicit local time zone.
    private static GregorianCalendar cal = new GregorianCalendar();

    // null means local time zone
    private static TimeZone timeZone = null;

    public static void setTimeZone(TimeZone timeZone) {
        FixDateFormat.timeZone = timeZone;
        FixDateFormat.cal.setTimeZone(timeZone);
    }

    public static void setTimeZoneUTC() {
        setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // NOTE: the shared and synchronized calendar is faster in single threaded case.
    public static synchronized String formatSync(long millis) {
        cal.setTimeInMillis(millis);
        return format(cal);
    }

    // slower if single threaded but possibly faster for multi core multi threaded case
    public static String format(long millis) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);
        if (timeZone != null) {
            cal.setTimeZone(timeZone);
        }
        return format(cal);
    }

    private static String format(Calendar cal) {
        StringBuilder sb = new StringBuilder(24);
        sb.append(cal.get(Calendar.YEAR)); // assume 4 digits for normal cases
        sb.append('-');
        appendNumber(sb, cal.get(Calendar.MONTH) + 1, 2);
        sb.append('-');
        appendNumber(sb, cal.get(Calendar.DAY_OF_MONTH), 2);
        sb.append(' ');
        appendNumber(sb, cal.get(Calendar.HOUR_OF_DAY), 2);
        sb.append(':');
        appendNumber(sb, cal.get(Calendar.MINUTE), 2);
        sb.append(':');
        appendNumber(sb, cal.get(Calendar.SECOND), 2);
        sb.append('.');
        appendNumber(sb, cal.get(Calendar.MILLISECOND), 3);
        sb.append(' ');
        return sb.toString();
    }

    // not that flexible.. only digits=2,3 will work
    private static void appendNumber(StringBuilder sb, int value, int digits) {
        if (digits > 1 && value < 10) {
            sb.append('0');
        }
        if (digits > 2 && value < 100) {
            sb.append('0');
        }
        sb.append(value);
     }
}
