package com.applications.philipp.apkinson.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Philipp on 18.05.2016.
 */
public class TelephoneStateService extends Service {
    private CallReceiver callReceiver;

    public TelephoneStateService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        callReceiver = new CallReceiver(this);
        callReceiver.start();

        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callReceiver.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
