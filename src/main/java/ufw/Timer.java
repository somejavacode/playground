package ufw;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Timer {

    private String action;
    private long start;
    private long lastSplit;
    private String splitAction;
    private long duration;


    /**
     * start timer with defined action
     *
     * @param action action used for logging
     * @param log    if true log start
     */
    public Timer(String action, boolean log) {
        this.action = action;
        this.start = System.nanoTime();
        this.lastSplit = start;
        this.splitAction = null;
        if (log) {
            Log.info("start " + action);
        }
    }

    /**
     * start timer with defined action and split action
     *
     * @param action action used for logging
     * @param log    if true log start
     */
    public Timer(String action, String splitAction, boolean log) {
        this.action = action;
        this.splitAction = splitAction;
        this.start = System.nanoTime();
        this.lastSplit = start;
        if (log) {
            Log.info("start " + action + "/" + splitAction);
        }
    }

    /**
     * log intermediate time (show total time and time since last split)
     *
     * @param newSplitAction next split action used for logging
     * @param message        message about last action (what was done)
     */
    public void split(String newSplitAction, String message, boolean log) {
        long splitTime = System.nanoTime();
        long splitDelta = splitTime - lastSplit;
        long current = splitTime - start;
        if (log) {
            String messageStr = message == null ? "" : "(" + message + ")";
            Log.info("split " + action + "/" + newSplitAction + " " +
                    splitAction + messageStr + "=" + getTimeString(splitDelta, true) +
                    " total=" + getTimeString(current, true));
        }
        lastSplit = splitTime;
        splitAction = newSplitAction;
    }

    /**
     * log intermediate time (show total time and time since last split)
     *
     * @param newSplitAction next split action used for logging
     */
    public void split(String newSplitAction, boolean log) {
        split(newSplitAction, null, log);
    }

    /**
     * stop timer
     *
     * @param message message about what was completed
     * @param log if true log end with duration and action
     */
    public void stop(String message, boolean log) {
        Validate.isTrue(duration == 0, "timer already stopped.");
        long end = System.nanoTime();
        String messageStr = message == null ? "" : "(" + message + ")";
        duration = end - start;
        if (log) {
            if (splitAction == null) {
                Log.info("done " + action + messageStr + " took=" + getTimeString(duration, true));
            }
            else {
                long splitDelta = end - lastSplit;
                Log.info("done " + action + "/" + splitAction + " "
                        + splitAction + messageStr + "=" + getTimeString(splitDelta, true) +
                        " total=" + getTimeString(duration, true));
            }
        }
    }

    /**
     * stop timer
     *
     * @param log if true log end with duration and action
     */
    public void stop(boolean log) {
        stop(null, log);
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
