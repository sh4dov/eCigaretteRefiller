package com.sh4dov.ecigaretterefiller.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.sh4dov.ecigaretterefiller.R;
import com.sh4dov.model.Refill;
import com.sh4dov.repositories.DbHandler;
import com.sh4dov.repositories.RefillsRepository;

import java.util.Calendar;


public class EditRefillActivity extends Activity {
    public final static String EditRefillKey = "EditRefillKey";

    Refill editedRefill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_refill);
        final RefillsRepository db = new DbHandler(this, null);

        Intent intent = getIntent();
        editedRefill = (Refill)intent.getSerializableExtra(EditRefillKey);

        this.findViewById(R.id.edit_refill_save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Refill refill = new Refill();

                DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(editedRefill.date);
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                refill.date = calendar.getTime();

                NumberPicker numberPicker = (NumberPicker) findViewById(R.id.liquid_size_part1);
                int part1 = numberPicker.getValue();
                numberPicker = (NumberPicker) findViewById(R.id.liquid_size_part2);
                int part2 = numberPicker.getValue();
                refill.size = (double) part1 + ((double) part2 / 10);

                EditText editText = (EditText) findViewById(R.id.editText);
                refill.name = editText.getText().toString();

                if (refill.size == 0.0 || refill.name.isEmpty()) {
                    return;
                }

                refill.id = editedRefill.id;
                db.update(refill);
                setResult(RESULT_OK);
                finish();
            }
        });

        this.findViewById(R.id.edit_refill_delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete(editedRefill.id);
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        NumberPicker picker1 = (NumberPicker) findViewById(R.id.liquid_size_part1);
        picker1.setMaxValue(9);
        picker1.setMinValue(0);
        NumberPicker picker2 = (NumberPicker) findViewById(R.id.liquid_size_part2);
        picker2.setMaxValue(9);
        picker2.setMinValue(0);

        double size = editedRefill.size;
        int part1 = (int) Math.floor(size);
        int part2 = (int) Math.floor((size - part1) * 10);
        picker1.setValue(part1);
        picker2.setValue(part2);

        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText(editedRefill.name);
        editText.selectAll();

        DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(editedRefill.date);
        datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        super.onStart();
    }
}
