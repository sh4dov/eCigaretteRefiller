package com.sh4dov.ecigaretterefiller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by sh4dov on 2014-12-14.
 */
public class MonthRefills {
    public ArrayList<Refill> Refills = new ArrayList<Refill>();
    public Calendar MonthId;
    public Date startDate;
    public Date endDate;

    public double getAverage(){
        return getLiquidSum() / getDays();
    }

    public double getLiquidSum(){
        double result = 0;

        for(Refill refill: Refills){
            result += refill.size;
        }

        return result;
    }

    private int getDays(){
        int days = 1;
        if(startDate == null || endDate == null){
            return days;
        }

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(startDate);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(endDate);
        calendar2.set(Calendar.HOUR_OF_DAY, 0);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);

        days = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(calendar2.getTimeInMillis() - calendar1.getTimeInMillis()));
        if (days <= 0) {
            days = 1;
        }

        return days;
    }
}
