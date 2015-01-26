package com.sh4dov.gdrive;

import android.app.Activity;
import android.app.ProgressDialog;

import com.sh4dov.common.Notificator;
import com.sh4dov.common.ToastNotificator;
import com.sh4dov.google.DriveService;
import com.sh4dov.google.listeners.OnFailedListener;
import com.sh4dov.google.listeners.UserRecoverableRequestCodeProvider;

public class GDriveBase implements UserRecoverableRequestCodeProvider, OnFailedListener {
    private final DriveService driveService;
    private final Notificator notificator;
    private final ProgressDialog progressDialog;
    private int reconnectRequestCode;

    public GDriveBase(DriveService driveService, Activity activity, int reconnectRequestCode) {
        notificator = new ToastNotificator(activity);
        this.reconnectRequestCode = reconnectRequestCode;
        progressDialog = new ProgressDialog(activity);
        this.driveService = driveService
                .setUserRecoverableRequestCodeProvider(this);
    }

    public static DriveService createService(Activity activity) {
        return new DriveService(activity)
                .addScope(DriveService.DRIVE);
    }

    public void close() {
        driveService.close();
        progressDialog.hide();
        progressDialog.dismiss();
    }

    @Override
    public int getRequestCode() {
        return reconnectRequestCode;
    }

    @Override
    public void onFailed(Exception e) {
        progressDialog.hide();
        notificator.showInfo(e.getMessage());
    }

    protected DriveService getDriveService() {
        return driveService;
    }

    protected Notificator getNotificator() {
        return notificator;
    }

    protected ProgressDialog getProgressDialog() {
        return progressDialog;
    }
}
