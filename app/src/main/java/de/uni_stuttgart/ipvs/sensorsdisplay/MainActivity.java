package de.uni_stuttgart.ipvs.sensorsdisplay;

import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.Intent;
import android.content.ComponentName;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static String TAG = MainActivity.class.getCanonicalName();
    private static final Handler handler = new Handler();
    private ISensorService sensorServiceProxy = null;
    private Context context = this;
    private int serviceTimeout;
    private TextView ambient_temp;
    private TextView pressure;
    private TextView illuminance;
    private TextView humidity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ambient_temp = (TextView)findViewById(R.id.ambient_temp);
        pressure = (TextView)findViewById(R.id.pressure);
        illuminance = (TextView)findViewById(R.id.illuminance);
        humidity = (TextView)findViewById(R.id.humidity);
        final TextView sensor_timeout = (TextView)findViewById(R.id.sensor_timeout);

        SeekBar timeout_selection = (SeekBar)findViewById(R.id.timeout_selection);
        serviceTimeout = timeout_selection.getProgress();
        sensor_timeout.setText(getString(R.string.sensor_timeout)+String.valueOf(serviceTimeout)+"s");

        // perform seek bar change listener event used for getting the progress value
        timeout_selection.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                int timeout = (10*progressChangedValue)/100;
                if (timeout == 0) {
                    timeout = 1;
                }
                serviceTimeout = timeout;
                sensor_timeout.setText(getString(R.string.sensor_timeout)+String.valueOf(timeout)+"s");
            }
        });

        Intent i = new Intent(this,SensorService.class);
        bindService(i,this, BIND_AUTO_CREATE);

        handler.postDelayed(textRunnable, serviceTimeout);
    }

    private final Runnable textRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(textRunnable, serviceTimeout*1000);
            String serviceMsg = "";
            if (sensorServiceProxy != null) {
                try {
                    serviceMsg = sensorServiceProxy.askMeasurement(Defines.AMBIENT_TEMP);
                    ambient_temp.setText(getString(R.string.ambient_temp)+" "+serviceMsg);

                    serviceMsg = sensorServiceProxy.askMeasurement(Defines.PRESSURE);
                    pressure.setText(getString(R.string.pressure)+" "+serviceMsg);

                    serviceMsg = sensorServiceProxy.askMeasurement(Defines.ILLUMINANCE);
                    illuminance.setText(getString(R.string.illuminance)+" "+serviceMsg);

                    serviceMsg = sensorServiceProxy.askMeasurement(Defines.AMBIENT_HUMIDITY);
                    humidity.setText(getString(R.string.ambient_humidity)+" "+serviceMsg);
                }
                catch (RemoteException ex) {
                    serviceMsg = "exception";
                    Log.i(TAG,serviceMsg);
                }
            }
            else
            {
                serviceMsg = "no service";
                Log.i(TAG,serviceMsg);
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.i(TAG,"Service connected");
        sensorServiceProxy = ISensorService.Stub.asInterface(iBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i(TAG,"Service disconnected");
        sensorServiceProxy = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorServiceProxy!=null) {
            unbindService(this);
        }
    }

}

