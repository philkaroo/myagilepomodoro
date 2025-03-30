/* 
 * Copyright (C) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mypomodoro.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeConstants;

/**
 * Date utility class
 *
 */
public class DateUtil {

    public static final Locale US_LOCALE = new Locale("en", "US");
    private static Locale locale = US_LOCALE;
    private static final String US_datePattern = "MMMM dd yyyy";
    private static final String US_shortDatePattern = "MM/dd/yyyy";
    private static final String US_shortDatePatternNoYear = "MM/dd";
    private static final String EN_timePattern = "hh:mm a"; // AM/PM

    public DateUtil(Locale locale) {
        DateUtil.locale = locale;
    }

    public static Locale getLocale() {
        return locale;
    }

    public static String getFormatedDate(Date date) {
        String pattern = locale.equals(US_LOCALE) ? US_datePattern : "dd MMMM yyyy";
        return getFormatedDate(date, pattern);
    }

    public static String getLongFormatedDate(Date date) {
        String pattern = "EEEE, " + (locale.equals(US_LOCALE) ? US_datePattern : "dd MMMM yyyy");
        return getFormatedDate(date, pattern);
    }

    public static String getShortFormatedDate(Date date) {
        String pattern = locale.equals(US_LOCALE) ? US_shortDatePattern : "dd/MM/yyyy";
        return getFormatedDate(date, pattern);
    }

    public static String getShortFormatedDateNoYear(Date date) {
        String pattern = locale.equals(US_LOCALE) ? US_shortDatePatternNoYear : "dd/MM";
        return getFormatedDate(date, pattern);
    }

    public static String getFormatedDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        return sdf.format(date);
    }

    public static String getFormatedTime(Date date) {
        String pattern = locale.getLanguage().equals("en") ? EN_timePattern : "HH:mm";
        return getFormatedTime(date, pattern);
    }

    public static String getFormatedTime(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        return sdf.format(date);
    }

    public static boolean isUSLocale() {
        return locale.equals(US_LOCALE);
    }

    /*
     * Converts a string into a date
     * 
     * @param formatedDate string with date and time (eg "13/05/2000")
     * @param pattern pattern of the date (eg dd/MM/yyy)
     */
    public static Date getDate(String formatedDate, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(formatedDate);
    }

    /*
     * Check if a date is today
     * 
     * @param date
     */
    public static boolean isDateToday(Date date) {
        DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();
        return dateComparator.compare(date, new Date()) == 0;
    }

    /*
     * Check if a date is in the past
     * 
     * @param date
     */
    public static boolean inPast(Date date) {
        DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();
        return dateComparator.compare(date, new Date()) == -1;
    }

    /*
     * Check if a date is in the future
     * 
     * @param date
     */
    public static boolean inFuture(Date date) {
        DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();
        return dateComparator.compare(date, new Date()) == 1;
    }

    /*
     * Check if a date1 is sooner than date2
     * 
     * @param date1
     * @param date2
     */
    public static boolean isSooner(Date date1, Date date2) {
        DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();
        return dateComparator.compare(date1, date2) == -1;
    }

    /*
     * Check if a date1 is sooner than date2
     * 
     * @param date1
     * @param date2
     */
    public static boolean isEquals(Date date1, Date date2) {
        DateTimeComparator dateComparator = DateTimeComparator.getDateOnlyInstance();
        return dateComparator.compare(date1, date2) == 0;
    }

    /**
     * Returns an ordered list of days of month between two dates
     *
     * @param dateStart
     * @param dateEnd
     * @return array list of days of months
     */
    /*public static ArrayList<Integer> getDaysOfMonth(Date dateStart, Date dateEnd) {
     DateTime start = new DateTime(dateStart.getTime());
     DateTime end = new DateTime(dateEnd.getTime());
     ArrayList<Integer> days = new ArrayList<Integer>();
     while (start.isBefore(end) || start.isEqual(end)) {
     days.add(start.dayOfMonth().get());
     start = start.plusDays(1);
     }
     return days;
     }*/
    /**
     * Returns an ordered list of dates of month between two dates minus
     * exclusions
     *
     * @param startDate
     * @param endDate
     * @param excludeSaturdays
     * @param excludeSundays
     * @param excludeDates
     * @return array list of dates of months excluding some exceptions
     */
    public static ArrayList<Date> getDatesWithExclusions(Date startDate, Date endDate, boolean excludeSaturdays, boolean excludeSundays, ArrayList<Date> excludeDates) {
        DateTime start = new DateTime(startDate.getTime());
        DateTime end = new DateTime(endDate.getTime());
        ArrayList<Date> dates = new ArrayList<Date>();
        while (start.isBefore(end) || start.isEqual(end)) {
            if (!isExcluded(start, excludeSaturdays, excludeSundays, excludeDates)) {
                dates.add(start.toDate());
            }
            start = start.plusDays(1);
        }
        return dates;
    }

    /**
     * Check if a date is part of the exclusion list
     *
     * @param dateTime
     * @param excludeSaturdays
     * @param excludeSundays
     * @param excludeDates
     * @return true if excluded
     */
    private static boolean isExcluded(DateTime dateTime, boolean excludeSaturdays, boolean excludeSundays, ArrayList<Date> excludeDates) {
        boolean isExcluded = false;
        if (excludeSaturdays && dateTime.getDayOfWeek() == DateTimeConstants.SATURDAY) { // excluding saturdays
            isExcluded = true;
        } else if (excludeSundays && dateTime.getDayOfWeek() == DateTimeConstants.SUNDAY) { // excluding sundays
            isExcluded = true;
        } else {
            for (Date excludeDate : excludeDates) {
                if (isSameDay(excludeDate, dateTime.toDate())) {
                    isExcluded = true;
                    break;
                }
            }
        }
        return isExcluded;
    }

    /**
     * Returns the day of month of a date
     *
     * @param date
     * @return day of month
     */
    public static int convertToDayOfMonth(Date date) {
        return new DateTime(date.getTime()).dayOfMonth().get();
    }

    /**
     * Returns the day of week of a date
     *
     * @param date
     * @return day of week
     */
    public static int convertToDayOfWeek(Date date) {
        return new DateTime(date.getTime()).dayOfWeek().get();
    }

    /**
     * Compares two dates
     *
     * @param date1
     * @param date2
     * @return true is dates are equal
     */
    public static boolean isSameDay(Date date1, Date date2) {
        DateTime dateTime1 = new DateTime(date1);
        DateTime dateTime2 = new DateTime(date2);
        return dateTime1.withTimeAtStartOfDay().isEqual(dateTime2.withTimeAtStartOfDay());
    }

    /**
     * Returns the date at the start of the day
     *
     * @param date
     * @return date at start of day
     */
    public static Date getDateAtStartOfDay(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    /**
     * Returns the date at midnight
     *
     * @param date
     * @return date at midnight
     */
    public static Date getDateAtMidnight(Date date) {
        return new DateTime(date).plusDays(1).withTimeAtStartOfDay().toDate();
    }

    public static Date addMinutesToNow(int minutes) {
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    public static Date addMillisecondsToNow(long milliseconds) {
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.MILLISECOND, (int) milliseconds);
        return calendar.getTime();
    }

    // Calendar.HOUR: gets hour in 12h format
    // Calendar.HOUR_OF_DAY: gets hour in 24h format
    public static int getHourOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return locale.getLanguage().equals("en") ? calendar.get(Calendar.HOUR) : calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    public static boolean isMonday(Date date) {
        return convertToDayOfWeek(date) == DateTimeConstants.MONDAY;
    }

    public static boolean isFirstDayOfMonth(Date date) {
        DateTime dateTime = new DateTime(date);
        DateTime firstDayOfMonth = dateTime.dayOfMonth().withMinimumValue(); // fist day of month
        return dateTime.withTimeAtStartOfDay().isEqual(firstDayOfMonth.withTimeAtStartOfDay());
    }
}
