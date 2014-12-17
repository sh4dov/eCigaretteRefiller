package com.sh4dov.gdrive;

import android.app.Activity;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.sh4dov.common.Notificator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by sh4dov on 2014-12-17.
 */
public class GDriveBackup extends GDriveBackupBase {
    private String backup;
    public GDriveBackup(Activity activity, int resolveConnectionRequestCode) {
        super(activity, resolveConnectionRequestCode);
    }

    public void backup(String backup){
        this.backup = backup;
        connect();
    }

    @Override
    protected ResultCallback<DriveApi.MetadataBufferResult> getChildrenRetrievedCallback() {
        return childrenRetrievedCallback;
    }

    private com.google.android.gms.common.api.ResultCallback<com.google.android.gms.drive.DriveApi.MetadataBufferResult> childrenRetrievedCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {
            if(!result.getStatus().isSuccess()){
                onFail("Problem while connecting to GDrive.");
                return;
            }

            for (Metadata metadata : result.getMetadataBuffer()) {
                String title = metadata.getTitle();
                if(title.equals(backupName)){
                    Drive.DriveApi
                            .getFile(getGoogleApiClient(), metadata.getDriveId())
                            .open(getGoogleApiClient(), DriveFile.MODE_WRITE_ONLY, null)
                            .setResultCallback(updateBackup);
                    return;
                }
            }

            createNewBackup();
        }
    };

    private void createNewBackup() {
        Drive.DriveApi
                .newDriveContents(getGoogleApiClient())
                .setResultCallback(createNewBackup);
    }

    private com.google.android.gms.common.api.ResultCallback<com.google.android.gms.drive.DriveApi.DriveContentsResult> createNewBackup = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(backupName)
                    .setMimeType("text/plain")
                    .build();

            DriveContents driveContents = updateBackup(result);

            Drive.DriveApi
                .getAppFolder(getGoogleApiClient())
                .createFile(getGoogleApiClient(), changeSet, driveContents)
                .setResultCallback(createdBackup);
        }
    };

    private DriveContents updateBackup(DriveApi.DriveContentsResult result) {
        if(!result.getStatus().isSuccess()){
            onFail(result.getStatus().getStatusMessage());
            return null;
        }

        DriveContents driveContents = result.getDriveContents();
        ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
        FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
        Writer writer = new OutputStreamWriter(fileOutputStream);
        try {
            writer.write(backup);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            onFail(e.getMessage());
        }
        return driveContents;
    }

    private com.google.android.gms.common.api.ResultCallback<com.google.android.gms.drive.DriveFolder.DriveFileResult> createdBackup = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if(result.getStatus().isSuccess()){
                onSuccess("Backup created.");
            }
            else{
                onFail(result.getStatus().getStatusMessage());
            }
        }
    };

    public com.google.android.gms.common.api.ResultCallback<com.google.android.gms.common.api.Status> backupUpdates = new ResultCallback<Status>() {
        @Override
        public void onResult(Status status) {
            if(status.isSuccess()){
                onSuccess("Backup updated.");
            }
            else{
                onFail(status.getStatusMessage());
            }
        }
    };

    private com.google.android.gms.common.api.ResultCallback<com.google.android.gms.drive.DriveApi.DriveContentsResult> updateBackup = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            DriveContents driveContents = updateBackup(result);
            driveContents.commit(getGoogleApiClient(), null)
                    .setResultCallback(backupUpdates);
        }
    };
}
