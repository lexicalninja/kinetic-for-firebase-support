package com.kinetic.fit.ui.workout;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.objects.StandardHuds;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.hud.HUDFragment;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;

import java.util.Locale;

/**
 * Created by Saxton on 12/26/16.
 */

@EActivity(R.layout.activity_warmup)
public class WarmupActivity extends FitActivity implements SessionController.SessionControllerObserver, HUDFragment.HUDDataProvider {

    private static final String TAG = "WorkoutWarmupFrag";
    private Profile mProfile;

    @ViewById(R.id.workout_warmup_text_time)
    TextView timeRemainingText;
    @ViewById(R.id.workout_warmup_text_target)
    TextView targetText;
    @ViewById(R.id.workout_warmup_text_target_value)
    TextView targetValueText;
    @ViewById(R.id.button_left)
    FitButton mButtonLeft;
    @ViewById(R.id.button_middle)
    FitButton mButtonMiddle;
    @ViewById(R.id.button_right)
    FitButton mButtonRight;

    @Bean
    VideoController videoController;

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            mSessionController.startResumeWarmup();
            mSessionController.registerObserver(WarmupActivity.this);
            updateTargetValue(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    @AfterViews
    void afterCreateViews() {
        mButtonMiddle.setVisibility(View.GONE);
        mButtonLeft.setText("Reset");
        mButtonRight.setText("Skip");
        mButtonRight.setFitButtonStyle(FitButton.DEFAULT);
    }

    @Click(R.id.button_left)
    void onButtonReset() {
        mSessionController.restartWarmup();
    }

    @Click(R.id.button_right)
    void onButtonSkip() {
        workoutWarmupComplete();
    }

    private boolean targettingPower = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(SessionController_.intent(this).get(), mSessionConnection, BIND_AUTO_CREATE);
        mProfile = Profile.current();
        setTitle("Warmup");
    }

    private void updateTargetValue(boolean init) {
        if ((!targettingPower || init) && mSessionController.getSensorValues().currentPower >= 0) {
            targettingPower = true;
            int ftp = mProfile.getPowerFTP();
            if (ftp <= 0) {
                ftp = 200;
            }
            double goalLower = ftp * 0.3;
            double goalUpper = ftp * 0.7;
            targetText.setText("Target Power".toUpperCase());
            targetValueText.setText(String.format(Locale.getDefault(), "%.0f - %.0f", goalLower, goalUpper));
        } else if ((targettingPower || init) && mSessionController.getSensorValues().currentPower == -1) {
            targettingPower = false;
            int hrMax = mProfile.getHeartMax();
            int hrResting = mProfile.getHeartResting();
            if (hrMax <= 0) {
                hrMax = 190;
            }
            if (hrResting <= 0) {
                hrResting = 60;
            }
            int hrReserve = hrMax - hrResting;
            double targetHR = hrReserve * 0.65 + hrResting;
            targetText.setText("Target Heart Rate".toUpperCase());
            targetValueText.setText(String.format(Locale.getDefault(), "%.0f", targetHR));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSessionConnection != null) {
            unbindService(mSessionConnection);
        }
        if (mSessionController != null) {
            mSessionController.unregisterObserver(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void updateValues() {
        if (mSessionController.getDurations().warmupTimeRemaining > 0) {
            String timeString = ViewStyling.timeToStringMSF(mSessionController.getDurations().warmupTimeRemaining);
            timeRemainingText.setText(timeString);
            updateTargetValue(false);
        } else {
            workoutWarmupComplete();
        }
    }

    @Override
    public void sessionTick(double timeDelta) {
        updateValues();
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {

    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {

    }

    public void workoutWarmupComplete() {
        mSessionController.endWarmup();
        if (mSessionController.getPowerSensor() != null && mSessionController.getPowerSensor().requiresCalibration()) {
            CalibrateActivity_.intent(this).start();
            finish();
        } else {
            WorkoutActivity_.intent(this).start();
            finish();
        }
    }

    /***********************************************************************************************
     * All of the stuff below here will need to be offloaded or fixed, depending on what it is.
     * HUD Data will need to be fixed with personalized HUDs.
     * <p>
     * onBackPressed is used in this Activity and WorkoutActivity and should be moved somewhere it
     * can be useful and not duplicated?????
     **********************************************************************************************/
    @Override
    public JSONArray getHudData() {
        return StandardHuds.getSingleHudArray();

    }

    @Override
    public void onBackPressed() {
        if (mSessionController.getState() == null) {
            mSessionController.finishAndCleanup(TAG + "onBackPressed");
//            try {
//                mSessionController.getSession().unpin();
//                mSessionController.getSession().delete();
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            super.onBackPressed();
        } else {
            // alert!
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Workout?");
            builder.setMessage("Are you sure you want to cancel this workout?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                    mSessionController.finishAndCleanup(TAG + "onBackpressed2");
                    mSessionController.stopTimer();
                    mSessionController.deleteSession();
                    if (!videoController.videoIsNull()) {
                        videoController.setVideo(null);
                    }
                    RootActivity_.intent(WarmupActivity.this)
                            .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .start();
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {

                }
            });
            builder.show();
        }
    }
}
