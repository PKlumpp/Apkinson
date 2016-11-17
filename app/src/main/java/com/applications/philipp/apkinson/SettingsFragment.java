package com.applications.philipp.apkinson;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.applications.philipp.apkinson.services.TelephoneStateService;

import java.util.Locale;


/**
 * Created by Philipp on 17.05.2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        SharedPreferences appPreferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        findPreference("textfield_preference_client_ID").setSummary(appPreferences.getString("client_ID","1"));
        findPreference("checkbox_preference_telephone_recording").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean valueNow = Boolean.valueOf(newValue.toString());
                sharedPreferences.edit().putBoolean(preference.getKey(), valueNow);
                sharedPreferences.edit().apply();
                Intent intent = new Intent(getActivity().getApplicationContext(), TelephoneStateService.class);
                intent.addFlags(Service.START_STICKY);
                if (valueNow) {
                    getActivity().startService(intent);
                    Log.d("SUCCESS", "Service registered!");
                } else {
                    getActivity().stopService(intent);
                    Log.d("SUCCESS", "Service stopped!");
                }
                return true;
            }
        });

        findPreference("checkbox_preference_telephone_accel").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                return true;
            }
        });
        findPreference("list_preference_language").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (sharedPreferences.getString("list_preference_language", "").equals(o.toString())){
                    return true;
                }
                sharedPreferences.edit().putString(preference.getKey(), o.toString());
                sharedPreferences.edit().apply();
                Locale locale = new Locale(o.toString());
                Locale.setDefault(locale);
                Configuration config = getResources().getConfiguration();
                config.locale = locale;
                getActivity().getBaseContext().getResources().updateConfiguration(config,
                        getActivity().getBaseContext().getResources().getDisplayMetrics());
                getActivity().recreate();
                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }
}
