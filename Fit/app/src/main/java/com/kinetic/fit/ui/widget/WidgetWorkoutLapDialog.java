package com.kinetic.fit.ui.widget;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.hud.HUDpropertyViewMini;

import java.util.ArrayList;

/**
 * Created by Saxton on 4/6/16.
 */
public class WidgetWorkoutLapDialog extends Dialog implements SessionController.SessionControllerObserver {

    Context mContext;
    public TextView lapText;
    public TextView durationText;
    public TextView powerText;
    public HUDpropertyViewMini powerHUD;
    public HUDpropertyViewMini lapHUD;
    protected Profile profile;
    private long countdown;
    private long start;
    private long now;
    private long finish;
    private MyGestureDetector gestDetec;
    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    private ArrayList<HUDpropertyViewMini> mPropertyViews = new ArrayList<>();


    private SessionController.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController.SessionControllerBinder) service;
            registerObserver();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            dismiss();
            return false;
        }
    }

    public WidgetWorkoutLapDialog(Context context, WorkoutTextAndTime tat) {
        super(context);
        mContext = context;
        context.bindService(SessionController_.intent(context).get(), mSessionConnection, Context.BIND_AUTO_CREATE);
        setContentView(R.layout.dialog_workout_popup);
        initView();
        setView(tat);
        gestDetec = new MyGestureDetector();    //initial setup
        gestureDetector = new GestureDetector(getContext(), gestDetec);
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event))
            return true;
        else
            return false;
    }

    public void initView() {
        lapText = (TextView) findViewById(R.id.dialog_popup_text);
        durationText = (TextView) findViewById(R.id.dialog_popup_duration_text);
        powerText = (TextView) findViewById(R.id.dialog_popup_power_text);
        powerHUD = (HUDpropertyViewMini) findViewById(R.id.dialog_power_view);
        powerHUD.setProperty(FitProperty.Power);
        lapHUD = (HUDpropertyViewMini) findViewById(R.id.dialog_laps_view);
        lapHUD.setProperty(FitProperty.WorkoutIntervalDurationToGo);

        mPropertyViews.add(lapHUD);
        mPropertyViews.add(powerHUD);


    }

    public void setView(WorkoutTextAndTime tat) {
        lapText.setText(tat.getText());
        durationText.setText(tat.getDurationText());
        if (profile == null) {
            profile = Profile.current();
        }
        powerText.setText(tat.getPowerEnd() * profile.getPowerFTP() / 100 + " watts");
        countdown = (long) (tat.getCountdown() * 1000);
        start = System.currentTimeMillis();
        finish = start + countdown;
    }

    public void updateValues(SessionController.SessionControllerBinder sc) {
        for (HUDpropertyViewMini view : mPropertyViews) {
            view.updateValue(sc, null);
        }
    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {
    }

    @Override
    public void sessionTick(double timeDelta) {
        updateValues();
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {
    }


    public void updateValues() {
        powerHUD.updateValue(mSessionController, mSessionController.getSensorValues());
        now = System.currentTimeMillis();
        if (now < finish) {
//            Log.d("Something", "Countdown : " + (finish - now));
            long timer = ((finish - now) / 1000);
            lapHUD.changeTimer(timer);
        } else {
            lapHUD.changeTimer(0);
        }
    }

    @Override
    public void show() {
        super.show();
        final Handler handler = new Handler();
        final Runnable dismissRunnable = new Runnable() {
            @Override
            public void run() {
                if (isShowing()) {
                    dismiss();
                }
            }
        };
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(dismissRunnable);
            }
        });
        handler.postDelayed(dismissRunnable, 5000);
    }

    public void registerObserver() {
        mSessionController.registerObserver(this);
    }

    @Override
    public void onDetachedFromWindow() {
        this.dismiss();
        unbind();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onStop() {
        dismiss();
        super.onStop();
    }

    public void unbind() {

        if (mSessionController != null) {
            mSessionController.unregisterObserver(this);
            mContext.unbindService(mSessionConnection);
        }
    }
}

