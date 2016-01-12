package ufw;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * this is a "fixed" HTTP date (RFC7231 7.1.1.1) parser and formatter.
 * <br/>
 * according date format pattern: "EEE, dd MMM yyyy HH:mm:ss z"
 * <br/>
 * e.g. "Tue, 27 Jul 2009 12:28:53 GMT"
 */
public class HttpDateTool {

    private static final String GMT_ID = "GMT";

    private static final TimeZone GMT = TimeZone.getTimeZone(GMT_ID);

    private static GregorianCalendar cal = new GregorianCalendar(GMT);

    // NOTE: the shared and synchronized calendar is faster in single threaded case.
    public static synchronized long parseSync(String date) {
        return parse(cal, date);
    }

    // slower if single threaded but possibly faster for multi core multi threaded case
    public static long parse(String date) {
        GregorianCalendar cal = new GregorianCalendar(GMT);
        return parse(cal, date);
    }

    private static long parse(Calendar cal, String date) {
        Validate.notNull(date);
        Validate.isTrue(date.length() == 29, " invalid date string length. date='", date, "'");
        cal.set(Calendar.YEAR, Integer.parseInt(date.substring(12, 16)));
        cal.set(Calendar.MONTH, lookupMonth(date.substring(8, 11)));
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(5, 7)));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(date.substring(17, 19)));
        cal.set(Calendar.MINUTE, Integer.parseInt(date.substring(20, 22)));
        cal.set(Calendar.SECOND, Integer.parseInt(date.substring(23, 25)));
        cal.set(Calendar.MILLISECOND, 0);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        int dayDate = lookupDay(date.substring(0, 3));
        Validate.isTrue(day == dayDate, "day does not match. Date='", date, "'");
        String tz = date.substring(26, 29);
        Validate.isTrue(GMT_ID.equals(tz), "Invalid time zone. value=", tz);
        return cal.getTimeInMillis();
    }

    public static synchronized void appendSync(StringBuilder sb, long millis) {
        cal.setTimeInMillis(millis);
        format(sb, cal);
    }

    private static long lastSecond = 0;
    private static String lastDate = null;


    public static synchronized String formatSyncCached(StringBuilder sb, long millis) {
        if (millis / 1000 == lastSecond) {
            return lastDate;
        }
        else {
            lastDate = formatSync(millis);
            lastSecond = millis / 1000;
            return lastDate;
        }
    }

    public static synchronized void append(StringBuilder sb, long millis) {
        GregorianCalendar cal = new GregorianCalendar(GMT);
        cal.setTimeInMillis(millis);
        format(sb, cal);
    }
    public static synchronized String formatSync(long millis) {
        StringBuilder sb = new StringBuilder(39);
        cal.setTimeInMillis(millis);
        format(sb, cal);
        return sb.toString();
    }

    public static synchronized String format(long millis) {
        StringBuilder sb = new StringBuilder(39);
        GregorianCalendar cal = new GregorianCalendar(GMT);
        cal.setTimeInMillis(millis);
        format(sb, cal);
        return sb.toString();
    }

    private static void format(StringBuilder sb, Calendar cal) {
        sb.append(DAYS[cal.get(Calendar.DAY_OF_WEEK)]);
        sb.append(", ");
        appendNumber(sb, cal.get(Calendar.DAY_OF_MONTH), 2);
        sb.append(' ');
        sb.append(MONTHS[cal.get(Calendar.MONTH)]);
        sb.append(' ');
        sb.append(cal.get(Calendar.YEAR)); // assume 4 digits for normal cases
        sb.append(' ');
        appendNumber(sb, cal.get(Calendar.HOUR_OF_DAY), 2);
        sb.append(':');
        appendNumber(sb, cal.get(Calendar.MINUTE), 2);
        sb.append(':');
        appendNumber(sb, cal.get(Calendar.SECOND), 2);
        sb.append(" GMT");
    }

    private static final String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                             "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    private static int lookupMonth(String month) {
        for (int i = 0; i < MONTHS.length; i++) {
            if (month.equals(MONTHS[i])) {
                return i;
            }
        }
        throw new RuntimeException("invalid month string: '" + month + "'");
    }

    // NOTE: Sunday starts with 1.
    private static final String[] DAYS = { "", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    private static int lookupDay(String day) {
        for (int i = 1; i < DAYS.length; i++) {
            if (day.equals(DAYS[i])) {
                return i;
            }
        }
        throw new RuntimeException("invalid day string: '" + day + "'");
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
