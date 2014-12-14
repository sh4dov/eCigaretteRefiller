package com.sh4dov.ecigaretterefiller;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity
implements NewRefillFragment.RefillRepository, ItemFragment.ItemOperations {

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
    ViewPager mViewPager;

    RefillsRepository db;
    FileDialog fileDialog;
    private static class RequestCodes{
        public static final int EDIT = 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        fileDialog = new FileDialog(this, new File(".."));
        fileDialog.setFileEndsWith(".csv");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener(){
            public void fileSelected(File file){
                final File csvFile = file;
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener(){
                    @Override
                public void onClick(DialogInterface dialogInterface,  int which){
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                db.clear();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                break;
                        }

                        db.importFromCsv(csvFile);
                        mSectionsPagerAdapter.notifyDataSetChanged();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Delete data from database?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
            }
        });

        db = new DbHandler(this);
        AverageProvider averageProvider = new AverageProvider(db);
        FragmentFactory fragmentFactory = new FragmentFactory(averageProvider);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), fragmentFactory, db);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(FragmentFactory.NewRefill);
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

        switch(id){
            case R.id.action_settings:
            return true;

            case R.id.action_import_csv:
                importFromCsv();
                return true;

            case R.id.action_exit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void importFromCsv(){
        fileDialog.showDialog();
    }

    @Override
    public void AddNew(Refill refill) {
        db.Add(refill);
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(FragmentFactory.Overview);
    }

    @Override
    public void EditRefill(Refill refill) {
        Intent intent = new Intent(this, EditRefillActivity.class);
        intent.putExtra(EditRefillActivity.EditRefillKey, refill);
        startActivityForResult(intent, RequestCodes.EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode)
        {
            case RequestCodes.EDIT:
                mSectionsPagerAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private final FragmentFactory fragmentFactory;
        private RefillsRepository db;

        public SectionsPagerAdapter(FragmentManager fm, FragmentFactory fragmentFactory, RefillsRepository db) {
            super(fm);
            this.fragmentFactory = fragmentFactory;
            this.db = db;
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

    public  class FragmentFactory {
        private static final int Overview = 0;
        private static final int NewRefill = 1;
        private static final int RefillList = 2;
        private static final int MonthRefillsList = 3;

        private final AverageProvider averageProvider;

        public FragmentFactory(AverageProvider averageProvider){
            this.averageProvider = averageProvider;
        }

        public Fragment create(int sectionNumber) {
            Fragment fragment;
            Bundle args = new Bundle();

            switch(sectionNumber){
                case RefillList:
                    fragment = new ItemFragment();
                    fragment.setArguments(args);
                    return fragment;

                case NewRefill:
                    fragment = new NewRefillFragment();
                    Refill refill = db.getLastRefill();
                    if(refill != null){
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
