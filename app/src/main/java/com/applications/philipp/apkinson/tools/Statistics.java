package com.applications.philipp.apkinson.tools;

import java.util.Arrays;

/**
 * Created by Philipp on 23.02.2017.
 */

public class Statistics {
    float[] data;
    int size;

    public Statistics(float[] data) {
        this.data = data;
        size = data.length;
    }

    public float getMean() {
        float sum = 0.0f;
        for (float a : data)
            sum += a;
        return sum / size;
    }

    public float getVariance() {
        float mean = getMean();
        float temp = 0;
        for (float a : data)
            temp += (a - mean) * (a - mean);
        return temp;
    }

    public float getStdDev() {
        return (float) Math.sqrt(getVariance());
    }

    public float median() {
        Arrays.sort(data);

        if (data.length % 2 == 0) {
            return ((data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0f);
        }
        return data[data.length / 2];
    }
}

