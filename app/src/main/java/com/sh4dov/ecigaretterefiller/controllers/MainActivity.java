package com.sh4dov.ecigaretterefiller.controllers;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.sh4dov.common.FragmentOperator;
import com.sh4dov.common.Notificator;
import com.sh4dov.common.ProgressIndicator;
import com.sh4dov.common.ProgressPointerIndicator;
import com.sh4dov.common.TaskScheduler;
import com.sh4dov.ecigaretterefiller.FileDialog;
import com.sh4dov.ecigaretterefiller.R;
import com.sh4dov.ecigaretterefiller.business.logic.AverageProvider;
import com.sh4dov.gdrive.GDriveBackup;
import com.sh4dov.gdrive.GDriveBase;
import com.sh4dov.gdrive.GDriveRestore;
import com.sh4dov.google.DriveService;
import com.sh4dov.model.Refill;
import com.sh4dov.repositories.DbHandler;
import com.sh4dov.repositories.RefillsRepository;

import java.io.File;


public class MainActivity extends Activity
        implements NewRefillFragment.RefillRepository, ItemFragment.ItemOperations, Notificator, FragmentOperator {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager viewPager;

    RefillsRepository db;
    FileDialog fileDialog;
    private GDriveBackup gDriveBackup;
    private GDriveRestore gDriveRestore;

    @Override
    public void goToFragment(int fragmentId) {
        viewPager.setCurrentItem(fragmentId);
    }

    @Override
    public void reload() {
        mSectionsPagerAdapter.notifyDataSetChanged();
    }


    @Override
    public void showInfo(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showInfo(int id) {
        Toast.makeText(this, this.getText(id), Toast.LENGTH_LONG).show();
    }

    private static class RequestCodes {
        public static final int EDIT = 1;
        public static final int Backup = 2;
        public static final int Restore = 3;
    }

    protected void onStop() {
        super.onStop();
        gDriveBackup.close();
        gDriveRestore.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileDialog = new FileDialog(this, new File(".."));
        fileDialog.setFileEndsWith(".csv");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                final File csvFile = file;
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int which) {
                        final ProgressPointerIndicator progressPointer = new ProgressPointerIndicator();
                        ProgressIndicator progressIndicator = new ProgressIndicator(MainActivity.this, ProgressDialog.STYLE_HORIZONTAL, new TaskScheduler(MainActivity.this)
                                .willExecute(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                db.clear();
                                                break;
                                        }

                                        db.importFrom(csvFile, progressPointer);
                                    }
                                })
                                .willExecuteOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSectionsPagerAdapter.notifyDataSetChanged();
                                        showInfo("Successfully imported from csv");
                                    }
                                }));
                        progressPointer.setProgressPointer(progressIndicator);
                        progressIndicator.execute();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Delete data from database?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
            }
        });

        db = new DbHandler(this, this);
        AverageProvider averageProvider = new AverageProvider(db);
        FragmentFactory fragmentFactory = new FragmentFactory(averageProvider);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), fragmentFactory);

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setCurrentItem(FragmentFactory.NewRefill);

        DriveService driveService = GDriveBase.createService(this);
        gDriveBackup = new GDriveBackup(driveService, this, RequestCodes.Backup);
        gDriveRestore = new GDriveRestore(driveService, this, RequestCodes.Restore, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;

            case R.id.action_import_csv:
                importFromCsv();
                return true;

            case R.id.action_export_csv:
                exportToCsv();
                return true;

            case R.id.action_exit:
                finish();
                return true;

            case R.id.action_backup_gdrive:
                choseAccount(RequestCodes.Backup);
                return true;

            case R.id.action_restore_gdrive:
                choseAccount(RequestCodes.Restore);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void choseAccount(int requestCode) {
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, true, null, null, null, null);
        startActivityForResult(intent, requestCode);
    }

    private void exportToCsv() {
        SaveFileDialog dialog = new SaveFileDialog();
        dialog.addFileListener(new SaveFileDialog.FileListener() {
            @Override
            public void fileSelected(File file) {
                if (db.exportToCsv(file)) {
                    Toast.makeText(MainActivity.this, R.string.successfully_exported, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.cannot_export, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setFileName("ecigaretterefiller.csv");
        dialog.show(getFragmentManager(), "SaveFileDialog");

    }

    private void importFromCsv() {
        fileDialog.showDialog();
    }

    @Override
    public void AddNew(Refill refill) {
        db.Add(refill);
        mSectionsPagerAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(FragmentFactory.Overview);
    }

    @Override
    public void EditRefill(Refill refill) {
        Intent intent = new Intent(this, EditRefillActivity.class);
        intent.putExtra(EditRefillActivity.EditRefillKey, refill);
        startActivityForResult(intent, RequestCodes.EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCodes.EDIT:
                mSectionsPagerAdapter.notifyDataSetChanged();
                break;

            case RequestCodes.Backup:
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    gDriveBackup.backup(accountName);
                }
                break;

            case RequestCodes.Restore:
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    gDriveRestore.restore(accountName);
                }
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private final FragmentFactory fragmentFactory;

        public SectionsPagerAdapter(FragmentManager fm, FragmentFactory fragmentFactory) {
            super(fm);
            this.fragmentFactory = fragmentFactory;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragmentFactory.create(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public int getItemPosition(Object object) {
            // Causes adapter to reload all Fragments when
            // notifyDataSetChanged is called
            return POSITION_NONE;
        }
    }

    public class FragmentFactory {
        private static final int Overview = 0;
        private static final int NewRefill = 1;
        private static final int RefillList = 2;
        private static final int MonthRefillsList = 3;

        private final AverageProvider averageProvider;

        public FragmentFactory(AverageProvider averageProvider) {
            this.averageProvider = averageProvider;
        }

        public Fragment create(int sectionNumber) {
            Fragment fragment;
            Bundle args = new Bundle();

            switch (sectionNumber) {
                case RefillList:
                    fragment = new ItemFragment();
                    fragment.setArguments(args);
                    return fragment;

                case NewRefill:
                    fragment = new NewRefillFragment();
                    Refill refill = db.getLastRefill();
                    if (refill != null) {
                        args.putDouble(NewRefillFragment.SizeKey, refill.size);
                        args.putString(NewRefillFragment.NameKey, refill.name);
                    }
                    fragment.setArguments(args);
                    return fragment;

                case MonthRefillsList:
                    fragment = new MonthRefillsFragment();
                    fragment.setArguments(args);
                    return fragment;

                default:
                case Overview:
                    fragment = new OverviewFragment();
                    AverageProvider.AverageData data = averageProvider.Get();
                    args.putDouble(OverviewFragment.AverageKey, data.Average);
                    args.putDouble(OverviewFragment.MonthAverageKey, data.MonthAverage);
                    args.putDouble(OverviewFragment.CurrentAverageKey, data.CurrentAverage);
                    args.putDouble(OverviewFragment.AllSizeKey, data.AllSize);
                    args.putDouble(OverviewFragment.MonthSizeKey, data.MonthSize);
                    args.putDouble(OverviewFragment.CurrentSizeKey, data.CurrentSize);
                    fragment.setArguments(args);
                    return fragment;
            }
        }

    }

}
