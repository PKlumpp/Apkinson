package com.applications.philipp.apkinson.evaluation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.applications.philipp.apkinson.Plugins.IApkinsonCallback;
import com.applications.philipp.apkinson.Plugins.IApkinsonPlugin;
import com.applications.philipp.apkinson.Plugins.PluginConstants;
import com.applications.philipp.apkinson.Plugins.PluginOption;
import com.applications.philipp.apkinson.Plugins.ShortArray;
import com.applications.philipp.apkinson.SettingsFragment;
import com.applications.philipp.apkinson.database.ApkinsonSQLiteHelper;
import com.applications.philipp.apkinson.database.Call;
import com.applications.philipp.apkinson.tools.Levenshtein;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

/**
 * Created by Philipp on 29.11.2016.
 */
public class Evaluator extends Thread{
    Context context;
    Handler parent;
    private IApkinsonPlugin mService = null;
    private IApkinsonCallback mCallback = getRemoteCallback();
    ServiceConnection mServiceConnection;
    final int CONVERSION = 2;
    ApkinsonSQLiteHelper database;
    String resultSympalogWord = "empty";
    String resultSympalogSyllable = "empty";

    public Evaluator(Context context, Handler parent){
        this.context = context;
        this.parent = parent;
        database = new ApkinsonSQLiteHelper(context.getApplicationContext());
    }

    @Override
    public void run(){
        System.loadLibrary("pocketsphinx_jni");
        File directory = new File(String.valueOf(Environment.getExternalStorageDirectory()) + File.separator +  "Apkinson Files" + File.separator + "Evaluation");
        Log.d("EVALUATION", "Searching files in " + directory.toString());
        File[] files = directory.listFiles();
        Log.d("EVALUATION", "Found " + files.length + " files for evaluation");
        int counter = 0;
        for(File file : files){
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                Log.e("EVALUATION", e.getMessage());
                continue;
            }
            try {
                fileInputStream.skip(40);
                byte[] word = new byte[4];
                fileInputStream.read(word);
                String wordToString = new String(word, "US-ASCII");
                if (!wordToString.equals("data")){
                    Log.w("EVALUATION", "Data Chunk not found, instead: " + wordToString);
                }
            } catch (IOException e) {
                Log.e("EVALUATION", e.getMessage());
            }

            //setupSympalogWord();

            Config configWord = Decoder.defaultConfig();
            Config configSyllable = Decoder.defaultConfig();
            Assets assets = null;
            try {
                assets = new Assets(context);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ERROR","No Assets");
            }
            File assetsDir = null;
            try {
                assetsDir = assets.syncAssets();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ERROR","Assets are null");
            }
            configWord.setString("-hmm", new File(assetsDir, "en-us-ptm").getPath());
            configWord.setString("-dict", new File(assetsDir, "patakaWord.dict").getPath());
            configWord.setString("-lm", new File(assetsDir, "patakaWord_lm.dmp").getPath());

            configSyllable.setString("-hmm", new File(assetsDir, "en-us-ptm").getPath());
            configSyllable.setString("-dict", new File(assetsDir, "patakaSyllable.dict").getPath());
            configSyllable.setString("-lm", new File(assetsDir, "patakaSyllable_lm.dmp").getPath());

            Decoder decoderWord = new Decoder(configWord);
            Decoder decoderSyllable = new Decoder(configSyllable);

            decoderWord.startUtt();
            decoderSyllable.startUtt();

            byte[] data = new byte[4096];

            try {
                int nBytes;
                while ((nBytes = fileInputStream.read(data)) >= 0){
                    ByteBuffer bb = ByteBuffer.wrap(data, 0, nBytes);

                    // Not needed on desktop but required on android
                    bb.order(ByteOrder.LITTLE_ENDIAN);

                    short[] s = new short[nBytes/2];
                    bb.asShortBuffer().get(s);
                    decoderWord.processRaw(s, nBytes/2, false, false);
                    decoderSyllable.processRaw(s, nBytes/2, false, false);
                    //ShortArray sArray = new ShortArray(s);
                    /*
                    try {
                        mService.sendPCMData(sArray);
                    } catch (RemoteException e) {
                        Log.e("EVALUATION", e.getMessage());
                    }
                    */
                }
            }catch (Exception e){
                Log.e("EVALUATION", e.getMessage());
            }
            /*
            try {
                mService.stopInput();
            } catch (RemoteException e) {
                Log.e("EVALUATION", e.getMessage());
            }
            */
            decoderWord.endUtt();
            decoderSyllable.endUtt();
            CharSequence resultWord = decoderWord.hyp().getHypstr();
            CharSequence resultSyllable = decoderSyllable.hyp().getHypstr();
            Log.d("RESULT_WORD_SPHINX", resultWord.toString());
            Log.d("RESULT_SYLLABLE_SPHINX", resultSyllable.toString());
            String word = resultWord.toString();
            String syllable = resultSyllable.toString();
            Call sphinx = new Call(new Date(), false, false, file);
            word = word.replace("a","");
            word = word.replace(" ","");
            syllable = syllable.replace("a","");
            syllable = syllable.replace(" ","");
            double resultLevenshtein = Levenshtein.distance(word, syllable);
            double result = Math.round(resultLevenshtein / (word.length() / 3d) * 100d) / 100d;
            try {
                int pataka = word.length() - word.replace("p", "").length();
                sphinx.setCallRESULT("Sphinx: " + String.valueOf(result) + " Pataka: " + pataka);
                database.addCall(sphinx);
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            /*
            while(resultSympalogWord.equals("empty")){
                Log.d("EVALUATION", "Waiting for Sympalog Word Recognizer");
                try {
                    sleep(500);
                }catch (InterruptedException e){
                    Log.e("EVALUATION", e.getMessage());
                }
            }
            unbindServiceConnection();
            try {
                fileInputStream = new FileInputStream(file);
                fileInputStream.skip(44);
            } catch (IOException e) {
                Log.e("EVALUATION", e.getMessage());
            }
            setupSympalogSyllable();

            try {
                int nBytes;
                while ((nBytes = fileInputStream.read(data)) >= 0){
                    ByteBuffer bb = ByteBuffer.wrap(data, 0, nBytes);

                    // Not needed on desktop but required on android
                    bb.order(ByteOrder.LITTLE_ENDIAN);

                    short[] s = new short[nBytes/2];
                    bb.asShortBuffer().get(s);
                    ShortArray sArray = new ShortArray(s);
                    try {
                        mService.sendPCMData(sArray);
                    } catch (RemoteException e) {
                        Log.e("EVALUATION", e.getMessage());
                    }
                }
            }catch (Exception e){
                Log.e("EVALUATION", e.getMessage());
            }
            try {
                mService.stopInput();
            } catch (RemoteException e) {
                Log.e("EVALUATION", e.getMessage());
            }
            while(resultSympalogSyllable.equals("empty")){
                Log.d("EVALUATION", "Waiting for Sympalog Syllable Recognizer");
                try {
                    sleep(500);
                }catch (InterruptedException e){
                    Log.e("EVALUATION", e.getMessage());
                }
            }
            Log.d("RESULT_WORD_SYMPA", resultSympalogWord);
            Log.d("RESULT_SYLLABLE_SYMPA", resultSympalogSyllable);
            word = resultSympalogWord;
            syllable = resultSympalogSyllable;
            Call sympalog = new Call(new Date(), true, true, file);
            word = word.replace("a","");
            word = word.replace("P", "p");
            word = word.replace(" ","");
            syllable = syllable.replace("a","");
            syllable = syllable.replace("@KORREKT", "");
            syllable = syllable.replace("@FALSCH", "");
            syllable = syllable.replace(" ","");
            resultLevenshtein = Levenshtein.distance(word, syllable);
            result = Math.round(resultLevenshtein / (word.length() / 3d) * 100d) / 100d;
            try {
                int pataka = word.length() - word.replace("p", "").length();
                sympalog.setCallRESULT("Sympa: " + String.valueOf(result) + " Pataka: " + pataka);
                database.addCall(sympalog);
            } catch (NullPointerException e){
                e.printStackTrace();
            }

            resultSympalogSyllable = "empty";
            resultSympalogWord = "empty";
            unbindServiceConnection();
            */
            Message message = parent.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("update", "Processed " + ++counter + " files. " + (files.length-counter) + " remaining.");
            message.setData(bundle);
            message.sendToTarget();
        }
    }

    private void setupSympalogSyllable() {
        Intent intent = new Intent(PluginConstants.ACTION_BIND);
        PackageManager pm = context.getPackageManager();
        String packageName = "";
        try {
            List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
            packageName = services.get(0).serviceInfo.packageName;
        } catch (NullPointerException e) {
            Log.e("ERROR", "NO MATCHING SERVICE FOUND");
            return;
        }
        intent.setPackage(packageName);
        mServiceConnection = getServiceConnection();
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(intent);

        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Map<String,Object> options = new HashMap<String,Object>();
            options.put("RecognizerName", "pataka");
            options.put("LanguageModel", 1);
            List<PluginOption> optionsList = mService.getOptions();
            mService.startInputWithOptions(16000, options);
            //mService.startInput(SAMPLE_RATE_8kHZ);
        } catch (RemoteException e) {
            Log.e("PLUGIN-ERROR", e.getMessage());
        }
    }

    private void setupSympalogWord() {
        Intent intent = new Intent(PluginConstants.ACTION_BIND);
        PackageManager pm = context.getPackageManager();
        String packageName = "";
        try {
            List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
            packageName = services.get(0).serviceInfo.packageName;
        } catch (NullPointerException e) {
            Log.e("ERROR", "NO MATCHING SERVICE FOUND");
            return;
        }
        intent.setPackage(packageName);
        mServiceConnection = getServiceConnection();
        context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        context.startService(intent);

        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Map<String,Object> options = new HashMap<String,Object>();
            options.put("RecognizerName", "pataka");
            options.put("LanguageModel", 0);
            List<PluginOption> optionsList = mService.getOptions();
            mService.startInputWithOptions(16000, options);
            //mService.startInput(SAMPLE_RATE_8kHZ);
        } catch (RemoteException e) {
            Log.e("PLUGIN-ERROR", e.getMessage());
        }
    }

    private ServiceConnection getServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IApkinsonPlugin.Stub.asInterface(service);
                try {
                    mService.registerCallback(mCallback);
                } catch (RemoteException e) {
                    Log.e("ERROR", e.getMessage());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                Log.d("SERVICE", "Service disconnected");
            }
        };
    }

    private IApkinsonCallback getRemoteCallback() {
        IApkinsonCallback apkinsonCallback = new IApkinsonCallback.Stub() {
            @Override
            public void onRegistered(String intentActionName, String bundleResultKey) throws RemoteException {
                Log.d("REGISTER", "Got registered to call " + intentActionName + " and use key " + bundleResultKey);
            }

            @Override
            public void onResult(String result) throws RemoteException {
                Log.d("CALLBACK", "RESULT CALLBACK REACHED");
                mService.unregisterCallback(mCallback);
                if (resultSympalogWord.equals("empty")) {
                    resultSympalogWord = result;
                } else {
                    resultSympalogSyllable = result;
                }
            }

            @Override
            public void onError(String errorMsg) throws RemoteException {
                mService.unregisterCallback(mCallback);
                Log.e("ERROR", errorMsg);
            }
        };
        return apkinsonCallback;
    }

    private void unbindServiceConnection(){
        try {
            context.unbindService(mServiceConnection);
        } catch (Exception e){
            Log.w("ERROR UNBINDING:", e.getMessage());
        }
    }
}
