/*
 * Arello Mobile
 * Mobile Framework
 * Except where otherwise noted, this work is licensed under a Creative Commons Attribution 3.0 Unported License
 * http://creativecommons.org/licenses/by/3.0
 */

package com.arellomobile.android.libs.system;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.logging.Logger;

/**
 * Simple class for detect shaking of device
 * @author Swift
 */
public class ShakeDetector implements SensorEventListener {
	private static final int FORCE_THRESHOLD = 700;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;

	private SensorManager mSensorMgr;
	private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
	private long mLastTime;
	private OnShakeListener mShakeListener;
	private Context mContext;
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;
	private final Logger log = Logger.getLogger(getClass().getName());

	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	public interface OnShakeListener {
		public void onShake();
	}

	public ShakeDetector(Context context) {
		mContext = context;
		resume();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		mShakeListener = listener;
	}

	public void resume() {
		mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}
		boolean supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		if (!supported) {
			mSensorMgr.unregisterListener(this, mSensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
			log.info("Accelerometer not supported");
		}
	}

	public void pause() {
		if (mSensorMgr != null) {
			mSensorMgr.unregisterListener(this, mSensorMgr.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER));
			mSensorMgr = null;
		}
	}

	public void onSensorChanged(SensorEvent sensorEvent) {
		int sensor = sensorEvent.sensor.getType();
		float[] values = sensorEvent.values;
		if (sensor != SensorManager.SENSOR_ACCELEROMETER) return;
		long now = System.currentTimeMillis();

		if ((now - mLastForce) > SHAKE_TIMEOUT) {
			mShakeCount = 0;
		}

		if ((now - mLastTime) > TIME_THRESHOLD) {
			long diff = now - mLastTime;
			log.config( "values[SensorManager.DATA_X] = " + values[SensorManager.DATA_X]);
			log.config( "values[SensorManager.DATA_Y] = " + values[SensorManager.DATA_Y]);
			log.config( "values[SensorManager.DATA_Z] = " + values[SensorManager.DATA_Z]);

			float speed = (Math.abs(values[SensorManager.DATA_X] - mLastX) + Math.abs(values[SensorManager.DATA_Y] - mLastY) + Math.abs(values[SensorManager.DATA_Z] - mLastZ)) / diff * 10000;
			if (speed > FORCE_THRESHOLD) {
				log.config( "speed = " + speed);
				if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
					mLastShake = now;
					mShakeCount = 0;
					if (mShakeListener != null) {
						mShakeListener.onShake();
					}
				}
				mLastForce = now;
			}
			mLastTime = now;

			mLastX = values[SensorManager.DATA_X];
			mLastY = values[SensorManager.DATA_Y];
			mLastZ = values[SensorManager.DATA_Z];
		}
	}

}
