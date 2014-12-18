package com.sh4dov.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by sh4dov on 2014-12-17.
 */
public class ProgressIndicator extends AsyncTask<Void, Integer, Void> {
    private Context mContext;
    ProgressDialog mProgress;
    private int mProgressDialog=0;
    private Runnable job;

    public ProgressIndicator(Context context, int progressDialog, Runnable job){
        this.mContext = context;
        this.mProgressDialog = progressDialog;
        this.job = job;
    }

    @Override
    public void onPreExecute() {
        mProgress = new ProgressDialog(mContext);
        mProgress.setMessage("Please wait...");
        if (mProgressDialog==ProgressDialog.STYLE_HORIZONTAL){

            mProgress.setIndeterminate(false);
            mProgress.setMax(100);
            mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        mProgress.setCancelable(false);
        mProgress.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressDialog==ProgressDialog.STYLE_HORIZONTAL){
            mProgress.setProgress(values[0]);
        }
    }

    @Override
    protected Void doInBackground(Void... values) {
        try {
            job.run();

        } catch (Exception e) {
            e.printStackTrace();
        }

        mProgress.dismiss();

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mProgress.dismiss();
    }
}