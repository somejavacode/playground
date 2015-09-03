package ufw;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Timer {

    private String action;
    private long start;
    private long lastSplit;
    private long duration;


    /**
     * start timer with defined action
     *
     * @param action action used for logging
     * @param log if true log start
     */
    public Timer(String action, boolean log) {
        this.action = action;
        this.start = System.nanoTime();
        this.lastSplit = start;
        if (log) {
            Log.info("start " + action);
        }
    }

    /**
     * log intermediate time (show total time and time since last split)
     *
     * @param splitAction split action used for logging
     */
    public void split(String splitAction) {
        long splitTime = System.nanoTime();
        long splitDelta = splitTime - lastSplit;
        long current = splitTime - start;
        Log.info("split " + action + "/" + splitAction +
                " t=" + getTimeString(current, true) +
                " s=" + getTimeString(splitDelta, true));
        lastSplit = splitTime;
    }

    /**
     * stop timer
     *
     * @param log if true log end with duration and action
     */
    public void stop(boolean log) {
        Validate.isTrue(duration == 0, "timer already stopped.");
        duration = System.nanoTime() - start;
        if (log) {
            Log.info("done " + action + " took=" + getTimeString(duration, true));
        }
    }

    /**
     * @return duration in nanoseconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * get string representation of time
     *
     * @param fractions show fractions of milliseconds (microseconds)
     * @return time String
     */
    public String getTimeString(long time, boolean fractions) {
        if (fractions) {
            // use formatter, could speed up with
            // show millis with 3 digits (i.e. microseconds)
            DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            return df.format(time / 1000000.0) + "ms";
        }
        else {
            return duration / 1000000 + "ms";
        }
    }
}
