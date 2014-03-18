package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MALDateTools {
    private static final String ISO8601DATESTRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String MALTIMEZONE = "America/Los_Angeles";

    /*
     * this parses all the different date formats MAL returns into one normalized to store in the db
     */
    public static Date parseMALDate(String maldate) {
        // easiest possibility
        if (maldate.equals("now")) {
            Date result = new Date();
            return result;
        }

        /* simply using SimpleDateformat and parse different strings is not working, because
         * SDF can't distinguish between the format "yyyy-MM-dd, h:m a" and "MM-dd-yy, h:m a" (which
         * are both used by MAL) and it also can't parse relative dates like "<Weekday>, h:m a" or
         * "x hours ago", so determine the date format by using regex
         */
        String[] date_regex = {
                "\\d{4}-\\d{2}-\\d{2}, \\d{1,2}:\\d{2} (AM|PM)", // yyyy-MM-dd, h:m a
                "\\d{2}-\\d{2}-\\d{2}, \\d{1,2}:\\d{2} (AM|PM)", // MM-dd-yy, h:m a
                "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday), (\\d{1,2}):(\\d{2}) (AM|PM)", // EEEE, h:m a
                "Yesterday, (\\d{1,2}):(\\d{2}) (AM|PM)", // Yesterday, h:m a
                "(\\d*) (hours?|minutes?) ago" // x hours/minutes ago
        };

        for (int i = 0; i < date_regex.length; i++) {
            Pattern pattern = Pattern.compile(date_regex[i]);
            Matcher matcher = pattern.matcher(maldate);
            SimpleDateFormat sdf;
            Calendar cal;
            if (matcher.find()) {
                switch (i) {
                    case 0: // yyyy-MM-dd, h:m a
                        sdf = new SimpleDateFormat("yyyy-MM-dd, h:m a", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone(MALTIMEZONE));
                        try {
                            Date result = sdf.parse(maldate);
                            return result;
                        } catch (ParseException e) {
                            Log.e("MALX", "parsing exception: " + e.getMessage());
                        }
                        break;
                    case 1: // MM-dd-yy, h:m a
                        sdf = new SimpleDateFormat("MM-dd-yy, h:m a", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone(MALTIMEZONE));
                        try {
                            Date result = sdf.parse(maldate);
                            return result;
                        } catch (ParseException e) {
                            Log.e("MALX", "parsing exception: " + e.getMessage());
                        }
                        break;
                    case 2: // EEEE, h:m a
                        String[] week_days = {
                                "Sunday",
                                "Monday",
                                "Tuesday",
                                "Wednesday",
                                "Thursday",
                                "Friday",
                                "Saturday"
                        };
                        cal = Calendar.getInstance(TimeZone.getTimeZone(MALTIMEZONE));
                        int currentdayofweek = cal.get(Calendar.DAY_OF_WEEK);
                        int dayofweek = Arrays.asList(week_days).indexOf(matcher.group(1)) + 1;
                        int difference = currentdayofweek - dayofweek;
                        if (difference <= 0) // if difference is < 0, then it was in the last week
                            difference += 7;

                        cal.add(Calendar.DATE, difference > 0 ? difference * -1 : difference);
                        cal.set(Calendar.HOUR, Integer.parseInt(matcher.group(2)));
                        cal.set(Calendar.MINUTE, Integer.parseInt(matcher.group(3)));
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.AM_PM, matcher.group(4).equals("AM") ? Calendar.AM : Calendar.PM);
                        return cal.getTime();
                    case 3: // Yesterday, h:m a
                        cal = Calendar.getInstance(TimeZone.getTimeZone(MALTIMEZONE));
                        cal.add(Calendar.DATE, -1);
                        cal.set(Calendar.HOUR, Integer.parseInt(matcher.group(1)));
                        cal.set(Calendar.MINUTE, Integer.parseInt(matcher.group(2)));
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.AM_PM, matcher.group(3).equals("AM") ? Calendar.AM : Calendar.PM);
                        return cal.getTime();
                    case 4:
                        Date result = new Date();
                        int count = Integer.parseInt(matcher.group(1));
                        String unit = matcher.group(2);
                        if ( unit.matches("minute[s?]") )
                            result.setTime(result.getTime() - (count * 60000));
                        else if ( unit.matches("hour[s?]") )
                            result.setTime(result.getTime() - (count * 3600000));
                        return result;
                }
            }
        }
        return null;
    }

    public static String parseMALDateToISO8601String(String maldate) {
        SimpleDateFormat formatSdf = new SimpleDateFormat(ISO8601DATESTRING);

        Date date = parseMALDate(maldate);
        if ( date != null )
            return formatSdf.format(date);

        // return unformatted if parsing failed
        return maldate;
    }

    public static String formatDate(Date date, Context context) {
        Resources res = context.getResources();

        long diffHoursToNow = new Date().getTime() - date.getTime();
        final int MINUTE = 60000;
        final int HOUR = MINUTE * 60;
        final int DAY = HOUR * 24;

        if (diffHoursToNow < HOUR) {
            int minutes = (int) diffHoursToNow / MINUTE;
            return res.getQuantityString(R.plurals.minutes_ago, minutes, minutes);
        }

        if (diffHoursToNow < DAY) {
            int hours = (int) diffHoursToNow / HOUR;
            return res.getQuantityString(R.plurals.hours_ago, hours, hours);
        }

        SimpleDateFormat formatSdf;
        if ( diffHoursToNow < DAY * 2) {
            String dateformat_yesterday = res.getString(R.string.dateformat_yesterday);
            formatSdf = new SimpleDateFormat(dateformat_yesterday);
            return formatSdf.format(date);
        }

        if ( diffHoursToNow < DAY * 5) {
            String dateformat_dayname = res.getString(R.string.dateformat_dayname);
            formatSdf = new SimpleDateFormat(dateformat_dayname);
            return formatSdf.format(date);
        }

        String dateformat = res.getString(R.string.dateformat);
        formatSdf = new SimpleDateFormat(dateformat);
        return formatSdf.format(date);
    }

    public static String formatISO8601DateString(String date, Context context) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601DATESTRING);
        try {
            Date result = sdf.parse(date);
            return formatDate(result, context);
        } catch (ParseException e) {
            Log.e("MALX", "parsing exception: " + e.getMessage());
        }
        // return unformatted if parsing failed
        return date;
    }
}
