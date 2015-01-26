package com.sh4dov.common;

import android.app.Activity;
import android.widget.Toast;

public class ToastNotificator implements Notificator {
    private Activity activity;

    public ToastNotificator(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void showInfo(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void showInfo(final int id) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, activity.getText(id), Toast.LENGTH_LONG).show();
            }
        });
    }
}
