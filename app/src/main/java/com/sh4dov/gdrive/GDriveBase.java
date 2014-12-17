package com.sh4dov.gdrive;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sh4dov.common.Notificator;

/**
 * Created by sh4dov on 2014-12-17.
 */
public abstract class GDriveBase
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final Activity activity;
    private final int resolveConnectionRequestCode;
    private final GoogleApiClient googleApiClient;
    private Notificator notificator;

    public GDriveBase(Activity activity, int resolveConnectionRequestCode, Notificator notificator){
        this.activity = activity;
        this.resolveConnectionRequestCode = resolveConnectionRequestCode;
        this.notificator = notificator;

        googleApiClient = setup(new GoogleApiClient
                .Builder(activity,this, this))
                .build();
    }

    protected abstract GoogleApiClient.Builder setup(GoogleApiClient.Builder builder);

    protected GoogleApiClient getGoogleApiClient() {return googleApiClient;}

    protected Activity getActivity(){return activity;}

    protected void showInfo(String message){
        if(notificator != null){
            notificator.showInfo(message);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, resolveConnectionRequestCode);
            } catch (IntentSender.SendIntentException e) {
                showInfo(e.getMessage());
            }
        } else {
            GooglePlayServicesUtil
                    .getErrorDialog(connectionResult.getErrorCode(), activity, 0)
                    .show();
        }
    }
}
