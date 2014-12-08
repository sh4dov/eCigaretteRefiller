package com.sh4dov.ecigaretterefiller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by SYSTEM on 2014-12-06.
 */
public class AverageProvider {
    private DbHandler db;

    public AverageProvider(DbHandler db) {
        this.db = db;
    }

    public class AverageData{
        public double Average;
        public double AllSize;
        public double MonthAverage;
        public double MonthSize;
        public double CurrentAverage;
        public double CurrentSize;
    }

    private class Info{
        public double Average;
        public double Size;
    }

    public AverageData Get(){
        AverageData result = new AverageData();
        ArrayList<Refill> refills =  db.GetRefills();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        Date currentEnd = calendar.getTime();
        calendar.add(Calendar.MONTH, -1);
        Date currentStart = calendar.getTime();
        Date monthEnd = currentStart;
        calendar.add(Calendar.MONTH, -1);
        Date monthStart = calendar.getTime();

        Info info = getInfo(refills, null, null);
        result.Average = info.Average;
        result.AllSize = info.Size;

        info = getInfo(refills, monthStart, monthEnd);
        result.MonthAverage = info.Average;
        result.MonthSize = info.Size;

        info = getInfo(refills, currentStart, currentEnd);
        result.CurrentAverage = info.Average;
        result.CurrentSize = info.Size;

        return result;
    }

    private Info getInfo(ArrayList<Refill> refills, Date start, Date end) {
        double result = 0;
        Date now = new Date();
        Date max = end != null && end.before(now) ? end : now, min = start;

        for (int i = 0; i < refills.size(); i++) {
            Refill refill = refills.get(i);
            if (start != null && end != null && (refill.date.before(start) || refill.date.after(end))) {
                continue;
            }
            result += refill.size;
            if (min == null || refill.date.before(min)) {
                min = refill.date;
            }
        }

        int days = 1;
        if (min != null && max != null) {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(min);
            calendar1.set(Calendar.HOUR_OF_DAY, 0);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.set(Calendar.MILLISECOND, 0);

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(max);
            calendar2.set(Calendar.HOUR_OF_DAY, 0);
            calendar2.set(Calendar.MINUTE, 0);
            calendar2.set(Calendar.SECOND, 0);
            calendar2.set(Calendar.MILLISECOND, 0);

            days = (int)TimeUnit.MILLISECONDS.toDays(Math.abs(calendar2.getTimeInMillis() - calendar1.getTimeInMillis()));
            if (days <= 0) {
                days = 1;
            }
        }

        Info info = new Info();
        info.Average = result / days;
        info.Size = result;

        return info;
    }
}
