package com.applications.philipp.apkinson.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

// import com.applications.philipp.apkinson.Plugins.PluginOption;
import com.applications.philipp.apkinson.database.ApkinsonSQLiteHelper;
import com.applications.philipp.apkinson.database.Call;
import com.applications.philipp.apkinson.Plugins.IApkinsonCallback;
import com.applications.philipp.apkinson.Plugins.IApkinsonPlugin;
import com.applications.philipp.apkinson.Plugins.ShortArray;
import com.applications.philipp.apkinson.Plugins.PluginConstants;
// import com.applications.philipp.apkinson.tools.Levenshtein;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
// import java.nio.ByteBuffer;
// import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
// import java.util.ArrayList;
import java.util.Date;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;

/*
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;

*/
/**
 * Created by Philipp on 20.05.2016.
 */
public class CallRecorder extends Thread {

    private Context context;
    private static final int SAMPLE_RATE_HZ = 16000;
    private static final int SAMPLE_RATE_8KHZ = 8000;
    private static final double CONVERSION = (double) SAMPLE_RATE_HZ / SAMPLE_RATE_8KHZ;
    private boolean outgoing;
    private boolean contacts;
    MediaRecorder recorder;
    private static Boolean recording;
    private static ApkinsonSQLiteHelper database;
    private String result = "";
    private IApkinsonPlugin mService = null;
    private IApkinsonCallback mCallback = getRemoteCallback();
    private String pathData;
    Call call = null;
    // ServiceConnection mServiceConnection;

    public CallRecorder(Context context, boolean outgoing, boolean contacts, String pathData) {
        this.context = context;
        this.outgoing = outgoing;
        this.contacts = contacts;
        this.pathData = pathData;
        database = new ApkinsonSQLiteHelper(context.getApplicationContext());
    }

    @Override
    public void run() {
        Log.e("CALLRECORDER","NEW INSTANCE");
        // recording = true;
        // Intent intent = new Intent(PluginConstants.ACTION_BIND);
        // PackageManager pm = context.getPackageManager();
        // String packageName = "";
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recording = true;
        startRecording();
    }
    private void startRecording()
    {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyyhhmmss");
        Date date = new Date();
        String format = simpleDateFormat.format(date);
        //String pathData = Environment.getExternalStorageDirectory() + File.separator + "AppSpeechData";
        Log.e("CALLRECORDER", "Path"+pathData);
        //File filePcm = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "testRecord" + format + ".pcm");
        File filePcm = new File(pathData, "Apkinson_" + format + ".pcm");
        //File filePcm = new File(Environment.getExternalStorageDirectory(), "testRecord" + format + ".pcm");
        //AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //audioManager.setMode(AudioManager.MODE_IN_CALL);
        int time = (int) System.currentTimeMillis();

        try {
            filePcm.createNewFile();

            Log.e("CALLRECORDER", "Creating PCM");

            OutputStream outputStream = new FileOutputStream(filePcm);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            minBufferSize *= CONVERSION;

            short[] audioData = new short[minBufferSize];
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            Log.e("CALLRECORDER", "Audiorecord Initialize");
            AcousticEchoCanceler canceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            NoiseSuppressor suppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            AutomaticGainControl gainControl = AutomaticGainControl.create(audioRecord.getAudioSessionId());

/*
            if (gainControl != null) {
                gainControl.setEnabled(true);
                Log.e("CALLRECORDER", "Set gainControl");
            }

            if (suppressor != null) {
                suppressor.setEnabled(true);
                Log.e("CALLRECORDER", "Set supressor");
            }

            if (canceler != null) {
                canceler.setEnabled(true);
                Log.e("CALLRECORDER", "Set canceler");
            }
*/
            audioRecord.startRecording();
            Log.e("CALLRECORDER", "Recording started");

            //int samplesIn20ms = SAMPLE_RATE_8HZ / 50;
            ShortArray shortArray=null;
            //mService.startInput(SAMPLE_RATE_8HZ);
            //File fileWav = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "testRecord" + format + ".wav");
            //pathData = pathData+File.separator+"WAV";
            File fileWav = new File(pathData, "Apkinson_" + format + ".wav");
            //File fileWav = new File(Environment.getExternalStorageDirectory(), "testRecord" + format + ".wav");
            fileWav.createNewFile();
            Log.e("CALLRECORDER", pathData);

            // Create Call instance to save in database
            call = new Call(date, contacts, outgoing, fileWav);
            // short[] audioData = new short[minBufferSize];

            while (recording) {
                //Log.e("CALLRECORDER", String.valueOf(recording));
                int numrec = audioRecord.read(audioData, 0, minBufferSize);
                try {
                    for (int i = 0; i < numrec; i++)
                        dataOutputStream.writeShort(audioData[i]);
                } catch (IOException e) {
                    Log.d("ERROR", e.getMessage());
                    e.printStackTrace();
                }//endException
            }
            Log.d("LOOP", "++++++++Loop was left now!++++++++");
            audioRecord.stop();
            audioRecord.release();
            dataOutputStream.close();
            //mService.stopInput();

            //audioManager.setMode(AudioManager.MODE_NORMAL);
            time = (int) (System.currentTimeMillis() - time);
            time /= 1000;
            call.setDurationSECONDS(time);
/*
            if (gainControl != null) {
                gainControl.release();
            }

            if (suppressor != null) {
                suppressor.release();
            }

            if (canceler != null) {
                canceler.release();
            }
*/
            WavFileWriter wavFileWriter = new WavFileWriter(SAMPLE_RATE_HZ, filePcm, fileWav, context, format);
            wavFileWriter.start();
        } catch (IOException e) {
            Log.d("ERROR", e.getMessage());
            e.printStackTrace();
        } //catch (RemoteException e) {
            //e.printStackTrace();
        //}


        // processSyllables(filePcm);
        //System.loadLibrary("pocketsphinx_jni");
        //processSphinx(filePcm);
    }
/*
    private void processSyllables(File filePcm) {
        while (result.equals("")){
            try {
                sleep(2000);
            }catch (InterruptedException e){
                Log.e("Error", e.getMessage());
            }
        }
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
        try {
            Map<String,Object> options = new HashMap<String,Object>();
            options.put("RecognizerName", "pataka");
            options.put("LanguageModel", 1);
            mService.startInputWithOptions(SAMPLE_RATE_8kHZ, options);
        } catch (RemoteException e) {
            Log.e("PLUGIN-ERROR", e.getMessage());
        }
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(filePcm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("ERROR","FILE READING ERROR");
        }
        byte[] b = new byte[4096];
        try {
            int nbytes;
            while ((nbytes = stream.read(b)) >= 0) {
                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);

                // Not needed on desktop but required on android
                bb.order(ByteOrder.LITTLE_ENDIAN);

                short[] s = new short[nbytes/2];
                bb.asShortBuffer().get(s);
                ShortArray sArray = new ShortArray(s);
                try {
                    mService.sendPCMData(sArray);
                } catch (RemoteException e) {
                    Log.e("Error", e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.e("ERROR","Error" + e.getMessage());
        }
        try {
            mService.stopInput();
        } catch (RemoteException e) {
            Log.e("Error", e.getMessage());
        }
        Log.d("Syllables", "Analysis finished.");
    }

    private void processSphinx(File pcm) {
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

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(pcm);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("ERROR","FILE READING ERROR");
        }
        decoderWord.startUtt();
        decoderSyllable.startUtt();

        Log.d("TEST", "----BEFORE EVALUATION----");
        byte[] b = new byte[4096];
        try {
            int nbytes;
            while ((nbytes = stream.read(b)) >= 0) {
                ByteBuffer bb = ByteBuffer.wrap(b, 0, nbytes);

                // Not needed on desktop but required on android
                bb.order(ByteOrder.LITTLE_ENDIAN);

                short[] s = new short[nbytes/2];
                bb.asShortBuffer().get(s);
                decoderWord.processRaw(s, nbytes/2, false, false);
                decoderSyllable.processRaw(s, nbytes/2, false, false);
            }
        } catch (IOException e) {
            Log.e("ERROR","Error" + e.getMessage());
        }
        decoderWord.endUtt();
        Log.d("TEST", "----AFTER EVALUATION----");
        decoderSyllable.endUtt();
        CharSequence resultWord = decoderWord.hyp().getHypstr();
        CharSequence resultSyllable = decoderSyllable.hyp().getHypstr();
        Log.d("TEST", "----BEFORE RESULT----");
        Log.d("RESULT_WORD", resultWord.toString());
        Log.d("TEST", "----AFTER RESULT----");
        Log.d("RESULT_SYLLABLE", resultSyllable.toString());
        String word = resultWord.toString();
        String syllable = resultSyllable.toString();
        word = word.replace("a","");
        word = word.replace(" ","");
        syllable = syllable.replace("a","");
        syllable = syllable.replace(" ","");
        double resultLevenshtein = Levenshtein.distance(word, syllable);
        double result = Math.round(resultLevenshtein / (word.length() / 3d) * 100d) / 100d;
        try {
            call.setCallRESULT(String.valueOf(result));
            database.addCall(call);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
*/
    private short[] resampleTo8kHz(short[] audioData) {
        short[] resampled = new short[(int) ((double) audioData.length / CONVERSION)];
        for (int i = 0; i < resampled.length; i++) {
            //Interpolation
            double indexAudioData = (double) i * CONVERSION;
            double index0 = Math.floor(indexAudioData);
            double index1 = Math.ceil(indexAudioData);
            short value = (short) ((index1 - indexAudioData) * audioData[(int) index0] + (indexAudioData - index0) * audioData[(int) index0]);
            resampled[i] = value;
        }
        return resampled;
    }

    /*
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
    */
    private IApkinsonCallback getRemoteCallback() {
        IApkinsonCallback apkinsonCallback = new IApkinsonCallback.Stub() {
            @Override
            public void onRegistered(String intentActionName, String bundleResultKey) throws RemoteException {
                Log.d("REGISTER", "Got registered to call " + intentActionName + " and use key " + bundleResultKey);
            }

            @Override
            public void onResult(String result) throws RemoteException {
                // Log.d("CALLBACK", "RESULT CALLBACK REACHED");
                mService.unregisterCallback(mCallback);
                setResult(result);
            }

            @Override
            public void onError(String errorMsg) throws RemoteException {
                mService.unregisterCallback(mCallback);
                Log.e("ERROR", errorMsg);
            }
        };
        return apkinsonCallback;
    }

    public void stopRecording() {
        // Log.d("RECORDING", "STOPPED!");
        recording = false;
        /*
        recorder.stop();
        recorder.release();
        recorder = null;
        */
    }

    private void setResult(String result) {
        this.result = result;
        Log.d("RESULT", result);
        String resultString = "";
        String[] subset = result.split(" ");
        for (String s : subset){
            if (s.contains("|")){
                break;
            }
            if (s.contains("@ZIFFER")){
                resultString += s.replace("@ZIFFER", " ");
            }
        }

        try {
            call.setCallRESULT(resultString);
            database.addCall(call);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
/*
        if (this.result.equals("")){
            this.result = "NO RESULT";
        }*/
    }
/*
    private void writeShort(final DataOutputStream output, final short value)
            throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }
    public void unbindServiceConnection(){
        try {
            context.unbindService(mServiceConnection);
        } catch (Exception e){
            Log.e("ERROR UNBINDING:", e.getMessage());
        }
    } */
}
