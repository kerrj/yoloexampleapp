package com.example.justin.tensorflowtest;

import android.util.Log;

/**
 * Created by justin on 6/12/17.
 */

public class StatTimer {
    long time;

    public StatTimer(){
        time = System.nanoTime();
    }
    public void tic() {
        time = System.nanoTime();
    }

    public double toc(String message) {
        double elapsedms = (System.nanoTime() - time) / 1E6;
        Log.d("Elapsed Time", message + ": " + Double.toString(elapsedms));
        return elapsedms;
    }
}