package com.sh4dov.gdrive;

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
import com.sh4dov.common.Notificator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by sh4dov on 2014-12-16.
 */
public class GDriveWriteFile extends GDriveBase
implements ResultCallback<DriveApi.DriveContentsResult> {

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
            showInfo(e.getMessage());
        }

        IntentSender intentSender = Drive.DriveApi
                .newCreateFileActivityBuilder()
                .setInitialMetadata(metadataChangeSet)
                .setInitialDriveContents(driveContents)
                .build(getGoogleApiClient());

        try{
            getActivity().startIntentSenderForResult(intentSender, requestCodeCreator, null, 0, 0, 0);
        }
        catch(IntentSender.SendIntentException e){
            showInfo(e.getMessage());
        }
    }

    public void connect() {
        getGoogleApiClient().connect();
    }

    public GDriveWriteFile(Activity activity, int resolveConnectionRequestCode, Notificator notificator){
        super(activity, resolveConnectionRequestCode, notificator);
    }

    @Override
    protected GoogleApiClient.Builder setup(GoogleApiClient.Builder builder) {
        return builder
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE);
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
            .setResultCallback(this);
    }

    public void writeFile(int requestCodeCreator, String content){
        this.requestCodeCreator = requestCodeCreator;
        this.content = content;
        getGoogleApiClient().connect();
    }
}
