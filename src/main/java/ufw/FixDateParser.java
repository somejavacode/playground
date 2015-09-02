package ufw;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * this is a "fixed" HTTP date parser. pattern: "EEE, dd MMM yyyy HH:mm:ss z"
 * <br/>
 * e.g. "Tue, 27 Jul 2009 12:28:53 GMT"
 */
public class FixDateParser {

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

    private static int lookupMonth(String month) {
        if (month.equals("Jan")) return Calendar.JANUARY;
        if (month.equals("Feb")) return Calendar.FEBRUARY;
        if (month.equals("Mar")) return Calendar.MARCH;
        if (month.equals("Apr")) return Calendar.APRIL;
        if (month.equals("May")) return Calendar.MAY;
        if (month.equals("Jun")) return Calendar.JUNE;
        if (month.equals("Jul")) return Calendar.JULY;
        if (month.equals("Aug")) return Calendar.AUGUST;
        if (month.equals("Sep")) return Calendar.SEPTEMBER;
        if (month.equals("Oct")) return Calendar.OCTOBER;
        if (month.equals("Nov")) return Calendar.NOVEMBER;
        if (month.equals("Dec")) return Calendar.DECEMBER;
        throw new RuntimeException("invalid month string: '" + month + "'");
    }

    private static int lookupDay(String day) {
        if (day.equals("Sun")) return Calendar.SUNDAY;
        if (day.equals("Mon")) return Calendar.MONDAY;
        if (day.equals("Tue")) return Calendar.TUESDAY;
        if (day.equals("Wed")) return Calendar.WEDNESDAY;
        if (day.equals("Thu")) return Calendar.THURSDAY;
        if (day.equals("Fri")) return Calendar.FRIDAY;
        if (day.equals("Sat")) return Calendar.SATURDAY;
        throw new RuntimeException("invalid day string: '" + day + "'");
    }

}
