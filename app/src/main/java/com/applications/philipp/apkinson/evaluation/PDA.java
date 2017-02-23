package com.applications.philipp.apkinson.evaluation;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.applications.philipp.apkinson.tools.FFT;
import com.applications.philipp.apkinson.tools.Statistics;

/**
 * Created by Philipp on 22.02.2017.
 */
public class PDA extends Thread {
    private Context context;
    private Handler handler;
    private boolean recording = true;

    public PDA(Context context) {
        this.context = context;
        this.handler = null;
    }

    @Override
    public void run() {
        int minBufferSize = 0;
        int samplingRate = 0;
        for (int rate : new int[]{16000, 48000, 44100}) {
            minBufferSize = AudioRecord.getMinBufferSize(rate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (minBufferSize > 0) {
                samplingRate = rate;
                break;
            }
        }
        // Change buffer size to next power of two (FFT requirement)
        //minBufferSize = 32 - Integer.numberOfLeadingZeros(minBufferSize - 1);
        //minBufferSize = (int) Math.pow(2,minBufferSize);

        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
        recorder.startRecording();
        short[] audioData = new short[minBufferSize];
        FFT fft = new FFT(4096, samplingRate);

        while (recording) {
            int numberOfShort = recorder.read(audioData, 0, minBufferSize);
            if (numberOfShort == minBufferSize) {
                float[] floatAudio = shortToFloat(audioData);
                fft.forward(floatAudio);
                float[] imag = fft.getSpectrumImaginary();
                float[] real = fft.getSpectrumReal();
                float[] pcc = productOfComplexCunjugates(real, imag);
                imag = new float[real.length];
                float[] autoCorrelation = new float[4096];
                fft.inverse(pcc, imag, autoCorrelation);
                normalize(autoCorrelation, new Statistics(floatAudio).getVariance());
                //int startIndex = fft.freqToIndex(80);
                //int maxIndex = fft.freqToIndex(260);
                int maxCorrelation = findMaxIndex(10, autoCorrelation);

                if (maxCorrelation <= 4095) {
                    float pitchFreq = samplingRate / maxCorrelation;
                    Log.w("PDA", "Detected pitch frequency is " + pitchFreq);
                }
            }
        }
        recorder.stop();
        recorder.release();
    }

    private void normalize(float[] autoCorrelation, float variance) {
        for (int i = 0; i < autoCorrelation.length; i++) {
            autoCorrelation[i] /= variance;
        }
    }

    private int findMaxIndex(int startIndex, float[] autoCorrelation) {
        float maxValue = autoCorrelation[startIndex];
        int maxIndex = startIndex;
        for (int i = startIndex + 1; i < autoCorrelation.length / 2; i++) {
            if (autoCorrelation[i] > maxValue) {
                maxValue = autoCorrelation[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private float[] productOfComplexCunjugates(float[] real, float[] imag) {
        float[] array = new float[real.length];
        for (int i = 0; i < real.length; i++) {
            array[i] = (float) (Math.pow(real[i], 2) + Math.pow(imag[i], 2));
        }
        return array;
    }

    private float[] shortToFloat(short[] audioData) {
        float[] array = new float[4096];
        for (int i = 0; i < audioData.length; i++) {
            array[i] = audioData[i];
        }
        return array;
    }

    public void endRecording() {
        recording = false;
    }
}
