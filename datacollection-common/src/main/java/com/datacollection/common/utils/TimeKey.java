package com.datacollection.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Generate time-key use in Collect and SyncProfile module
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class TimeKey {

    private static final String PATTERN = "yyyy-MM-dd-HH";

    public static String fromDate(Date date) {
        DateFormat df = new SimpleDateFormat(PATTERN);
        return df.format(date);
    }

    public static String currentTimeKey() {
        return fromDate(new Date());
    }

    public static String nextTimeKey(String currentTimeKey) {
        try {
            DateFormat df = new SimpleDateFormat(PATTERN);
            Date current = df.parse(currentTimeKey);
            Date nextHour = DateTimes.add(Calendar.HOUR, 1, current);

            return df.format(nextHour);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean satisfyTime(String currentTimeKey) {
        try {
            DateFormat df = new SimpleDateFormat(PATTERN);
            Date current = df.parse(currentTimeKey);
            Date now = df.parse(df.format(new Date()));

            return current.before(now);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println(nextTimeKey("2017-11-01-00"));
    }
}
