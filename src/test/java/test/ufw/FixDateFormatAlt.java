package test.ufw;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;

/**
 * test FixDatFormat with java.time (java8)
 */
public class FixDateFormatAlt {

    private static ZoneId zid = ZoneId.systemDefault();

    public static void setTimeZoneUTC() {
        zid = ZoneId.of("UTC");
    }

    public static String format(long millis) {
        StringBuilder sb = new StringBuilder(24);
        format(sb, millis);
        return sb.toString();
    }

    public static void format(StringBuilder sb, long millis) {
        LocalDateTime ltd = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zid);
        format(sb, ltd);
    }

    private static void format(StringBuilder sb, LocalDateTime cal) {
        sb.append(cal.get(ChronoField.YEAR)); // assume 4 digits for normal cases
        sb.append('-');
        appendNumber(sb, cal.get(ChronoField.MONTH_OF_YEAR), 2);
        sb.append('-');
        appendNumber(sb, cal.get(ChronoField.DAY_OF_MONTH), 2);
        sb.append(' ');
        appendNumber(sb, cal.get(ChronoField.HOUR_OF_DAY), 2);
        sb.append(':');
        appendNumber(sb, cal.get(ChronoField.MINUTE_OF_HOUR), 2);
        sb.append(':');
        appendNumber(sb, cal.get(ChronoField.SECOND_OF_MINUTE), 2);
        sb.append('.');
        appendNumber(sb, cal.get(ChronoField.MILLI_OF_SECOND), 3);
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
