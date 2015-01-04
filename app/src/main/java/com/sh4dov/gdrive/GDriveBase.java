package com.sh4dov.gdrive;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.sh4dov.common.Notificator;
import com.sh4dov.ecigaretterefiller.ListenerList;

/**
 * Created by sh4dov on 2014-12-17.
 */
public abstract class GDriveBase
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final Activity activity;
    private final int resolveConnectionRequestCode;
    private final GoogleApiClient googleApiClient;
    private ListenerList<GDriveListener> listeners = new ListenerList<GDriveListener>();

    public interface GDriveListener{
        void onSuccess(String message);
        void onFail(String message);
    }

    public void addListener(GDriveListener listener){listeners.add(listener);}

    protected void onSuccess(final String message){
        listeners.fireEvent(new ListenerList.FireHandler<GDriveListener>() {
            @Override
            public void fireEvent(GDriveListener listener) {
                listener.onSuccess(message);
            }
        });
    }

    protected void onFail(final String message){
        listeners.fireEvent(new ListenerList.FireHandler<GDriveListener>() {
            @Override
            public void fireEvent(GDriveListener listener) {
                listener.onFail(message);
            }
        });
    }

    public GDriveBase(Activity activity, int resolveConnectionRequestCode){
        this.activity = activity;
        this.resolveConnectionRequestCode = resolveConnectionRequestCode;

        googleApiClient = setup(new GoogleApiClient
                .Builder(activity,this, this))
                .build();
    }

    public void connect() {
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
        googleApiClient.reconnect();
    }

    public void clean(){
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }

        googleApiClient.unregisterConnectionFailedListener(this);
        googleApiClient.unregisterConnectionCallbacks(this);
    }

    protected abstract GoogleApiClient.Builder setup(GoogleApiClient.Builder builder);

    protected GoogleApiClient getGoogleApiClient() {return googleApiClient;}

    protected Activity getActivity(){return activity;}

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
                onFail(e.getMessage());
            }
        } else {
            GooglePlayServicesUtil
                    .getErrorDialog(connectionResult.getErrorCode(), activity, 0)
                    .show();
        }
    }
}
