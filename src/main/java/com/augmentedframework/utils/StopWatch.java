package com.augmentedframework.utils;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

/**
 * Stopwatch class captures the elapsed/start time of the stop watch
 *
 * @author YSabato
 * @version 1.0
 * @since 01/01/2020
 */
public class StopWatch {

    private static Stopwatch sw = Stopwatch.createStarted();

    /**
     * Returns the start time of the stop watch
     *
     * @return time in seconds
     */
    public static long startTime() {
        return sw.elapsed(TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the elapsed time (secs) of the stop watch.
     *
     * @param startTime
     *
     * @return elapsed time in seconds
     */
    public static long elapsedTime(long startTime) {
        return (long) (sw.elapsed(TimeUnit.MILLISECONDS) - startTime) / 1000;
    }

    /**
     * Returns the elapsed time (ms) of the stop watch.
     *
     * @param startTime
     *
     * @return elapsed time in milli seconds
     */
    public static long elapsedTimeInMilliSecs(long startTime) {
        return (long) (sw.elapsed(TimeUnit.MILLISECONDS) - startTime);
    }
}
