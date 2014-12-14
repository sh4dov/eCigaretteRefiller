package com.sh4dov.ecigaretterefiller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by sh4dov on 2014-12-14.
 */
public class MonthRefillsAdapter extends ArrayAdapter<MonthRefills> {
    public MonthRefillsAdapter(Context context, ArrayList<MonthRefills> refills){
        super(context, 0, refills);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        MonthRefills refill = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_month_refills, parent, false);
        }

        TextView month = (TextView)convertView.findViewById(R.id.monthName);
        String[] months = new DateFormatSymbols().getMonths();
        month.setText(new SimpleDateFormat("LLLL", Locale.getDefault()).format(refill.MonthId.getTime()));

        TextView year = (TextView)convertView.findViewById(R.id.year);
        year.setText(new SimpleDateFormat("yyyy").format(refill.MonthId.getTime()));

        TextView average = (TextView)convertView.findViewById(R.id.average);
        average.setText(new DecimalFormat("0.00").format(refill.getAverage()));

        TextView ml = (TextView)convertView.findViewById(R.id.size);
        ml.setText(new DecimalFormat("0.0").format(refill.getLiquidSum()));

        return convertView;
    }
}
