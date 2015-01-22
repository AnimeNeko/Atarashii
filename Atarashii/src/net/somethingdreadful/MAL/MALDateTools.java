package net.somethingdreadful.MAL;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

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
    private static final String ISO8601DATESTRING = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String MALTIMEZONE = "America/Los_Angeles";

    /*
     * this parses all the different date formats MAL returns into one normalized to store in the db
     */
    public static Date parseMALDate(String maldate) {
        // easiest possibility
        if (maldate.toLowerCase(Locale.US).equals("now")) {
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
                "\\d{4}-\\d{2}-\\d{2}",                          // yyyy-MM-dd
                "(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday), (\\d{1,2}):(\\d{2}) (AM|PM)", // EEEE, h:m a
                "(January|February|March|April|May|June|July|August|September|October|November|December) +\\d{1,2}, \\d{4}", // MMMM dd, yyyy
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
                            Crashlytics.log(Log.ERROR, "MALX", "MALDateTools.parseMALDate(): case 0: " + e.getMessage());
                            Crashlytics.logException(e);
                        }
                        break;
                    case 1: // MM-dd-yy, h:m a
                        sdf = new SimpleDateFormat("MM-dd-yy, h:m a", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone(MALTIMEZONE));
                        try {
                            Date result = sdf.parse(maldate);
                            return result;
                        } catch (ParseException e) {
                            Crashlytics.log(Log.ERROR, "MALX", "MALDateTools.parseMALDate(): case 1: " + e.getMessage());
                            Crashlytics.logException(e);
                        }
                        break;
                    case 2: // yyyy-MM-dd
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone(MALTIMEZONE));
                        try {
                            Date result = sdf.parse(maldate);
                            return result;
                        } catch (ParseException e) {
                            Crashlytics.log(Log.ERROR, "MALX", "MALDateTools.parseMALDate(): case 2: " + e.getMessage());
                            Crashlytics.logException(e);
                        }
                        break;
                    case 3: // EEEE, h:m a
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
                    case 4: //MMMM dd, yyyy
                        sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone(MALTIMEZONE));
                        try {
                            Date result = sdf.parse(maldate);
                            return result;
                        } catch (ParseException e) {
                            Crashlytics.log(Log.ERROR, "MALX", "MALDateTools.parseMALDate(): case 4: " + e.getMessage());
                            Crashlytics.logException(e);
                        }
                        break;
                    case 5: // Yesterday, h:m a
                        cal = Calendar.getInstance(TimeZone.getTimeZone(MALTIMEZONE));
                        cal.add(Calendar.DATE, -1);
                        cal.set(Calendar.HOUR, Integer.parseInt(matcher.group(1)));
                        cal.set(Calendar.MINUTE, Integer.parseInt(matcher.group(2)));
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.AM_PM, matcher.group(3).equals("AM") ? Calendar.AM : Calendar.PM);
                        return cal.getTime();
                    case 6:
                        Date result = new Date();
                        int count = Integer.parseInt(matcher.group(1));
                        String unit = matcher.group(2);
                        if (unit.matches("minute[s?]"))
                            result.setTime(result.getTime() - (count * 60000));
                        else if (unit.matches("hour[s?]"))
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
        if (date != null)
            return formatSdf.format(date);

        // return empty string if parsing failed
        return "";
    }

    public static String formatDate(Date date, Context context, boolean withtime) {
        if (date == null)
            return "";
        SimpleDateFormat formatSdf;
        Resources res = context.getResources();

        if (withtime) { // only do "nice" formatting if time
            long diffHoursToNow = new Date().getTime() - date.getTime();
            final int MINUTE = 60000;
            final int HOUR = MINUTE * 60;
            final int DAY = HOUR * 24;

            if (diffHoursToNow > 0) {
                if (diffHoursToNow < HOUR) {
                    int minutes = (int) diffHoursToNow / MINUTE;
                    return res.getQuantityString(R.plurals.minutes_ago, minutes, minutes);
                }

                if (diffHoursToNow < DAY) {
                    int hours = (int) diffHoursToNow / HOUR;
                    return res.getQuantityString(R.plurals.hours_ago, hours, hours);
                }

                if (diffHoursToNow < DAY * 2) {
                    String dateformat_yesterday = res.getString(R.string.datetimeformat_yesterday);
                    formatSdf = new SimpleDateFormat(dateformat_yesterday);
                    return formatSdf.format(date);
                }

                if (diffHoursToNow < DAY * 5) {
                    String dateformat_dayname = res.getString(R.string.datetimeformat_dayname);
                    formatSdf = new SimpleDateFormat(dateformat_dayname);
                    return formatSdf.format(date);
                }
            } else {
                diffHoursToNow = date.getTime() - new Date().getTime();
                if (diffHoursToNow < HOUR) {
                    int minutes = (int) diffHoursToNow / MINUTE;
                    return res.getQuantityString(R.plurals.minutes_left, minutes, minutes);
                }

                if (diffHoursToNow < DAY) {
                    int hours = (int) diffHoursToNow / HOUR;
                    return res.getQuantityString(R.plurals.hours_left, hours, hours);
                }

                if (diffHoursToNow < DAY * 2) {
                    String dateformat_yesterday = res.getString(R.string.datetimeformat_tomorrow);
                    formatSdf = new SimpleDateFormat(dateformat_yesterday);
                    return formatSdf.format(date);
                }

                if (diffHoursToNow < DAY * 5) {
                    String dateformat_dayname = res.getString(R.string.datetimeformat_dayname);
                    formatSdf = new SimpleDateFormat(dateformat_dayname);
                    return formatSdf.format(date);
                }
            }

            String dateformat = res.getString(R.string.datetimeformat);
            formatSdf = new SimpleDateFormat(dateformat);
            return formatSdf.format(date);
        } else {
            String dateformat = res.getString(R.string.dateformat);
            formatSdf = new SimpleDateFormat(dateformat);
            return formatSdf.format(date);
        }
    }

    private static Date parseISODate(String date) {
        Date result = null;
        String[] isoFormats = {
                ISO8601DATESTRING,
                "yyyy-MM-dd'T'HH:mmZ",
                "yyyy-MM-dd'T'HHZ"
        };
        int i = 0;

        while (i < isoFormats.length && result == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(isoFormats[i], Locale.US);
            i++;
            try {
                result = sdf.parse(date);
            } catch (ParseException e) {
                // really nothing to do here
            }
        }

        return result;
    }

    public static String formatDateString(String date, Context context, boolean withtime) {
        Date result = parseISODate(date);
        if (result == null) {
            result = parseMALDate(date);
        }

        if (result != null) {
            return formatDate(result, context, withtime);
        }

        // return empty string if parsing failed
        return "";
    }
}
