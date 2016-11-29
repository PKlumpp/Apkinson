package com.applications.philipp.apkinson.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Philipp on 18.05.2016.
 * <p/>
 * Listener for active calls.
 */
public class CallReceiver {
    private Context context;
    private TelephonyManager telephonyManager;
    private CallStateListener callStateListener;
    private static boolean outgoing = true;

    //Incoming Calls
    private class CallStateListener extends PhoneStateListener {
        private CallRecorder mCallRecorder = null;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.w("CSL", "ENTERED CSL WITH ID " + this.toString());
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    outgoing = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (incomingNumber.length() > 7) {
                        try {
                            mCallRecorder = new CallRecorder(context, outgoing, contactExists(context, incomingNumber));
                            outgoing = true;
                            mCallRecorder.start();
                        } catch (Exception e) {
                            Log.d("EXCEPTION", "Could not start recording!");
                            break;
                        }
                        Toast.makeText(context, "Detected!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d("IDLE", "RECORDER_STATE: " + mCallRecorder);
                    outgoing = true;
                    if (mCallRecorder != null) {
                        try {
                            mCallRecorder.stopRecording();
                            //mCallRecorder = null;
                        } catch (Exception e) {
                            Log.d("EXCEPTION", "Could not end recording!");
                            break;
                        }
                        Toast.makeText(context, "Successful!", Toast.LENGTH_LONG).show();

                    }
                    break;
            }
        }
    }


    public CallReceiver(Context context) {
        this.context = context;
        callStateListener = new CallStateListener();
    }

    public void start() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void stop() {
        Log.d("RECORDER STATE", "" + callStateListener.mCallRecorder);
        if (callStateListener.mCallRecorder != null) {
            Log.d("CONNECTION STATE", "" + callStateListener.mCallRecorder.mServiceConnection);
        }
        if (callStateListener.mCallRecorder != null) {
            callStateListener.mCallRecorder.stopRecording();
            callStateListener.mCallRecorder.unbindServiceConnection();
        }
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
    }

    private boolean contactExists(Context context, String number) {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
}
