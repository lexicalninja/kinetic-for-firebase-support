package com.kinetic.fit.ui.hud;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.ui.video.VideoController;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;

@EFragment
public class HUDFragment extends Fragment implements SessionController.SessionControllerObserver {

    private static final String TAG = "HUDFragment";
    private HUDDataProvider dataProvider;
    private JSONArray hud;
    private boolean observeSession = true;
    private ArrayList<HUDPropertyView> mPropertyViews = new ArrayList<>();

    @Bean
    VideoController videoController;

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            if (observeSession) {
                mSessionController.registerObserver(HUDFragment.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    public static HUDFragment newInstance(JSONArray hud, boolean observeSession) {
        HUDFragment fragment = new HUDFragment_();
        fragment.hud = hud;
        fragment.observeSession = observeSession;
        fragment.setRetainInstance(false);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("hud", hud.toString());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {
                hud = new JSONArray(savedInstanceState.getString("hud"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        View view = createRow(inflater, hud, getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout.LayoutParams createLayoutParams(boolean horizontal) {
        if (horizontal) {
            return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        } else {
            return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        }
    }

    private LinearLayout createRow(LayoutInflater inflater, JSONArray properties, boolean horizontal) {
        LinearLayout row = new LinearLayout(getActivity());
        if (horizontal) {
            row.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            row.setOrientation(LinearLayout.VERTICAL);
        }
        for (int i = 0; i < properties.length(); i++) {
            int propertyId = properties.optInt(i, -1);
            if (propertyId > -1) {
                HUDPropertyView colView = (HUDPropertyView) inflater.inflate(R.layout.hud_property_view, row, false);
                FitProperty property = FitProperty.Cadence;
                if (propertyId < FitProperty.values().length) {
                    property = FitProperty.values()[propertyId];
                }
                // Set property on View ...
                colView.setProperty(property);

                row.addView(colView, createLayoutParams(horizontal));
                mPropertyViews.add(colView);
            } else {
                JSONArray propArray = properties.optJSONArray(i);
                if (propArray != null) {
                    View colView = createRow(inflater, propArray, !horizontal);
                    if (colView != null) {
                        row.addView(colView, createLayoutParams(horizontal));
                    }
                }
            }
        }
        return row;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().bindService(SessionController_.intent(getContext()).get(), mSessionConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            dataProvider = (HUDDataProvider) context;
            if (hud == null) {
                hud = dataProvider.getHudData();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement HUDDataProvider");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mSessionController != null && observeSession) {
            mSessionController.unregisterObserver(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSessionConnection != null) {
            getContext().unbindService(mSessionConnection);
        }
    }

    @UiThread
    @Receiver(actions = SensorDataService.SENSOR_DATA, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void onSensorData() {
        if (mSessionController == null) {
            updateValues();
        }
    }

    protected void updateValues() {
        if (mSessionController != null) {
            for (HUDPropertyView propertyView : mPropertyViews) {
                propertyView.updateValue(mSessionController, null);
            }
        } else {
            // get data from sensors ... not all property views can use this.
        }
    }

    @Override
    public void sessionTick(double timeDelta) {
        if (mSessionController != null) {
            updateValues();
        }
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {

    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {

    }

    public interface HUDDataProvider {
        JSONArray getHudData();
    }

}