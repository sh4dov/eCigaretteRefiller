package com.sh4dov.ecigaretterefiller;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by SYSTEM on 2014-12-06.
 */
public class OverviewFragment extends Fragment {
    public static final String AverageKey = "Average";
    public static final String AllSizeKey = "AllSize";
    public static final String MonthAverageKey = "MonthAverage";
    public static final String MonthSizeKey = "MonthSize";
    public static final String CurrentAverageKey = "CurrentAverage";
    public static final String CurrentSizeKey = "CurrentSize";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
        return rootView;
    }

    @Override
    public void onStart(){
        Bundle args = getArguments();
        if(args != null){
            double average = args.getDouble(AverageKey);
            double monthAverage = args.getDouble(MonthAverageKey);
            double currentAverage = args.getDouble(CurrentAverageKey);
            double allSize = args.getDouble(AllSizeKey);
            double monthSize = args.getDouble(MonthSizeKey);
            double currentSize = args.getDouble(CurrentSizeKey);

            TextView averageView = (TextView)getView().findViewById(R.id.average);
            averageView.setText(new DecimalFormat("#.##").format(average));
            TextView monthAverageView = (TextView)getView().findViewById(R.id.month_average);
            monthAverageView.setText(new DecimalFormat("#.##").format(monthAverage));
            TextView currentAverageView = (TextView)getView().findViewById(R.id.current_average);
            currentAverageView.setText(new DecimalFormat("#.##").format(currentAverage));
            TextView allSizeView = (TextView)getView().findViewById(R.id.all_size);
            allSizeView.setText(new DecimalFormat("0.0").format(allSize));
            TextView monthSizeView = (TextView)getView().findViewById(R.id.last_month_size);
            monthSizeView.setText(new DecimalFormat("0.0").format(monthSize));
            TextView currentSizeView = (TextView)getView().findViewById(R.id.current_size);
            currentSizeView.setText(new DecimalFormat("0.0").format(currentSize));
        }
        super.onStart();
    }
}
