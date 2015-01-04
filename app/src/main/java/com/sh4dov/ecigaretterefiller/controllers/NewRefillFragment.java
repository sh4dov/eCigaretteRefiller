package com.sh4dov.ecigaretterefiller.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.sh4dov.ecigaretterefiller.R;
import com.sh4dov.model.Refill;

import java.util.Calendar;

public class NewRefillFragment extends Fragment {
    public static final String SizeKey = "Size";
    public static final String NameKey = "Name";

    RefillRepository refillRepository;

    public interface RefillRepository {
        public void AddNew(Refill refill);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_refill, container, false);
        ((Button) view.findViewById(R.id.new_refill_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Refill refill = new Refill();

                DatePicker datePicker = (DatePicker) getView().findViewById(R.id.datePicker);
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                refill.date = calendar.getTime();

                NumberPicker numberPicker = (NumberPicker) getView().findViewById(R.id.liquid_size_part1);
                int part1 = numberPicker.getValue();
                numberPicker = (NumberPicker) getView().findViewById(R.id.liquid_size_part2);
                int part2 = numberPicker.getValue();
                refill.size = (double) part1 + ((double) part2 / 10);

                EditText editText = (EditText) getView().findViewById(R.id.editText);
                refill.name = editText.getText().toString();

                if (refill.size == 0.0 || refill.name.isEmpty()) {
                    return;
                }

                refillRepository.AddNew(refill);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        refillRepository = (RefillRepository) activity;
    }

    @Override
    public void onStart() {
        NumberPicker picker1 = (NumberPicker) getView().findViewById(R.id.liquid_size_part1);
        picker1.setMaxValue(9);
        picker1.setMinValue(0);
        NumberPicker picker2 = (NumberPicker) getView().findViewById(R.id.liquid_size_part2);
        picker2.setMaxValue(9);
        picker2.setMinValue(0);

        Bundle args = getArguments();
        if (args != null) {
            double size = args.getDouble(SizeKey);
            int part1 = (int) Math.floor(size);
            int part2 = (int) Math.floor(size * 10 - part1 * 10);
            picker1.setValue(part1);
            picker2.setValue(part2);

            String name = args.getString(NameKey);
            EditText editText = (EditText) getView().findViewById(R.id.editText);
            editText.setText(name);
            editText.selectAll();
        }

        super.onStart();
    }
}
