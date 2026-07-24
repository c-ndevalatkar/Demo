package com.symphony.applaunch.util;

import com.symphony.applaunch.constants.ApplicationConstants;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateHelperTest {

    // Helper to build fixed dates safely
    private Date date(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, hour, minute, second);
        return cal.getTime();
    }

    @Test
    void addDaysToDate_shouldAddGivenDays() {
        Date base = date(2020, 1, 10, 10, 0, 0);
        Date result = DateHelper.addDaysToDate(base, 5);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void addHourMinutesAndSeconds_shouldSetExactTime() {
        Date base = date(2020, 1, 10, 0, 0, 0);

        Date result = DateHelper.addHourMinutesAndSeconds(base, 13, 45, 30);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void addHoursToDate_withMinutes_shouldAddProperly() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date result = DateHelper.addHoursToDate(start, 2, 30);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
    }

    @Test
    void addHoursToDate_withSeconds_shouldAddProperly() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date result = DateHelper.addHoursToDate(start, 1, 15, 20);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(11, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
        assertEquals(20, cal.get(Calendar.SECOND));
    }

    @Test
    void checkCrossesMidnight_trueWhenDayChangesNotExactlyAtMidnight() {
        Date start = date(2020, 1, 10, 23, 0, 0);
        Date end = date(2020, 1, 11, 1, 0, 0);

        assertTrue(DateHelper.checkCrossesMidnight(start, end));
    }

    @Test
    void checkCrossesMidnight_falseWhenEndIsExactlyMidnight() {
        Date start = date(2020, 1, 10, 23, 0, 0);
        Date end = date(2020, 1, 11, 0, 0, 0);

        assertFalse(DateHelper.checkCrossesMidnight(start, end));
    }

    @Test
    void combineDateAndTime_shouldCombineWithoutExtraDay() {
        Date dateOnly = date(2020, 1, 10, 0, 0, 0);
        // time uses year 1900-01-01 -> getDaysToAdd = 0
        Date timeOnly = date(1900, 1, 1, 15, 30, 45);

        Date result = DateHelper.combineDateAndTime(dateOnly, timeOnly);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
    }

    @Test
    void combineDateAndTime_shouldAddOneDayWhenSpecial1900Jan2() {
        Date dateOnly = date(2020, 1, 10, 0, 0, 0);
        // time uses 1900-01-02 -> getDaysToAdd = 1
        Date timeOnly = date(1900, 1, 2, 9, 0, 0);

        Date result = DateHelper.combineDateAndTime(dateOnly, timeOnly);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(11, cal.get(Calendar.DAY_OF_MONTH)); // +1 day
        assertEquals(9, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    void combineDateAndTime_fromStrings_shouldReturnCombinedDate() throws ParseException {
        String dateString = "01/10/2020";
        String timeWithDateString = "01/01/1900 08:15:00";

        Date result = DateHelper.combineDateAndTime(
                dateString,
                timeWithDateString,
                DateHelper.US_DATE_FORMAT,
                DateHelper.REGULAR_TIME_FORMAT
        );

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(8, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(15, cal.get(Calendar.MINUTE));
    }

    @Test
    void fixCrossingMidnight_shouldAddDayWhenJobHoursNotPositive() {
        // End before start -> jobHours negative
        Date start = date(2020, 1, 11, 10, 0, 0);
        Date end = date(2020, 1, 10, 10, 0, 0);

        Date fixed = DateHelper.fixCrossingMidnight(start, end);

        Calendar cal = Calendar.getInstance();
        cal.setTime(fixed);

        assertEquals(11, cal.get(Calendar.DAY_OF_MONTH)); // original end + 1 day
    }

    @Test
    void fixCrossingMidnight_shouldReturnSameEndWhenJobHoursPositive() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date end = date(2020, 1, 10, 11, 0, 0);

        Date fixed = DateHelper.fixCrossingMidnight(start, end);

        assertEquals(end, fixed);
    }

    @Test
    void getDateDifference_withDates_shouldReturnPositiveDays() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date end = date(2020, 1, 15, 10, 0, 0);

        long diff = DateHelper.getDateDifference(start, end);

        assertEquals(5L, diff);
    }

    @Test
    void getDateDifference_withDates_shouldReturnNegativeDays() {
        Date start = date(2020, 1, 15, 10, 0, 0);
        Date end = date(2020, 1, 10, 10, 0, 0);

        long diff = DateHelper.getDateDifference(start, end);

        assertEquals(-5L, diff);
    }

    @Test
    void getDateDifference_withTimeStrings_shouldUseDefaultFormatWhenNull() throws ParseException {
        long diff = DateHelper.getDateDifference("10:00:00", "13:00:00", null);
        // 3 hours difference -> 0 full days between formatted ISO dates, so 0
        // (this mainly exercises the branch where timeFormat is null)
        assertEquals(0L, diff);
    }

    @Test
    void getDateFromString_shouldParseAccordingToFormat() throws ParseException {
        String dateStr = "2020-01-10";
        Date parsed = DateHelper.getDateFromString(dateStr, DateHelper.ISO_8601_DATE_FORMAT);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);

        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void getDateTimeFromStrings_shouldCombine() throws ParseException {
        String dateStr = "2020-01-10";
        String timeStr = " 12:30:45";
        Date dt = DateHelper.getDateTimeFromStrings(
                dateStr,
                timeStr,
                DateHelper.ISO_8601_DATE_FORMAT,
                " HH:mm:ss"
        );

        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
    }

    @Test
    void getFormattedDateString_shouldReturnFormattedDate() {
        Date d = date(2020, 1, 10, 16, 30, 0);
        String formatted = DateHelper.getFormattedDateString(d, DateHelper.US_DATE_FORMAT);

        assertEquals("01/10/2020", formatted);
    }

    @Test
    void getFormattedDateString_nullDateReturnsNull() {
        assertNull(DateHelper.getFormattedDateString(null, DateHelper.US_DATE_FORMAT));
    }

    @Test
    void getISO8601Date_shouldUseIsoFormat() {
        Date d = date(2020, 1, 10, 16, 30, 0);
        String iso = DateHelper.getISO8601Date(d);

        assertEquals("2020-01-10T16:30", iso);
    }

    @Test
    void getJobHours_withDates_shouldReturnHoursDifference() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date end = date(2020, 1, 10, 13, 30, 0);

        float hours = DateHelper.getJobHours(start, end);

        assertEquals(3.5f, hours, 0.0001f);
    }

    @Test
    void getJobHours_withNullDates_returnsZero() {
        assertEquals(0f, DateHelper.getJobHours(null, new Date()));
        assertEquals(0f, DateHelper.getJobHours(new Date(), null));
    }

    @Test
    void getJobHours_withStrings_shouldParseAndCompute() throws ParseException {
        float hours = DateHelper.getJobHours("10:00:00", "13:30:00", null);
        assertEquals(3.5f, hours, 0.0001f);
    }

    @Test
    void getJobTime_shouldReturnIsoStringWhenDatesNotNull() {
        Date sched = date(2020, 1, 10, 0, 0, 0);
        Date time = date(1900, 1, 1, 9, 15, 0);

        String jobTime = DateHelper.getJobTime(sched, time);

        assertEquals("2020-01-10T09:15", jobTime);
    }

    @Test
    void getJobTime_nullInputsReturnEmptyString() {
        assertEquals(DateHelper.EMPTY_STRING, DateHelper.getJobTime(null, new Date()));
        assertEquals(DateHelper.EMPTY_STRING, DateHelper.getJobTime(new Date(), null));
    }

    @Test
    void getMinutesAfterHour_shouldConvertDecimalHoursToMinutes() {
        BigDecimal duration = new BigDecimal("1.25"); // 1h 15m
        int minutes = DateHelper.getMinutesAfterHour(duration);

        assertEquals(75, minutes);
    }

    @Test
    void getSQLDate_shouldConvertUtilDateToSqlDate() {
        Date d = date(2020, 1, 10, 0, 0, 0);
        java.sql.Date sqlDate = DateHelper.getSQLDate(d);

        assertEquals(d.getTime(), sqlDate.getTime());
    }

    @Test
    void getSQLDate_fromStringShouldParseAndConvert() throws ParseException {
        java.sql.Date sqlDate = DateHelper.getSQLDate("01/10/2020", DateHelper.US_DATE_FORMAT);

        assertEquals("2020-01-10", sqlDate.toString());
    }

    @Test
    void getSQLDateString_shouldWrapInSqlLiteral() {
        Date d = date(2020, 1, 10, 0, 0, 0);
        String sqlStr = DateHelper.getSQLDateString(d);

        assertEquals("{d'2020-01-10'}", sqlStr);
    }

    @Test
    void getSQLDateFormatString_shouldReturnIsoFormat() {
        Date d = date(2020, 1, 10, 0, 0, 0);
        String sqlStr = DateHelper.getSQLDateFormatString(d);

        assertEquals("2020-01-10", sqlStr);
    }

    @Test
    void getSQLDateString_fromStringShouldParseAndFormat() throws ParseException {
        String sqlStr = DateHelper.getSQLDateString("01/10/2020", DateHelper.US_DATE_FORMAT);
        assertEquals("{d'2020-01-10'}", sqlStr);
    }

    @Test
    void getSQLTimeStamp_shouldConvertToTimestamp() {
        Date d = date(2020, 1, 10, 12, 0, 0);
        Timestamp ts = DateHelper.getSQLTimeStamp(d);

        assertEquals(d.getTime(), ts.getTime());
    }

    @Test
    void getWeekDay_withDate_shouldReturnCorrectConstant() {
        Date d = date(2020, 1, 5, 0, 0, 0); // 2020-01-05 was Sunday
        int day = DateHelper.getWeekDay(d);
        assertEquals(Calendar.SUNDAY, day);
    }

    @Test
    void getWeekDay_withString_shouldParseAndReturnCorrectConstant() throws ParseException {
        int day = DateHelper.getWeekDay("01/05/2020");
        assertEquals(Calendar.SUNDAY, day);
    }

    @Test
    void getYearDifference_shouldReturnPositiveYears() {
        Date start = date(2020, 1, 1, 0, 0, 0);
        Date end = date(2023, 1, 1, 0, 0, 0);

        long diff = DateHelper.getYearDifference(start, end);
        assertEquals(3L, diff);
    }

    @Test
    void getYearDifference_shouldReturnNegativeYears() {
        Date start = date(2023, 1, 1, 0, 0, 0);
        Date end = date(2020, 1, 1, 0, 0, 0);

        long diff = DateHelper.getYearDifference(start, end);
        assertEquals(-3L, diff);
    }

    @Test
    void midnight_shouldZeroTimeFields() {
        Date d = date(2020, 1, 10, 13, 45, 30);
        Date mid = DateHelper.midnight(d);

        Calendar cal = Calendar.getInstance();
        cal.setTime(mid);

        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void getCalendarInstance_shouldReturnZeroedTimeForToday() {
        Calendar cal = DateHelper.getCalendarInstance();
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test
    void daysBetween_singleArgumentShouldReturnDifferenceFromToday() {
        Calendar today = Calendar.getInstance();
        Calendar threeDaysAgo = (Calendar) today.clone();
        threeDaysAgo.add(Calendar.DAY_OF_MONTH, -3);

        int diff = DateHelper.daysBetween(threeDaysAgo);
        assertEquals(3, diff);
    }

    @Test
    void daysBetweenTwoDates_shouldReturnInclusiveDayCount() {
        Calendar start = Calendar.getInstance();
        start.clear();
        start.set(2020, Calendar.JANUARY, 1);

        Calendar end = Calendar.getInstance();
        end.clear();
        end.set(2020, Calendar.JANUARY, 3);

        int diff = DateHelper.daysBetweenTwoDates(start, end);
        assertEquals(3, diff); // 1,2,3
    }

    @Test
    void getLastDateOfCurrentWeek_shouldReturnSundayOfCurrentWeek() {
        Date d = DateHelper.getLastDateOfCurrentWeek();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    void getFormattedCurrentTimeString_shouldMatchTimePattern() {
        String time = DateHelper.getFormattedCurrentTimeString();
        assertTrue(time.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void getZeroAppendedString_shouldPadSingleDigit() {
        assertEquals("09", DateHelper.getZeroAppendedString(9));
        assertEquals("10", DateHelper.getZeroAppendedString(10));
    }

    @Test
    void compaireDateWithCurrentDate_shouldReturnFalseForFutureDateAndTrueForPast() {
        Date future = new Date(System.currentTimeMillis() + 60_000); // +1 minute
        Date past = new Date(System.currentTimeMillis() - 60_000);   // -1 minute

        assertFalse(DateHelper.compaireDateWithCurrentDate(future));
        assertTrue(DateHelper.compaireDateWithCurrentDate(past));
    }

    @Test
    void getCurrentDateWithDay_shouldMatchExpectedPattern() {
        String formatted = DateHelper.getCurrentDateWithDay();
        // E, dd MMM yyyy  e.g. "Tue, 30 Jun 2015"
        assertTrue(formatted.matches("^[A-Za-z]{3}, \\d{2} [A-Za-z]{3} \\d{4}$"));
    }

    @Test
    void getDateWithoutTime_shouldZeroTimeFields() {
        Date d = date(2020, 1, 10, 15, 30, 45);
        Date stripped = DateHelper.getDateWithoutTime(d);

        Calendar cal = Calendar.getInstance();
        cal.setTime(stripped);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void getTomorrowDate_shouldAddOneDay() {
        Date today = date(2020, 1, 10, 10, 0, 0);
        Date tomorrow = DateHelper.getTomorrowDate(today);

        Calendar cal = Calendar.getInstance();
        cal.setTime(tomorrow);
        assertEquals(11, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void getDateDifferenceInMins_shouldReturnMinutesComponent() {
        Date start = date(2020, 1, 10, 10, 0, 0);
        Date end = date(2020, 1, 10, 11, 15, 0); // 75 minutes difference

        Long mins = DateHelper.getDateDifferenceInMins(start, end);
        assertEquals(15L, mins); // 75 % 60 -> 15
    }

    @Test
    void getDateTime_shouldFollowExpectedPattern() {
        String dt = DateHelper.getDateTime();
        assertTrue(dt.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}"));
    }
}