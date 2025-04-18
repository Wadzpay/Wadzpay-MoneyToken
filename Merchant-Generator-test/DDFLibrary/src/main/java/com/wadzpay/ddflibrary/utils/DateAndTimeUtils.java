package com.wadzpay.ddflibrary.utils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateAndTimeUtils {

    /* get data and time */
    public static String getDateTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(cal.getTime());
    }
    /* get date */
    public static String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(cal.getTime());
    }
    /* get time */
    public static String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(cal.getTime());
    }
    /* get date time with format */
    public static Date getDateTimeFormat() {
        long millis=System.currentTimeMillis();
        Date sqlDate = new Date(millis);
//        Date currentDate = new Date();
        return sqlDate;
    }
}
