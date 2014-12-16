package com.sh4dov.ecigaretterefiller;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by sh4dov on 2014-12-16.
 */
public class GDriveController
implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<DriveApi.DriveContentsResult> {
    private final Activity activity;
    private int resolveConnectionRequestCode;
    private Notificator notificator;
    private final GoogleApiClient googleApiClient;
    private int mode;
    private int requestCodeCreator;
    private String content;

    @Override
    public void onResult(DriveApi.DriveContentsResult result) {
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setMimeType("text/html")
                .setTitle("ecigaretterefiller.csv")
                .build();

        DriveContents driveContents = result.getDriveContents();
        ParcelFileDescriptor parcelFileDescriptor =  driveContents.getParcelFileDescriptor();
        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
                .getFileDescriptor());
        Writer writer = new OutputStreamWriter(fileOutputStream);
        try {
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            notificator.showInfo(e.getMessage());
        }

        IntentSender intentSender = Drive.DriveApi
                .newCreateFileActivityBuilder()
                .setInitialMetadata(metadataChangeSet)
                .setInitialDriveContents(driveContents)
                .build(googleApiClient);

        try{
            activity.startIntentSenderForResult(intentSender, requestCodeCreator, null, 0, 0, 0);
        }
        catch(IntentSender.SendIntentException e){
            notificator.showInfo(e.getMessage());
        }
    }

    public void connect() {
        googleApiClient.connect();
    }

    private static class Mode{
        public static final int Read = 0;
        public static final int Write = 1;
    }

    public GDriveController(Activity activity, int resolveConnectionRequestCode, Notificator notificator){
        this.activity = activity;
        this.resolveConnectionRequestCode = resolveConnectionRequestCode;
        this.notificator = notificator;

        googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, resolveConnectionRequestCode);
            } catch (IntentSender.SendIntentException e) {
                notificator.showInfo(e.getMessage());
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch(mode){
            case Mode.Read:
                break;

            case Mode.Write:
                Drive.DriveApi.newDriveContents(googleApiClient)
                       .setResultCallback(this);
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void writeFile(int requestCodeCreator, String content){
        this.requestCodeCreator = requestCodeCreator;
        this.content = content;
        mode = Mode.Write;
        googleApiClient.connect();
    }
}
