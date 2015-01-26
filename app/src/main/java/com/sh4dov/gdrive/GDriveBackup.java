package com.sh4dov.gdrive;

import android.app.Activity;
import android.app.ProgressDialog;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.sh4dov.common.ProgressIndicator;
import com.sh4dov.common.ProgressPointerIndicator;
import com.sh4dov.common.TaskScheduler;
import com.sh4dov.ecigaretterefiller.R;
import com.sh4dov.google.DriveService;
import com.sh4dov.google.builders.FileBuilder;
import com.sh4dov.google.listeners.FolderListener;
import com.sh4dov.google.listeners.GetFilesListener;
import com.sh4dov.google.listeners.OnFailedListener;
import com.sh4dov.google.listeners.UploadFileListener;
import com.sh4dov.google.utils.FileHelper;
import com.sh4dov.repositories.DbHandler;

import java.util.ArrayList;
import java.util.List;


public class GDriveBackup extends GDriveBase implements GetFilesListener {
    File backup;
    File backupRoot;
    File refillerBackup = FileBuilder.createNewFile().setTitle(CarCostGDriveConst.BACKUP_NAME).build();
    private Activity activity;

    public GDriveBackup(DriveService driveService, Activity activity, final int reconnectRequestCode) {
        super(driveService, activity, reconnectRequestCode);
        this.activity = activity;
        getProgressDialog().setCancelable(false);
    }

    public void backup(String accountName) {
        getDriveService()
                .setAccountName(accountName)
                .setApplicationName(CarCostGDriveConst.APPLICATION_NAME);
        backupOnGDrive();
    }

    @Override
    public void onGetFiles(List<File> files) {
        backupRoot = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_ROOT_FOLDER_NAME, FileHelper.ROOT_ID);
        if (backupRoot == null) {
            createBackupRootFolder();
            return;
        }

        backup = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_APP_FOLDER_NAME, backupRoot.getId());
        if (backup == null) {
            createBackupFolder();
            return;
        }

        File refillerBackup = FileHelper.firstOrDefault(files, CarCostGDriveConst.BACKUP_NAME, backup.getId());
        if (refillerBackup != null) {
            this.refillerBackup = refillerBackup;
        }

        setBackupParentReference();
    }

    private void backupOnGDrive() {
        getProgressDialog().show();
        getProgressDialog().setMessage("connecting...");
        getDriveService().getFiles("title contains 'backup'", this, this, this);
    }

    private void createBackup() {
        exportToGDrive(refillerBackup, CarCostGDriveConst.BACKUP_NAME, null);
    }

    private void createBackupFolder() {
        if (backup == null) {
            getProgressDialog().setMessage("creating folder " + CarCostGDriveConst.BACKUP_APP_FOLDER_NAME);
            backup = FileBuilder
                    .createNewFolder()
                    .setTitle(CarCostGDriveConst.BACKUP_APP_FOLDER_NAME)
                    .build();
            ParentReference parentReference = new ParentReference();
            parentReference.setId(backupRoot.getId());
            ArrayList<ParentReference> parents = new ArrayList<ParentReference>();
            parents.add(parentReference);
            backup.setParents(parents);
            getDriveService().uploadFolder(backup, new FolderListener() {
                @Override
                public void onUpdatedFolder(File file) {
                    backup = file;
                    setBackupParentReference();
                }
            }, this, this);
        } else {
            setBackupParentReference();
        }
    }

    private void createBackupRootFolder() {
        if (backupRoot == null) {
            getProgressDialog().setMessage("creating folder " + CarCostGDriveConst.BACKUP_ROOT_FOLDER_NAME);
            backupRoot = FileBuilder
                    .createNewFolder()
                    .setTitle(CarCostGDriveConst.BACKUP_ROOT_FOLDER_NAME)
                    .build();
            getDriveService().uploadFolder(backupRoot, new FolderListener() {
                @Override
                public void onUpdatedFolder(File file) {
                    backupRoot = file;
                    createBackupFolder();
                }
            }, this, this);
        } else {
            createBackupFolder();
        }
    }

    private void exportToGDrive(final File file, final String backupName, final Runnable next) {
        final String[] content = new String[1];
        final ProgressPointerIndicator pointer = new ProgressPointerIndicator();
        ProgressIndicator progressIndicator = new ProgressIndicator(activity, ProgressDialog.STYLE_HORIZONTAL, new TaskScheduler(activity)
                .willExecute(new Runnable() {
                    @Override
                    public void run() {
                        content[0] = new DbHandler(activity, getNotificator()).exportToString();
                    }
                })
                .willExecuteOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (content[0] != null) {
                            getProgressDialog().show();
                            getProgressDialog().setMessage("backup to " + backupName);
                            getDriveService().uploadFile(file, content[0].getBytes(), new UploadFileListener() {
                                @Override
                                public void onUploaded(File file) {
                                    getNotificator().showInfo(activity.getText(R.string.backup_created) + ": " + backupName);
                                    getProgressDialog().hide();

                                    if (next != null) {
                                        next.run();
                                    }
                                }

                                @Override
                                public void onProgress(File file, double v) {

                                }
                            }, new OnFailedListener() {
                                @Override
                                public void onFailed(Exception e) {
                                    getProgressDialog().hide();
                                    getNotificator().showInfo(e.getMessage());
                                }
                            }, null);
                        }
                    }
                }));
        pointer.setProgressPointer(progressIndicator);
        progressIndicator.execute();
    }

    private void setBackupParentReference() {
        ParentReference parentReference = new ParentReference();
        parentReference.setId(backup.getId());
        ArrayList<ParentReference> parentReferences = new ArrayList<ParentReference>();
        parentReferences.add(parentReference);
        refillerBackup.setParents(parentReferences);

        getProgressDialog().hide();

        createBackup();
    }
}
