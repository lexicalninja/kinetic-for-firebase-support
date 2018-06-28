package com.kinetic.fit.ui.settings;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.SensorScanner;
import com.kinetic.fit.connectivity.SensorScanner_;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EActivity(R.layout.activity_sensors)
public class SensorsActivity extends FitActivity implements SensorDataService.SensorDataObserver {

    private static final String TAG = "SensorsActivity";

    private SensorScanner.SensorScannerBinder mScanner;
    private ServiceConnection mScannerConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mScanner = (SensorScanner.SensorScannerBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mScanner = null;
        }
    };


    private SensorDataService.SensorDataServiceBinder mSensorsData;
    private ServiceConnection mSensorsDataConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorsData = (SensorDataService.SensorDataServiceBinder) service;
            mSensorsData.registerObserver(SensorsActivity.this);
            sensorsAdded(mSensorsData.getSensors());
            updateAssignments();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorsData = null;
        }
    };


    @ViewById(R.id.listview_sensors)
    ListView mSensorList;

    @ViewById(R.id.sensor_assigned_cadence)
    ImageView mCadenceImage;
    @ViewById(R.id.sensor_assigned_power)
    ImageView mPowerImage;
    @ViewById(R.id.sensor_assigned_speed)
    ImageView mSpeedImage;
    @ViewById(R.id.sensor_assigned_heart)
    ImageView mHeartImage;

    @ViewById(R.id.sensor_assigned_cadence_name)
    TextView mCadenceTextView;
    @ViewById(R.id.sensor_assigned_power_name)
    TextView mPowerTextView;
    @ViewById(R.id.sensor_assigned_speed_name)
    TextView mSpeedTextView;
    @ViewById(R.id.sensor_assigned_heart_name)
    TextView mHeartTextView;

    SensorListAdapter mSensorListAdapter;

    private ArrayList<Sensor> mSensors = new ArrayList<Sensor>();

    @Override
    @UiThread
    public void sensorAdded(Sensor sensor) {
        if (!mSensors.contains(sensor)) {
            Log.d(TAG, "Sensor Found: " + sensor.getName());
            mSensors.add(sensor);
            mSensorListAdapter.notifyDataSetChanged();
            mSensorList.invalidateViews();
        }
    }

    @UiThread
    public void sensorsAdded(Collection<Sensor> sensors) {
        for (Sensor sensor : sensors) {
            if (!mSensors.contains(sensor)) {
                Log.d(TAG, "Sensor Found: " + sensor.getName());
                mSensors.add(sensor);
            }
        }
        mSensorListAdapter.notifyDataSetChanged();
        mSensorList.invalidateViews();
    }

    @Override
    public void sensorAssignmentsChanged() {
        updateAssignments();
    }

    @UiThread
    protected void updateAssignments() {
        if (mSensorsData == null) {
            return;
        }
        if (mSensorsData.getCadenceSensor() == null) {
            mCadenceImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            mCadenceTextView.setText("Cadence");
        } else {
            mCadenceImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            mCadenceTextView.setText(mSensorsData.getCadenceSensor().getName());
        }
        if (mSensorsData.getSpeedSensor() == null) {
            mSpeedImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            mSpeedTextView.setText("Speed");
        } else {
            mSpeedImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            mSpeedTextView.setText(mSensorsData.getSpeedSensor().getName());
        }
        if (mSensorsData.getPowerSensor() == null) {
            mPowerImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            mPowerTextView.setText("Power");
        } else {
            mPowerImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            mPowerTextView.setText(mSensorsData.getPowerSensor().getName());
        }
        if (mSensorsData.getHeartRateSensor() == null) {
            mHeartImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            mHeartTextView.setText("Heart Rate");
        } else {
            mHeartImage.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            mHeartTextView.setText(mSensorsData.getHeartRateSensor().getName());
        }
    }

    @Override
    @UiThread
    public void sensorRemoved(Sensor sensor) {
        mSensors.remove(sensor);
        mSensorListAdapter.notifyDataSetChanged();
        mSensorList.invalidateViews();
    }

    @AfterViews
    void buildSensorListAdapter() {
        mSensorListAdapter = new SensorListAdapter(this, mSensors);
        mSensorList.setAdapter(mSensorListAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindService(SensorScanner_.intent(this).get(), mScannerConnection, Context.BIND_AUTO_CREATE);
        bindService(SensorDataService_.intent(this).get(), mSensorsDataConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mScannerConnection);
        unbindService(mSensorsDataConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensors.clear();
        if (mSensorsData != null) {
            sensorsAdded(mSensorsData.getSensors());
        }
        mSensorListAdapter.notifyDataSetChanged();
        mSensorList.invalidateViews();
    }

    private class SensorListAdapter extends BaseAdapter {

        Context context;
        List<Sensor> data;
        private LayoutInflater inflater = null;

        public SensorListAdapter(Context context, List<Sensor> data) {
            this.context = context;
            this.data = data;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SensorListItem view = (SensorListItem) convertView;
            if (view == null) {
                view = (SensorListItem) inflater.inflate(R.layout.list_item_sensor, parent, false);
            }
            Sensor sensor = data.get(position);
            view.setSensor(sensor, context, mSensorsData);
            return view;
        }
    }

    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_AVAILABLE, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    @UiThread
    protected void onFirmwareUpdate(final Intent intent) {
        Log.d(TAG, "Firmwareupdateavailable");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smart Control Update");
        builder.setMessage("There is a firmware update available for your Smart Control. Update Now?");
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent ni = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_START);
                ni.putExtra(SensorDataService.SENSOR_ID, intent.getStringExtra(SensorDataService.SENSOR_ID));
                sendBroadcast(ni);
            }
        });
        builder.show();
    }
}
