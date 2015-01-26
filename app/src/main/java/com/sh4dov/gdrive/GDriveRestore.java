package com.sh4dov.gdrive;

import android.app.Activity;
import android.app.ProgressDialog;

import com.google.api.services.drive.model.File;
import com.sh4dov.common.FragmentOperator;
import com.sh4dov.common.ProgressIndicator;
import com.sh4dov.common.ProgressPointerIndicator;
import com.sh4dov.common.TaskScheduler;
import com.sh4dov.google.DriveService;
import com.sh4dov.google.builders.FileBuilder;
import com.sh4dov.google.listeners.DownloadFileListener;
import com.sh4dov.google.listeners.GetFilesListener;
import com.sh4dov.google.utils.FileHelper;
import com.sh4dov.repositories.DbHandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class GDriveRestore extends GDriveBase implements GetFilesListener {
    File refillerBackup = FileBuilder.createNewFile().setTitle(CarCostGDriveConst.BACKUP_NAME).build();
    private Activity activity;
    private FragmentOperator fragmentOperator;

    public GDriveRestore(DriveService driveService, Activity activity, int reconnectRequestCode, FragmentOperator fragmentOperator) {
        super(driveService, activity, reconnectRequestCode);
        this.activity = activity;
        this.fragmentOperator = fragmentOperator;
    }

    @Override
    public void onGetFiles(List<File> files) {
        File backupRoot = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_ROOT_FOLDER_NAME, FileHelper.ROOT_ID);
        if (backupRoot == null) {
            showThereIsNoBackup();
            return;
        }

        File backup = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_APP_FOLDER_NAME, backupRoot.getId());
        if (backup == null) {
            showThereIsNoBackup();
            return;
        }

        refillerBackup = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_NAME, backup.getId());

        getProgressDialog().hide();

        final Runnable reload = new Runnable() {
            @Override
            public void run() {
                getProgressDialog().hide();
                getNotificator().showInfo("Restored");
                fragmentOperator.reload();
            }
        };


        downloadFile(refillerBackup, CarCostGDriveConst.BACKUP_NAME, reload);
    }

    public void restore(String accountName) {
        getDriveService()
                .setAccountName(accountName)
                .setApplicationName(CarCostGDriveConst.APPLICATION_NAME);
        restoreFromGDrive();
    }

    private void downloadFile(File file, final String backupName, final Runnable next) {
        if (file == null) {
            if (next != null) {
                next.run();
            }
            return;
        }

        getProgressDialog().show();
        getProgressDialog().setMessage("Downloading " + file.getTitle());
        getDriveService().downloadFile(file, new DownloadFileListener() {
            @Override
            public void onDownloadedFile(File file, final byte[] bytes) {
                getProgressDialog().hide();
                restoreFromGDrive(new InputStreamReader(new ByteArrayInputStream(bytes)), backupName, next);
            }

            @Override
            public void onProgress(File file, double v) {
            }
        }, this, this);
    }

    private void restoreFromGDrive(final Reader reader, final String backupName, final Runnable next) {
        final ProgressPointerIndicator pointer = new ProgressPointerIndicator();
        ProgressIndicator progressIndicator = new ProgressIndicator(activity, ProgressDialog.STYLE_HORIZONTAL, new TaskScheduler(activity)
                .willExecute(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader bufferedReader = new BufferedReader(reader);

                        DbHandler dbHandler = new DbHandler(activity, getNotificator());
                        dbHandler.clear();
                        dbHandler.importFrom(bufferedReader, pointer);
                    }
                })
                .willExecuteOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getNotificator().showInfo("restored from " + backupName);
                        if (next != null) {
                            next.run();
                        }
                    }
                })
        );
        pointer.setProgressPointer(progressIndicator);
        progressIndicator.execute();
    }

    private void restoreFromGDrive() {
        getProgressDialog().show();
        getProgressDialog().setMessage("connecting...");
        getDriveService().getFiles("title contains 'backup'", this, this, this);
    }

    private void showThereIsNoBackup() {
        getNotificator().showInfo("There is no backup");
    }


}
