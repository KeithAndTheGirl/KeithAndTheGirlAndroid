package com.keithandthegirl.app.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class StringUtils {

    public static final String EMPTY_STRING = "";
    public static final String NEW_LINE = System.getProperty("line.separator");

    public static boolean isNullOrEmpty(String string) {
        return (string == null || string.length() == 0);
    }

    public static boolean isNullOrEmpty(CharSequence sequence) {
        return (sequence == null || sequence.length() == 0);
    }

    public static String urlEncodeUtf8(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, "UTF-8");
    }

    public static String ceateDashlessUUID() {
        return UUID.randomUUID().toString().replace("-", EMPTY_STRING);
    }

    /**
     * Returns title case of string.
     *
     * @param string input
     * @return titleCase string
     */
    public static String titleCase(String string) {
        StringBuilder builder = new StringBuilder();

        // split into words
        String[] parts = string.split("\\s+");

        // each word
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (part.length() > 0) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ENGLISH));
            }
        }

        return builder.toString();
    }

    /**
     * Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     *
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if (source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        do {
            i--;
        } while (i >= 0 && Character.isWhitespace(source.charAt(i)));

        return source.subSequence(0, i + 1);
    }

    public static String stringFromBuffer(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public static String getRemainingTimeFromDateAsHtmlString(long releaseDateLong, boolean image) {
        String resultString = StringUtils.EMPTY_STRING;
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(releaseDateLong);

        long YEAR_MILLIS    = 1000L * 60 * 60 * 24 * 365;
        long MONTH_MILLIS   = 1000L * 60 * 60 * 24 * 30 ;
        long WEEK_MILLIS    = 1000L * 60 * 60 * 24 * 7;
        long DAY_MILLIS     = 1000L * 60 * 60 * 24;
        long HOUR_MILLIS    = 1000L * 60 * 60;
        long MINUTE_MILLIS  = 1000L * 60;
        long SECONDS_MILLIS = 1000;

        String verb = image ? "View" : "Play";

        Date now = new Date();
        long delta = releaseDateLong - now.getTime();

        if (delta < MINUTE_MILLIS) {
            int seconds = (int) (delta / SECONDS_MILLIS);
            resultString = String.format("%s in <big>%d</big> seconds", verb, seconds);
        } else if (delta < HOUR_MILLIS) {
            int minutes = (int) (delta / MINUTE_MILLIS);
            int seconds = (int) ((delta % MINUTE_MILLIS) / SECONDS_MILLIS);
            if (seconds > 0) {
                resultString = String.format("%s in <big>%d</big> minutes <big>%d</big> seconds", verb, minutes, seconds);
            } else {
                resultString = String.format("%s in <big>%d</big> minutes", verb, minutes);
            }
        } else if (delta < DAY_MILLIS) {
            int hours = (int) (delta / HOUR_MILLIS);
            int minutes = (int) ((delta % HOUR_MILLIS) / MINUTE_MILLIS);
            if (minutes > 0) {
                resultString = String.format("%s in <big>%d</big> hours <big>%d</big> minutes", verb, hours, minutes);
            } else {
                resultString = String.format("%s in <big>%d</big> hours", verb, hours);
            }
        } else if (delta < WEEK_MILLIS) {
            int days = (int) (delta / DAY_MILLIS);
            int hours = (int) ((delta % DAY_MILLIS) / HOUR_MILLIS);
            if (hours > 0) {
                resultString = String.format("%s in <big>%d</big> days <big>%d</big> hours", verb, days, hours);
            } else {
                resultString = String.format("%s in <big>%d</big> days", verb, days);
            }
        } else if (delta < MONTH_MILLIS) {
            int weeks = (int) (delta / WEEK_MILLIS);
            int days = (int) ((delta % WEEK_MILLIS) / DAY_MILLIS);
            if (days > 0) {
                resultString = String.format("%s in <big>%d</big> weeks <big>%d</big> days", verb, weeks, days);
            } else {
                resultString = String.format("%s in <big>%d</big> weeks", verb, weeks);
            }
        } else if (delta < YEAR_MILLIS) {
            int months = (int) (delta / MONTH_MILLIS);
            int weeks = (int) ((delta % MONTH_MILLIS) / WEEK_MILLIS);
            if (weeks > 0) {
                resultString = String.format("%s in <big>%d</big> months <big>%d</big> weeks", verb, months, weeks);
            } else {
                resultString = String.format("%s in <big>%d</big> months", verb, months);
            }
        } else {
            android.text.format.DateUtils.getRelativeTimeSpanString(
                    releaseDateLong,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.FORMAT_SHOW_YEAR,
                    android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE);
        }

        return resultString;
    }
}
