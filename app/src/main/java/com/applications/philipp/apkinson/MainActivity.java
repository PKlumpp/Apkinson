package com.applications.philipp.apkinson;

import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.applications.philipp.apkinson.services.RequestPermission;
import com.applications.philipp.apkinson.services.TelephoneStateService;

import java.io.File;
import java.util.Locale;

public class MainActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String pathData= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Create app folders to store data
        pathData = Environment.getExternalStorageDirectory() + File.separator + "AppSpeechData";
        File datafolder = new File(pathData);
        boolean checkF = datafolder.exists();
        if (checkF == false) {
            datafolder.mkdirs();
            datafolder = new File(pathData + File.separator + "Features");
            datafolder.mkdirs();
            datafolder = new File(pathData + File.separator + "WAV");
            datafolder.mkdirs();
            datafolder = new File(pathData + File.separator + "ACC");//Folder to save data from acceler
            datafolder.mkdirs();
        }
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setLanguage();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        //-----------------------------------------------------------------
        //Request permissions Android>=6
        RequestPermission rp = new RequestPermission();
        rp.RequestPermission(this);
        //-----------------------------------------------------------------
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("checkbox_preference_telephone_recording", false)){//||sharedPreferences.getBoolean("checkbox_preference_telephone_accel", false)) {
            if (!isMyServiceRunning(TelephoneStateService.class)) {
                Intent serviceIntent = new Intent(this, TelephoneStateService.class);
                serviceIntent.addFlags(Service.START_STICKY);
                this.startService(serviceIntent);
            }
        }
        //if (sharedPreferences.getBoolean("checkbox_telephone_accel", false)) {
        //}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getApplicationContext().getResources().updateConfiguration(newConfig,
                getApplicationContext().getResources().getDisplayMetrics());
    }

    private void setLanguage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPreferences.getString("list_preference_language", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onResume(){
        super.onResume();
        firstTimeStart();
    }

    private void firstTimeStart() {
        SharedPreferences appPreferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appPreferences.edit();
        Boolean launched_before = appPreferences.getBoolean("launched_before", false);
        Log.e("MainActivity", "Launched Before: "+String.valueOf(launched_before));
        if (!launched_before){
            Intent serviceIntent = new Intent(this, TelephoneStateService.class);
            serviceIntent.addFlags(Service.START_STICKY);
            this.startService(serviceIntent);
            editor.putBoolean("launched_before", true);
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            String client_ID = telephonyManager.getDeviceId();
            editor.putString("client_ID", client_ID);
            editor.apply();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_results, container, false);

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0){
                SettingsFragment settingsFragment = new SettingsFragment();
                return settingsFragment;
            }
            if (position == 1){
                ResultsFragment resultsFragment = new ResultsFragment();
                return resultsFragment;
            }
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.services);
                case 1:
                    return getString(R.string.results);
            }
            return null;
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
