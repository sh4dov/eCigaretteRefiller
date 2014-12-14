package com.sh4dov.ecigaretterefiller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sh4dov on 2014-12-14.
 */
public class MonthRefillsProvider {
    private final RefillsRepository repository;

    public MonthRefillsProvider(RefillsRepository repository){
        this.repository = repository;
    }

    public ArrayList<MonthRefills> Get(){
        ArrayList<MonthRefills> result = new ArrayList<MonthRefills>();
        MonthRefills refills = null;
        Date endDate = new Date();
        Date startDate = new Date();

        for (Refill refill : repository.GetRefills()) {
            if(!isMatch(refills, refill.date)){
                if(refills != null){
                    refills.startDate = startDate;
                    refills.endDate = endDate;
                    endDate = startDate;
                    result.add(refills);
                }

                refills = new MonthRefills();
                refills.MonthId = generateId(refill.date);
            }
            refills.Refills.add(refill);
            startDate = refill.date;
        }

        return result;
    }

    private Calendar generateId(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.clear(Calendar.DAY_OF_MONTH);
        return calendar;
    }

    private boolean isMatch(MonthRefills refills, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return refills != null &&
                refills.MonthId != null &&
                refills.MonthId.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                refills.MonthId.get(Calendar.MONTH) == calendar.get(Calendar.MONTH);
    }
}
