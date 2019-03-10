package com.mega.megagps;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import static android.content.Context.SENSOR_SERVICE;

public class Compass implements SensorEventListener{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float prevAzimuthInDegrees;


    private Context mContext;
    int eventLauncher = 0;

    public Compass(Context context) {
        mContext = context;
        mSensorManager = (SensorManager)mContext.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void Resume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    public void Pause() {
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        eventLauncher = 0;
        //float alpha = 0.95f;

        if (sensorEvent.sensor == mAccelerometer) {
            System.arraycopy(sensorEvent.values, 0, mLastAccelerometer, 0, sensorEvent.values.length);
            mLastAccelerometerSet = true;
        } else if (sensorEvent.sensor == mMagnetometer) {
            System.arraycopy(sensorEvent.values, 0, mLastMagnetometer, 0, sensorEvent.values.length);
            mLastMagnetometerSet = true;
        }

        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            float azimuthInDegrees = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;

            if(prevAzimuthInDegrees - azimuthInDegrees > 180) {
                prevAzimuthInDegrees -= 360;
            }
            else if(azimuthInDegrees - prevAzimuthInDegrees > 180) {
                azimuthInDegrees -= 360;
            }

            prevAzimuthInDegrees = (prevAzimuthInDegrees * 9 + azimuthInDegrees) / 10;
            if(prevAzimuthInDegrees < 0) prevAzimuthInDegrees += 360;

            ((MainActivity)mContext).megaModel.setAngle(-prevAzimuthInDegrees);

            mLastAccelerometerSet = false;
            mLastMagnetometerSet = false;
        }

        ((MainActivity) mContext).Invalidate();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
