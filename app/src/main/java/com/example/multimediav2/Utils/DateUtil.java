package com.example.multimediav2.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public Date date = new Date();

    public DateUtil() {
    }

    public DateUtil(Date date) {
        this.date = date;
    }

    public DateUtil(int year, int month, int day, int hour, int min, int sec) {

        int _month = 0;

        switch (month) {
            case 1:
                _month = Calendar.JANUARY;
                break;
            case 2:
                _month = Calendar.FEBRUARY;
                break;
            case 3:
                _month = Calendar.MARCH;
                break;
            case 4:
                _month = Calendar.APRIL;
                break;
            case 5:
                _month = Calendar.MAY;
                break;
            case 6:
                _month = Calendar.JUNE;
                break;
            case 7:
                _month = Calendar.JULY;
                break;
            case 8:
                _month = Calendar.AUGUST;
                break;
            case 9:
                _month = Calendar.SEPTEMBER;
                break;
            case 10:
                _month = Calendar.OCTOBER;
                break;
            case 11:
                _month = Calendar.NOVEMBER;
                break;
            case 12:
                _month = Calendar.DECEMBER;
                break;
        }

        date = new Date(year, _month, day, hour, min, sec);
    }

    public int Year() {
        return date.getYear();
    }

    public int Month() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        switch (cal.get(Calendar.MONTH)) {
            case Calendar.JANUARY:
                return 1;
            case Calendar.FEBRUARY:
                return 2;
            case Calendar.MARCH:
                return 3;
            case Calendar.APRIL:
                return 4;
            case Calendar.MAY:
                return 5;
            case Calendar.JUNE:
                return 6;
            case Calendar.JULY:
                return 7;
            case Calendar.AUGUST:
                return 8;
            case Calendar.SEPTEMBER:
                return 9;
            case Calendar.OCTOBER:
                return 10;
            case Calendar.NOVEMBER:
                return 11;
            default:
                return 12;
        }

    }

    public int Day() {
        return date.getDate();
    }

    static public DateUtil Now() {
        return new DateUtil();
    }

    public DateUtil AddDays(int days) {
        long temp = 1000L * 60 * 60 * 24 * days;
        Date next = new Date(this.date.getTime() + temp);
        return new DateUtil(next);
    }

    public DateUtil AddHours(int hours) {
        long temp = 1000L * 60 * 60 * hours;
        Date next = new Date(this.date.getTime() + temp);
        return new DateUtil(next);
    }

    public DateUtil AddMins(int min) {
        long temp = 1000L * 60 * min;
        Date next = new Date(this.date.getTime() + temp);
        return new DateUtil(next);
    }

    public TimeSpan Sub(DateUtil value) {
        return new TimeSpan(this.date.getTime() - value.date.getTime());
    }

    public int DayOfWeek() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return 1;
            case Calendar.TUESDAY:
                return 2;
            case Calendar.WEDNESDAY:
                return 3;
            case Calendar.THURSDAY:
                return 4;
            case Calendar.FRIDAY:
                return 5;
            case Calendar.SATURDAY:
                return 6;
            default:
                return 7;
        }
    }

    public boolean Between(DateUtil begin, DateUtil end) {

        long me = this.date.getTime();
        long b = begin.date.getTime();
        long e = end.date.getTime();

        return me > b && me < e;
    }

    static public DateUtil GetByHourMin(String hm) {

        String[] temp = hm.split(":");
        int hour = Integer.parseInt(temp[0]);
        int min = Integer.parseInt(temp[1]);

        DateUtil now = DateUtil.Now();

        return new DateUtil(now.Year(), now.Month(), now.Day(), hour, min, 0);
    }

    public class TimeSpan {
        private long ms;

        public TimeSpan(long ms) {
            this.ms = ms;
        }

        public long TotalSeconds() {
            return this.ms / 1000;
        }
    }

    public String ToString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = format.format(date);
        return str;
    }

}