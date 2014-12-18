package com.sh4dov.common;

import android.app.Activity;

import java.util.ArrayList;

/**
 * Created by sh4dov on 2014-12-18.
 */
public class TaskScheduler implements Runnable {
    private Activity activity;
    private ArrayList<RunnableStorage> storage = new ArrayList<RunnableStorage>();

    public TaskScheduler(Activity activity){
        this.activity = activity;
    }

    public TaskScheduler willExecute(Runnable runnable){
        storage.add(new RunnableStorage(runnable));
        return this;
    }

    public TaskScheduler willExecuteOnUiThread(Runnable runnable){
        storage.add(new RunnableStorage(runnable, true));
        return this;
    }

    @Override
    public void run() {
        for(RunnableStorage r: storage){
            if(r.runOnUIThread){
                activity.runOnUiThread(r.runnable);
            }
            else{
                r.runnable.run();
            }
        }
    }

    private class RunnableStorage{
        public RunnableStorage(Runnable runnable) {
            this(runnable, false);
        }

        public RunnableStorage(Runnable runnable, boolean runOnUIThread){
            this.runnable = runnable;
            this.runOnUIThread = runOnUIThread;
        }

        public boolean runOnUIThread;
        public Runnable runnable;
    }
}
