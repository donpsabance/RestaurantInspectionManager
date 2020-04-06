package com.example.restaurantinspection.model;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Class for date format
 */

public class DateManager {

    public static Date dateCreate(String unformattedDate){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date inspectionDate = null;

        try {
            inspectionDate = simpleDateFormat.parse(unformattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  inspectionDate;
    }

    public static boolean check_ifOver_AYear(Date date){
        Date dateToday = new Date();
        long dateDifference = TimeUnit.DAYS.convert(dateToday.getTime() - date.getTime(), TimeUnit.MILLISECONDS);
        return dateDifference < 365;
    }

    public static String formatDateInspection(Date inspectionDate){

        String result;

        Date dateToday = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inspectionDate);

        long dateDifference = TimeUnit.DAYS.convert(dateToday.getTime() - inspectionDate.getTime(), TimeUnit.MILLISECONDS);

        if(dateDifference < 30){
            result = Long.toString(dateDifference) + " days ago.";
        } else if (dateDifference > 30 && dateDifference < 365){
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
        }

        return result;
    }
}
