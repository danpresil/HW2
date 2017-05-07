package com.example.dan_p.hw2.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Afeka on 1/8/2017.
 */
public class SensorService extends Service implements SensorEventListener {
    private static final String TAG =  SensorService.class.getSimpleName();;
    private boolean isFirstValue = true;
    private float[] firstAccelerometerValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] geomagneticValues = new float[3];
    private float azimuth = 0f;

    private Sensor accelerometerSensor;
    private Sensor geomagneticSensor;



    private long lastSensorChangeTime;

    ServiceBinder serviceBinder;
    private MyServiceListener listener;
    private SensorManager sensorManager;

    public interface MyServiceListener {
        void onSensorEvent(float[] values);
    }
    // To give access after the binding
    public class ServiceBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    public void setListener(MyServiceListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Step 2
        serviceBinder = new ServiceBinder();
        return serviceBinder;
    }

    // Step 5
    public void startListening() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Step 6
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, geomagneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Step 7.... 7... 7...
        synchronized (this) {
            if (listener != null) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    Log.d(TAG, "onSensorEvent TYPE_ACCELEROMETER: " + event.values[0] +
                            ", " + event.values[1] + ", " + event.values[2]);
                    System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
                    if (isFirstValue) {
                        lastSensorChangeTime = System.currentTimeMillis();
                        System.arraycopy(accelerometerValues, 0, firstAccelerometerValues, 0, 3);
                    }
                    isFirstValue = false;
                }

                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    Log.d(TAG, "onSensorEvent TYPE_MAGNETIC_FIELD: " + event.values[0] +
                            ", " + event.values[1] + ", " + event.values[2]);
                    System.arraycopy(event.values, 0, geomagneticValues, 0, 3);
                }

                float[] values = new float[4];
                // XYZ RELATIVE ORIENTATION values
                for (int i = 0; i < 3; i++)
                    values[i] = firstAccelerometerValues[i] - accelerometerValues[i];
                // SOURCE: https://github.com/iutinvg/compass/blob/master/app/src/main/java/com/sevencrayons/compass/Compass.java
                // ROTATION value
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, accelerometerValues,
                        geomagneticValues);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                    azimuth = azimuth % 360;
                    Log.d(TAG, "onSensorEvent ROTATION: " + azimuth);
                    values[3] = azimuth;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSensorChangeTime > 500) {
                    lastSensorChangeTime = currentTime;
                    listener.onSensorEvent(values);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}