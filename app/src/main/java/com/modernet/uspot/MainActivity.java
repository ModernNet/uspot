package com.modernet.uspot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    private static final String TAG = "MainActivity";
    public Get get;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    ActionButton mButton;
    private ArrayList<ArrayList<InterestPoint>> mDataset;
    private Check checker;
    private String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(8);
        Log.d(TAG,"MainActivity.onCreate()");

        /* Custom font for title of ActionBar
        http://www.tristanwaddington.com/2013/03/styling-the-android-action-bar-with-a-custom-font/
         */

        //fusedLocationService = new FusedLocationService(this);
        checker = new Check(this);
        get = new Get(this);
        categories = getResources().getStringArray(R.array.categories);
        mDataset = new ArrayList<ArrayList<InterestPoint>>();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        mButton = (ActionButton) findViewById(R.id.fab_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"Click on Button",Toast.LENGTH_SHORT).show();
                Intent postActivity = new Intent(MainActivity.this,PostActivity.class);
                startActivity(postActivity);
            }
        });

        if (checker.isInternetAvailable()) {
            for(int i=0; i<categories.length; ++i) {
                mDataset.add(get.getCategoryDataset(categories[i]));
            }
            get.freeRAM();
            if(!checker.isLocationEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.location_request_title);
                builder.setMessage(R.string.location_request_message_start);
                builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent settings = new Intent("com.google.android.gms.location.settings.GOOGLE_LOCATION_SETTINGS");
                        startActivity(settings);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                builder.create().show();
            }
        }
        else {
            for(int i=0; i<categories.length; ++i)
                mDataset.add(new ArrayList<InterestPoint>());

            Toast.makeText(this,R.string.internet_not_available,Toast.LENGTH_SHORT).show();
            Log.e(TAG,"Network connection not available");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private static final String TAG = "FragmentPagerAdapter";
        FragmentManager fm;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        private TabFragment newTabFragment(ArrayList<InterestPoint> mDataCat, int position) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("Dataset",mDataCat);
            bundle.putInt("position", position);
            TabFragment fragment = new TabFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if(position>=0 && position< categories.length)
                return newTabFragment(mDataset.get(position),position);
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = getFragment(position);
            if (fragment != null) {
                Log.d(TAG, "Attaching item #" + position + ": f=" + fragment);
            }
            else {
                Log.d(TAG, "Adding item #" + position + ": f=" + fragment);
                // getItem()
            }

            return super.instantiateItem(container,position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fm.saveFragmentInstanceState((Fragment)object);
            Log.d(TAG, "Detaching item #" + position + ": f=" + object
                    + " v=" + ((Fragment)object).getView());
            super.destroyItem(container, position, object);
        }

        private String makeFragmentName(int viewId, int index) {
            return "android:switcher:" + viewId + ":" + index;
        }

        public Fragment getFragment(int position) {
            String tag = makeFragmentName(R.id.pager,position);
            return fm.findFragmentByTag(tag);
        }

        @Override
        public int getCount() {
            return categories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            if(position>=0 && position< categories.length)
                return categories[position].toUpperCase();
            return null;
        }
    }

    /*A implementar: Salvar dades

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putParcelableArrayList(TAG_DATASET,mDataset);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }*/
}
