package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;

public class DateHelper {
    public static final Logger logger = LoggerFactory.getLogger(DateHelper.class);
    // Iso 8601 date/time format
    public static final String ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm";

    // Iso 8601 date/time format without the 'T'
    public static final String ISO_8601_DATE_TIME_FORMAT_SIMPLE = "yyyy-MM-dd HH:mm";

    // Iso 8601 date format
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd";

    // The standard way of formatting time with seconds
    public static final String REGULAR_TIME_FORMAT = "HH:mm:ss";

    public static final String SIMPLE_TIMESTAMP_FORMAT = "MM-dd-yyyy HH:mm:ss";

    // Standard formatting of time without seconds
    public static final String REGULAR_TIME_FORMAT_WITHOUT_SECONDS = "HH:mm";

    // Standard US representation of a date
    public static final String US_DATE_FORMAT = "MM/dd/yyyy";

    public static final String US_DATE_FORMAT_AM_PM = "MM/dd/yyyy hh:mm:ss a";

    public static final String US_DATE_FORMAT_FTP = "MMddyyyyhhmmss";

    // Standard US representation of a date
    public static final String INDIA_DATE_FORMAT = "dd/MM/yyyy";

    public static final String ISO_8601_DATE_FORMAT_DAY_FIRST = "dd-MM-yyyy";

    // Standard US representation of date and time with seconds
    public static final String US_DATE_TIME_FORMAT = US_DATE_FORMAT + " " + REGULAR_TIME_FORMAT;

    // Standard US representation of date and time without seconds
    public static final String US_DATE_TIME_FORMAT_WITHOUT_SECONDS = US_DATE_FORMAT + " "
            + REGULAR_TIME_FORMAT_WITHOUT_SECONDS;

    // The days of the week as a string array
    protected static final String[] DAYS_OF_THE_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday"};

    // The empty string
    public static final String EMPTY_STRING = "";

    // The year 1900
    public static final int YEAR_1900 = 1900;

    // The year 1900 as a string
    public static final String YEAR_1900_STRING = "1900";

    // First day string, holds what is usually considered the day a schedule
    public static final String FIRST_DAY_STRING = "01/01/1900";

    // Second day string, holds what is considered the following day that a
    // schedule can cross over into.
    public static final String SECOND_DAY_STRING = "01/02/1900";

    // The END OF TIME
    public static final String END_OF_TIME_STRING = "12/31/3000";

    // Midnight tomorrow
    public static final String MIDNIGHT_TOMORROW = "01/02/1900 00:00";

    /**
     * Adds a specified number of days to a date
     *
     * @param date      the date to add days to
     * @param daysToAdd the number of days to add;
     * @return a new date that is the old date plus a number of days
     */
    public static Date addDaysToDate(final Date date, final int daysToAdd) {
        final Calendar cal = Calendar.getInstance();
        return addDaysToDate(date, daysToAdd, cal);
    }

    /**
     * Adds a specified number of days to a date using the given calendar for
     * performance
     *
     * @param date      the date to add days to
     * @param daysToAdd the number of days to add
     * @param cal       a calendar to use in the operation
     * @return a new date that is the old date plus the number of days
     */
    public static Date addDaysToDate(final Date date, final int daysToAdd, final Calendar cal) {
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
        return cal.getTime();
    }

    /**
     * Method will add the specified number of hours, minutes and sconds to the
     * given date.
     *
     * @param date    the date to add days to
     * @param hours   the number of hours to add
     * @param minuts  the number of minutes to add
     * @param seconds the number of seconds to add
     * @return new date that is the old date plus the time
     */
    public static Date addHourMinutesAndSeconds(final Date date, final Integer hours, final Integer minuts,
                                                final Integer seconds) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minuts);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Method will add the specified number of hours and minutes to the given date.
     */
    public static Date addHoursToDate(final Date startDate, final Integer hours, final Integer minutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.HOUR, hours.intValue());
        cal.add(Calendar.MINUTE, minutes.intValue());
        return cal.getTime();
    }

    /**
     * Method will add the specified number of hours and minutes to the given date.
     */
    public static Date addHoursToDate(final Date startDate, final Integer hours, final Integer minutes,
                                      final Integer seconds) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.HOUR, hours.intValue());
        cal.add(Calendar.MINUTE, minutes.intValue());
        cal.add(Calendar.SECOND, seconds.intValue());
        return cal.getTime();
    }

    /**
     * Checks if this time range crosses midnight, if the end time is at midnight,
     * that is not crossing.
     *
     * @param startDate the start date time
     * @param endDate   the end date time
     */
    public static boolean checkCrossesMidnight(final Date startDate, final Date endDate) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        final int dayOfStart = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(endDate);
        return dayOfStart != cal.get(Calendar.DAY_OF_MONTH)
                && !(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0);
    }

    /**
     * Returns a single date that encompasses the information stored in the date and
     * the timestamp, and time seperately.
     *
     * @param date a date object used to store ONLY the date
     * @param time a timestamp used to store ONLY the time (and a little bit of info
     *             about what day this time occurs on DOH!)
     * @return a date object that holds the date and time represented by the two
     * objects passed in
     */
    public static Date combineDateAndTime(final Date date, final Date time) {
        final Calendar cal = Calendar.getInstance();
        return combineDateAndTime(date, time, cal);
    }

    /**
     * Returns a single date that encompasses the information stored in the date and
     * the timestamp, this is necessary because stores the date and time seperately.
     * Receives a calendar object for a possible performance enhancement.
     *
     * @param date a date object used to store ONLY the date
     * @param time a timestamp used to store ONLY the time (and a little bit of info
     *             about what day this time occurs on DOH!)
     * @param cal  calendar object to use when combining date and time
     * @return a date object that holds the date and time represented by the two
     * objects passed in
     */
    public static Date combineDateAndTime(final Date date, final Date time, final Calendar cal) {
        cal.setTime(time);
        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int addVal = getDaysToAdd(cal);
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, addVal);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        return cal.getTime();
    }

    /**
     * Combines a dateString and a timeString containing date related information
     * into a single date object
     *
     * @param dateString         the date
     * @param timeWithDateString the time, with some information related to date
     * @param dateFormat         the format of the date information, both in the
     *                           date string, and the time string
     * @param timeFormat         the format of the time
     * @return a date object that combines the data in the two strings
     * @throws ParseException if a parse exception occurs
     */
    public static Date combineDateAndTime(final String dateString, final String timeWithDateString,
                                          final String dateFormat, final String timeFormat) throws ParseException {
        final Calendar cal = Calendar.getInstance();
        return combineDateAndTime(dateString, timeWithDateString, dateFormat, timeFormat, cal);
    }

    /**
     * Combines a dateString and a timeString containing date related information
     * into a single date object using the given calander
     *
     * @param dateString         the date
     * @param timeWithDateString the time, with some information related to date
     * @param dateFormat         the format of the date information, both in the
     *                           date string, and the time string
     * @param timeFormat         the format of the time
     * @param cal                the calendar to use when combining the date and
     *                           time
     * @return a date object that combines the data in the two strings
     * @throws ParseException if a parse exception occurs
     */
    public static Date combineDateAndTime(final String dateString, final String timeWithDateString,
                                          final String dateFormat, final String timeFormat, final Calendar cal) throws ParseException {
        final Date timeWithDate = getDateFromString(timeWithDateString, dateFormat + " " + timeFormat);
        final Date date = getDateFromString(dateString, dateFormat);
        return combineDateAndTime(date, timeWithDate, cal);
    }

    private static long daysBetween(final Calendar startDate, final Calendar endDate) {
        final Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    /**
     * Fixes time pairs which should cross midnight by returning the correct end
     * date
     *
     * @param newStartDate the start date
     * @param newEndDate   the end date
     * @return returns the fixed end date, or the original end date if no change was
     * made
     */
    public static Date fixCrossingMidnight(final Date newStartDate, final Date newEndDate) {
        final float jobHours = DateHelper.getJobHours(newStartDate, newEndDate);
        if (jobHours <= 0) {
            return DateHelper.addDaysToDate(newEndDate, 1);
        }
        return newEndDate;
    }

    /**
     * Returns the current time as a date object
     *
     * @return the current time as a date object
     */
    public static Date getCurrentTime() {
        return new Date();
    }

    /**
     * To get the time difference between supplied start and end time
     *
     * @return return the difference in days as a float
     */
    public static long getDateDifference(final Date startDate, final Date endDate) {
        try {
            final Date startTime = getDateFromString(getFormattedDateString(startDate, ISO_8601_DATE_FORMAT),
                    ISO_8601_DATE_FORMAT);
            final Date endTime = getDateFromString(getFormattedDateString(endDate, ISO_8601_DATE_FORMAT),
                    ISO_8601_DATE_FORMAT);
            if (startTime == null || endTime == null) {
                return 0L;
            } else {
                final Calendar startCal = Calendar.getInstance();
                startCal.setTime(startTime);
                final Calendar endCal = Calendar.getInstance();
                endCal.setTime(endTime);
                if (endCal.getTimeInMillis() >= startCal.getTimeInMillis()) {
                    return daysBetween(startCal, endCal);
                } else {
                    return -daysBetween(endCal, startCal);
                }
            }
        } catch (final ParseException e) {
            logger.error(ApplicationConstants.CATCH_MESSAGE + e);
            logger.error("Inside getDateDifference method :" + e);
        }
        return 0;
    }

    /**
     * Given a start time and an end time returns the number of days between them,
     * uses the given time format to parse the times.calculate the differences
     * between two dates. First, you'll need to convert the date value into its
     * corresponding value in milliseconds and then do the math calculation so we
     * can get the difference in days
     *
     * @param startTime  the start time
     * @param endTime    the end time
     * @param timeFormat how to parse the times
     * @return the number of hours between the times
     * @throws ParseException if the times could not be parsed
     */
    public static long getDateDifference(final String startTime, final String endTime, String timeFormat)
            throws ParseException {
        if (timeFormat == null) {
            timeFormat = REGULAR_TIME_FORMAT;
        }
        final Date startDate = getDateFromString(startTime, timeFormat);
        final Date endDate = getDateFromString(endTime, timeFormat);
        return getDateDifference(startDate, endDate);
    }

    /**
     * Given a string that represents a date, a time, or a date/time, and the format
     * of the data, returns a Date object that represents the String.
     *
     * @param dateString a string that holds date and or time information
     * @param format     the format of the data
     * @return a date representing the given string
     * @throws ParseException if there was an issue parsing the date/time
     */
    public static Date getDateFromString(final String dateString, final String format) throws ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(dateString);
    }

    /**
     * Returns a fully constructed date object encompassing the data stored in the
     * dateString and timeString
     *
     * @param dateString a string representing the date
     * @param timeString a string representing the time
     * @param dateFormat the format the date is in
     * @param timeFormat the format the time is in
     * @return a date object that contains all data store in the strings
     * @throws ParseException if the date or time could not be parsed
     */
    public static Date getDateTimeFromStrings(final String dateString, final String timeString, final String dateFormat,
                                              final String timeFormat) throws ParseException {
        return getDateFromString(dateString + timeString, dateFormat + timeFormat);
    }

    /**
     * Determines if a day should be added to the date based on the date stored with
     * a time, dates and times
     *
     * @param cal the calendar holding the time w/date information
     * @return the number of days to add
     */
    public static int getDaysToAdd(final Calendar cal) {
        if (cal.get(Calendar.YEAR) == YEAR_1900 && cal.get(Calendar.MONTH) == Calendar.JANUARY
                && cal.get(Calendar.DAY_OF_MONTH) == 2) {
            return 1;
        }
        return 0;
    }

    /**
     * This method returns a formatted date string foramtted by the given format
     *
     * @param date   a date to retrieve a formatted string for
     * @param format how to format the date
     * @return a formatted string version of the given date
     */
    public static String getFormattedDateString(final Date date, final String format) {
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date != null) {
            return sdf.format(date);
        } else {
            return null;
        }
    }

    /**
     * The method takes Date as a parameter and returns its String representation in
     * ISO8601 format.
     *
     * @param date The date to be formated to ISO8601 pattern.
     * @return String representation of the date in ISO8601 format.
     */
    public static String getISO8601Date(final Date date) {
        return getFormattedDateString(date, ISO_8601_DATE_TIME_FORMAT);
    }

    /**
     * To get the time difference between supplied start and end time
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return return the difference in hours as a float
     */
    public static float getJobHours(final Date startTime, final Date endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        } else {
            return (float) (endTime.getTime() - startTime.getTime()) / (float) (1000 * 60 * 60);
        }
    }

    /**
     * Given a start time and an end time returns the number of hours between them,
     * uses the given time format to parse the times
     *
     * @param startTime  the start time
     * @param endTime    the end time
     * @param timeFormat how to parse the times
     * @return the number of hours between the times
     * @throws ParseException if the times could not be parsed
     */
    public static float getJobHours(final String startTime, final String endTime, String timeFormat)
            throws ParseException {
        if (timeFormat == null) {
            timeFormat = REGULAR_TIME_FORMAT;
        }
        final Date jobStrtTime = getDateFromString(startTime, timeFormat);
        final Date jobEndTime = getDateFromString(endTime, timeFormat);
        return getJobHours(jobStrtTime, jobEndTime);
    }

    /**
     * Computes and returns a string representation of the actual time from schedule
     * date and job time stored in database.
     *
     * @param schForDt Schedule Date
     * @param jobTime  Job time
     * @return Computed job time.
     */
    public static String getJobTime(final Date schForDt, final Date jobTime) {
        if (schForDt != null && jobTime != null) {
            final Date realDate = combineDateAndTime(schForDt, jobTime);
            return getISO8601Date(realDate);
        }
        return EMPTY_STRING;
    }

    /**
     * Extracts the decimal component in the passed {@link BigDecimal} as minutes
     *
     * @param duration
     * @return {@link Integer}
     */
    public static Integer getMinutesAfterHour(final BigDecimal duration) {
        final BigDecimal minutes = duration.multiply(new BigDecimal("60"), MathContext.DECIMAL32);
        return minutes.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    /**
     * Converts a java util date to a java sql date
     *
     * @param date the date to be converted
     * @return java sql date
     */
    public static java.sql.Date getSQLDate(final Date date) {
        return new java.sql.Date(date.getTime());
    }

    /**
     * Returns an sql date given a date as a string and the format of that date
     *
     * @param date   the date to parse and return an sql date for
     * @param format the format of the date in the string
     * @return an sql date created from the given date string
     * @throws ParseException if the date could not be parsed
     */
    public static java.sql.Date getSQLDate(final String date, final String format) throws ParseException {
        final Date d = getDateFromString(date, format);
        return getSQLDate(d);
    }

    /**
     * Converts a date to a string suitable for inclusion in an SQL query
     *
     * @param date the date to be converted
     * @return A string representation of the date in SQL format
     */
    public static String getSQLDateString(final Date date) {
        return "{d'" + getFormattedDateString(date, ISO_8601_DATE_FORMAT) + "'}";
    }

    /**
     * Converts a date to a string suitable for inclusion in an SQL query
     *
     * @param date the date to be converted yyyy-mm-dd format
     * @return A string representation of the date in SQL format
     */
    public static String getSQLDateFormatString(final Date date) {
        return getFormattedDateString(date, ISO_8601_DATE_FORMAT);
    }

    /**
     * Converts a string with a specified format into a string suitable for an SQL
     * query
     *
     * @param date   A string representation of a date
     * @param format the format the date is currently in
     * @return A string representation of the date in SQL format
     * @throws ParseException if the provided date is unparseable
     */
    public static String getSQLDateString(final String date, final String format) throws ParseException {
        final Date d = getDateFromString(date, format);
        return getSQLDateString(d);
    }

    /**
     * Converts a java util date to a java sql timestamp
     *
     * @param date the date to be converted
     * @return java sql timestamp
     */
    public static Timestamp getSQLTimeStamp(final Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * Returns the week day for a particular date
     *
     * @param date the date to get the week day for
     * @return an integer representing the week day
     */
    public static int getWeekDay(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Returns the week day the date string would fall on
     *
     * @param dateString a string representing a date
     * @return the integer representation of the day of the week the date falls on
     * @throws ParseException if the date could not be parsed
     */
    public static int getWeekDay(final String dateString) throws ParseException {
        final Date date = getDateFromString(dateString, US_DATE_FORMAT);
        return getWeekDay(date);
    }

    /**
     * To get the time difference between supplied start and end time in year
     *
     * @param startTime the start time
     * @param endTime   the end time
     * @return return the difference in hours as a float
     */
    public static long getYearDifference(final Date startTime, final Date endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        } else {
            final Calendar startCal = Calendar.getInstance();
            startCal.setTime(startTime);
            final Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);
            if (endCal.getTimeInMillis() >= startCal.getTimeInMillis()) {
                return yearBetween(startCal, endCal);
            } else {
                return -yearBetween(endCal, startCal);
            }
        }
    }

    /**
     * Calculates midnight of the day in which date lies with respect to a time
     * zone.
     **/

    public static Date midnight(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * This method is used to get the Start Date of the Current month
     *
     * @return Calendar
     */
    public static Calendar getCalendarInstance() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    private static long yearBetween(final Calendar startDate, final Calendar endDate) {
        final Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.YEAR, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    // Utility class, do not instantiate
    private DateHelper() {
    }

    public static Integer daysBetween(final Calendar startDate) {

        final Calendar modifiedDate = (Calendar) startDate.clone();
        final Calendar currentDate = Calendar.getInstance();

        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();

        calendar1.set(modifiedDate.get(Calendar.YEAR), modifiedDate.get(Calendar.MONTH),
                modifiedDate.get(Calendar.DAY_OF_MONTH));
        calendar2.set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH));

        long diff = calendar2.getTimeInMillis() - calendar1.getTimeInMillis();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return (int) diffDays;

    }

    public static Integer daysBetweenTwoDates(final Calendar startDate, final Calendar endDate) {

        long diff = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        long diffDays = diff / (24 * 60 * 60 * 1000);
        return (int) diffDays + 1;

    }

    public static Date getLastDateOfCurrentWeek() {
        Calendar c = Calendar.getInstance();
        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // Print dates of the current week starting on Monday
        c.add(Calendar.DATE, 6);
        return c.getTime();
    }

    public static String getFormattedCurrentTimeString() {
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(REGULAR_TIME_FORMAT);
        return sdf.format(cal.getTime());
    }

    public static String getZeroAppendedString(Integer hm) {

        String actaulString = "";

        if (hm < 10) {
            actaulString = "0" + hm.toString();
        } else {
            actaulString = hm.toString();
        }

        return actaulString;
    }

    public static Boolean compaireDateWithCurrentDate(Date date) {
        Date currentDate = new Date();

        if (date.compareTo(currentDate) > 0) {

            return false;
        } else if (date.compareTo(currentDate) <= 0) {

            return true;
        }

        return true;
    }

    /**
     * This method will return the system current date in the format "WeekDay, Day
     * Month Year"
     * <p>
     * eg- Tue,30 June 2015
     *
     * @return date in string format.
     */
    public static String getCurrentDateWithDay() {
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy");
        return format.format(currentDate);
    }

    public static Date getDateWithoutTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getTomorrowDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    public static Long getDateDifferenceInMins(Date sDate, Date eDate) {
        Long diff = eDate.getTime() - sDate.getTime();
        return diff / (60 * 1000) % 60;
    }

    public static final String getDateTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");
        return df.format(new Date());
    }

}
