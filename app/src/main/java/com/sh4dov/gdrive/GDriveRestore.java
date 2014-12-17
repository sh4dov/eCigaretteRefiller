package com.sh4dov.gdrive;

import android.app.Activity;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;
import com.sh4dov.ecigaretterefiller.ListenerList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by sh4dov on 2014-12-17.
 */
public class GDriveRestore extends GDriveBackupBase {
    private ListenerList<RestoreListener> listeners = new ListenerList<RestoreListener>();

    public interface RestoreListener {
        public void RestoreFrom(String value);
    }

    public void addListener(RestoreListener listener){listeners.add(listener);}

    public GDriveRestore(Activity activity, int resolveConnectionRequestCode) {
        super(activity, resolveConnectionRequestCode);
    }

    public void restore(){
        connect();
    }

    private void onRestore(final String value){
        listeners.fireEvent(new ListenerList.FireHandler<RestoreListener>() {
            @Override
            public void fireEvent(RestoreListener listener) {
                listener.RestoreFrom(value);
            }
        });
    }

    @Override
    protected ResultCallback<DriveApi.MetadataBufferResult> getChildrenRetrievedCallback() {
        return childrenRetrievedCallback;
    }

    private com.google.android.gms.common.api.ResultCallback<com.google.android.gms.drive.DriveApi.DriveContentsResult> readBackup = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(DriveApi.DriveContentsResult result) {
            if(!result.getStatus().isSuccess()){
                onFail(result.getStatus().getStatusMessage());
                return;
            }

            DriveContents driveContents = result.getDriveContents();
            InputStream inputStream = driveContents.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder builder = new StringBuilder();
            try {
                while((line = reader.readLine()) != null){
                    builder.append(line + "\r\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String value = builder.toString();
            onRestore(value);
        }
    };
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
                            .open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(readBackup);
                    return;
                }
            }

            onFail("There is no backup!");
        }
    };
}
