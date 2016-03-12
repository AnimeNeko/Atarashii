package net.somethingdreadful.MAL;

import android.text.format.DateUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTools {

    /**
     * Parse a date with an ISO8601 string.
     *
     * @param ISO8601  The ISO8601 String
     * @param withTime Use true when you want also the time (hours & minutes)
     * @return String The readable string.
     */
    public static String parseDate(String ISO8601, boolean withTime) {
        String result;
        if (ISO8601 == null)
            return "?";
        else
            result = getDateString(parseISO8601(ISO8601), withTime);

        return result == null ? ISO8601 : result;
    }

    /**
     * Parse a date with miliseconds.
     *
     * @param time The time in miliseconds
     * @return String The readable string.
     */
    public static String parseDate(Long time) {
        Calendar calander = Calendar.getInstance();
        calander.setTimeInMillis(time);
        return getDateString(calander.getTime(), true);
    }

    private static Date parseISO8601(String ISO8601) {
        switch (ISO8601.length()) {
            case 4: // 2015-05
                return getDate("yyyy", ISO8601);
            case 7: // 2015-05
                return getDate("yyyy-MM", ISO8601);
            case 10: // 2015-05-10
                return getDate("yyyy-MM-dd", ISO8601);
            case 18: // 2015-05-10T16+0100
                return getDate("yyyy-MM-dd'T'HHZ", ISO8601);
            case 21: // 2015-05-10T16:23+0100
                return getDate("yyyy-MM-dd'T'HH:mmZ", ISO8601);
            case 22: // 2015-05-10T16:23+01:00
                return getDate("yyyy-MM-dd'T'HH:mmZZZZZ", ISO8601); // AniList
            case 24: // 2015-05-10T16:23:20+0100
                return getDate("yyyy-MM-dd'T'HH:mm:ssZ", ISO8601);
            case 25: // 2015-05-10T16:23:20+0100
                return getDate("yyyy-MM-dd'T'HH:mm:ssZZZZZ", ISO8601); // AniList
            default:
                return new Date();
        }
    }

    private static String getDateString(Date date, boolean withTime) {
        if (date == null)
            return "";

        try {
            if (withTime) { // only do "nice" formatting if time
                long diffHoursToNow = new Date().getTime() - date.getTime();
                final int MINUTE = 60000;
                final int HOUR = MINUTE * 60;
                final int DAY = HOUR * 24;

                if (diffHoursToNow < 0)
                    diffHoursToNow = date.getTime() - new Date().getTime();

                if (diffHoursToNow < HOUR)
                    return getRelativeTime(date, DateUtils.MINUTE_IN_MILLIS);

                if (diffHoursToNow < DAY)
                    return getRelativeTime(date, DateUtils.HOUR_IN_MILLIS);

                if (diffHoursToNow < DAY * 2)
                    return getRelativeTime(date, DateUtils.DAY_IN_MILLIS);

                if (isSameYear(date))
                    return DateUtils.formatDateTime(Theme.context, date.getTime(), DateUtils.FORMAT_NO_YEAR) + " " + getRelativeTimePreposition(date);
                else
                    return DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(date) + " " + getRelativeTimePreposition(date);

            } else {
                DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                return dateFormatter.format(date);
            }
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DateTools.getDate(): " + date.toString() + ": " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    private static String getRelativeTimePreposition(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(date.getTime());
        try {
            String dateString = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
            Date time = simpleDateFormat.parse(dateString + " " + dateTime.get(Calendar.HOUR_OF_DAY) + ":" + dateTime.get(Calendar.MINUTE));
            return DateUtils.getRelativeTimeSpanString(Theme.context, time.getTime(), true).toString();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static boolean isSameYear(Date date) {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTimeInMillis(date.getTime());
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) == dateTime.get(Calendar.YEAR);
    }

    private static Date getDate(String formatter, String date) {
        try {
            return (new SimpleDateFormat(formatter, Locale.getDefault())).parse(date);
        } catch (Exception e) {
            Crashlytics.log(Log.ERROR, "MALX", "DateTools.getDate(): " + formatter + ": " + e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    private static String getRelativeTime(Date date, Long minResolution) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return DateUtils.getRelativeTimeSpanString(calendar.getTimeInMillis(), System.currentTimeMillis(), minResolution).toString();
    }
}
