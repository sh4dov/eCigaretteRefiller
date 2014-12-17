package com.sh4dov.ecigaretterefiller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sh4dov.ecigaretterefiller.R;
import com.sh4dov.model.Refill;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by sh4dov on 2014-12-08.
 */
public class RefillsAdapter extends ArrayAdapter<Refill> {
    public RefillsAdapter(Context context, ArrayList<Refill> refills){
        super(context, 0, refills);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        Refill refill = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_refill_item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.refill_name);
        name.setText(refill.name);
        TextView size = (TextView) convertView.findViewById(R.id.refill_size);
        size.setText(new DecimalFormat("0.0").format(refill.size));
        TextView date = (TextView) convertView.findViewById(R.id.refill_date);
        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(refill.date));

        return convertView;
    }
}
