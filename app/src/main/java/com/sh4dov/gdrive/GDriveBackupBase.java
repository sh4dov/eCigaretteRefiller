package com.sh4dov.gdrive;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;

/**
 * Created by sh4dov on 2014-12-17.
 */
public abstract class GDriveBackupBase extends GDriveBase {
    protected String backupName = "ecigaretterefiller.backup.csv";

    public GDriveBackupBase(Activity activity, int resolveConnectionRequestCode) {
        super(activity, resolveConnectionRequestCode);
    }

    @Override
    protected GoogleApiClient.Builder setup(GoogleApiClient.Builder builder) {
        return builder
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER);
    }

    @Override
    public void onConnected(Bundle bundle){
        super.onConnected(bundle);
        Drive.DriveApi
                .getAppFolder(getGoogleApiClient())
                .listChildren(getGoogleApiClient())
                .setResultCallback(getChildrenRetrievedCallback());
    }

    protected abstract ResultCallback<DriveApi.MetadataBufferResult> getChildrenRetrievedCallback();
}
