package de.uni_stuttgart.ipvs.sensorsdisplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SensorService extends Service implements SensorEventListener {
    private static String TAG = SensorService.class.getCanonicalName();

    private SensorServiceImpl impl;
    private SensorManager sensorManager;
    private Sensor temp;
    private Sensor pressureSensor;
    private Sensor illuminance;
    private Sensor humidity;
    private float tempMeasured = 0;
    private float pressureMeasured = 0;
    private float lightMeasured = 0;
    private float humidityMeasured = 0;

    private class SensorServiceImpl extends ISensorService.Stub {
        @Override
        public String askMeasurement(int type) throws RemoteException {
            String retMsg = "";
            Log.i(TAG,"ask Measurement");

            switch (type) {
                case Defines.AMBIENT_TEMP: {
                    retMsg = String.valueOf(tempMeasured)+"°C";
                    break;
                }
                case Defines.PRESSURE: {
                    retMsg = String.valueOf(pressureMeasured)+"hPa";
                    break;
                }
                case Defines.ILLUMINANCE: {
                    retMsg = String.valueOf(lightMeasured)+"lx";
                    break;
                }
                case Defines.AMBIENT_HUMIDITY: {
                    retMsg = String.valueOf(humidityMeasured)+"%";
                    break;
                }
            }
            return retMsg;
        }

    }

    @Override
    public void onCreate() {
        Log.i(TAG,"Creating service");
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        temp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        illuminance = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        humidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);

        sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, illuminance, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, humidity, SensorManager.SENSOR_DELAY_NORMAL);

        impl = new SensorServiceImpl();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"Binding service");
        return impl;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Starting service");
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"Destroying service");
        super.onDestroy();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        Log.i(TAG,"Reading sensors");
        if (event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE) {
            tempMeasured = event.values[0];
        }
        else if (event.sensor.getType()==Sensor.TYPE_LIGHT) {
            lightMeasured = event.values[0];
        }
        else if (event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY) {
            humidityMeasured = event.values[0];
        }
        else if (event.sensor.getType()==Sensor.TYPE_PRESSURE) {
            pressureMeasured = event.values[0];
        }
        else {
            //nothing to do
        }

    }

}
