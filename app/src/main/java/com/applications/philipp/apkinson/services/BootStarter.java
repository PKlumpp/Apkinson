package com.applications.philipp.apkinson.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;


/**
 * Created by Philipp on 20.05.2016.
 */
public class BootStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Log.e("BootStarter", "Shared preferences "+String.valueOf(sharedPreferences));
        if (sharedPreferences.getBoolean("checkbox_preference_telephone_recording", false)) {
            Intent serviceIntent = new Intent(context, TelephoneStateService.class);
            Log.e("BootStarter", "Service intent");
            context.startService(serviceIntent);
        }
    }
}
