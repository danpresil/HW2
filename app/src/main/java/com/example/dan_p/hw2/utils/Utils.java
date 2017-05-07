package com.example.dan_p.hw2.utils;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;


public class Utils {

    public static void shortVibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        if (v.hasVibrator())
            v.vibrate(100);
    }

    public static double generateRandom(int min , int max) {
        int range = (max - min) + 1;
        double v = (Math.random() * range) + min;
        return v;
    }
}
