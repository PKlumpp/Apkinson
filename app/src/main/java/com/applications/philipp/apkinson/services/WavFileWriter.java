package com.applications.philipp.apkinson.services;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.applications.philipp.apkinson.database.Call;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Philipp on 18.08.2016.
 */
public class WavFileWriter extends Thread {
    private int SAMPLE_RATE;
    private File filePcm;
    private File fileWav;
    private String format;
    Context context;

    public WavFileWriter(int sampleRate, File filePcm, File fileWav, Context context, String format){
        SAMPLE_RATE = sampleRate;
        this.filePcm = filePcm;
        this.fileWav = fileWav;
        this.context = context;
        this.format = format;
    }

    @Override
    public void run(){
        byte[] rawData = new byte[(int) filePcm.length()];
        int counter = 0;
        DataInputStream input = null;
        while(input == null) {
            try {
                input = new DataInputStream(new FileInputStream(filePcm));
                input.read(rawData);
            } catch (IOException e1) {
                e1.printStackTrace();
                if (counter < 10) {
                    try {
                        sleep(1000);
                        counter++;
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    return;
                }
            }
        }
        if (input != null) {
            try {
                input.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(fileWav));
            // WAVE header
            // see
            // http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, SAMPLE_RATE); // sample rate
            writeInt(output, SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            //output.write(rawData);
        } catch (Exception e){
            e.printStackTrace();
        }
        short[] shorts = new short[rawData.length / 2];
        ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer().get(shorts);
        ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
        for (short s : shorts) {
            bytes.putShort(s);
        }
        try {
            output.write(bytes.array());
            filePcm.delete();
            if (output != null) {
                output.close();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        int samplesIn20ms = SAMPLE_RATE / 50;
        //rmsCalculator rmsCalculator = new rmsCalculator(fileWav, samplesIn20ms, context, format);
        //rmsCalculator.start();
    }

    private void writeInt(final DataOutputStream output, final int value)
            throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value)
            throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output,
                             final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}
