package com.example.restaurantinspection.model;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateManager {

    public static String formatDateInspection(Date inspectionDate){

        String result = "";

        Date dateToday = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inspectionDate);

        long dateDifference = TimeUnit.DAYS.convert(dateToday.getTime() - inspectionDate.getTime(), TimeUnit.MILLISECONDS);

        if(dateDifference < 30){
            result = Long.toString(dateDifference);
        } else if (dateDifference > 30 && dateDifference < 365){
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR);
        }

        return result;
    }
}
