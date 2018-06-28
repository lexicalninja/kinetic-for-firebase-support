package com.kinetic.fit.ui.workout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.kinetic.fit.R;
import com.kinetic.fit.cast.FitCastService;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.objects.StandardHuds;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.analysis.AnalysisActivity;
import com.kinetic.fit.ui.hud.HUDFragment;
import com.kinetic.fit.ui.hud.HUDPagerFragment;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.settings.SettingsActivity;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.FitLinearLayout;
import com.kinetic.fit.ui.widget.WidgetWorkoutLapDialog;
import com.kinetic.fit.ui.widget.WorkoutGraphView;
import com.kinetic.fit.util.FITAudio;
import com.kinetic.fit.util.FitAnalytics;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;

import java.util.ArrayList;

import io.realm.Realm;

@EActivity(R.layout.activity_workout)
public class WorkoutActivity extends FitActivity implements
        HUDPagerFragment.HUDPagerDataProvider,
        HUDFragment.HUDDataProvider, SessionController.SessionControllerObserver {

    private static final String TAG = "WorkoutActivity";
    private static final String TAG_WORKER_FRAGMENT = "WorkerFragment";

    private WorkoutWorkerFragment workerFragment;

    boolean hidePopUps = false;
    boolean voiceOver = false;
    boolean autoLap = false;
    boolean eventCues = false;
    boolean zoneCues = false;
    SharedPreferences sharedPreferences;
    private int youTubeIndex = -1;
    private int youTubeSeekTo = -1;
    private ArrayList<PointF> mPowerLineArray;
    private ArrayList<PointF> mHeartRateLineArray;
    private ArrayList<PointF> mCadenceLineArray;
    private boolean firstPopUp = true;
    Context mContext;
    WidgetWorkoutLapDialog mLapDialog;
    MediaPlayer mp;
    Realm realm;
    Profile mProfile;
    VideoFrag videoFrag;
    FITAudio fitAudio;
    boolean savedinstance = false;

    @ViewById(R.id.workout_session_text_session_time)
    TextView sessionTimeText;
    @ViewById(R.id.workout_session_text_lap_time)
    TextView lapTimeText;
    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;
    @ViewById(R.id.button_right)
    FitButton rightButton;
    @ViewById(R.id.workout_overview_graph)
    WorkoutGraphView graph;
    @ViewById(R.id.workout_session_video_frame_layout)
    FrameLayout videoFrameLayout;
    @ViewById(R.id.workour_session_hud_fit_linear_layout)
    FitLinearLayout hudLayout;
    @FragmentById(R.id.workout_session_hud_fragment)
    HUDPagerFragment hudPagerFragment;

    @Bean
    VideoController videoController;

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            mSessionController.registerObserver(WorkoutActivity.this);
            if (mSessionController.getState() == SessionController_.SessionState.WorkoutPaused) {
                sessionStateChanged(SessionController_.SessionState.WorkoutPaused);
            } else if (mSessionController.getState() == SessionController.SessionState.Complete) {
                //do nothing
            } else {
                mSessionController.startResumeWorkout();
            }
            Workout workout = mSessionController.getWorkout();
            graph.setCurrentTimeLineVisibility(true);
            graph.setGradient(R.attr.colorFitPrimary, R.attr.colorFitBg0);
            graph.drawEntireWorkoutPower(workout);
            mSessionController.refreshSettings();
            sessionStateChanged(mSessionController.getState());
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FragmentManager fm = getSupportFragmentManager();
        workerFragment = (WorkoutWorkerFragment) fm.findFragmentByTag(TAG_WORKER_FRAGMENT);
        if (workerFragment == null) {
            workerFragment = new WorkoutWorkerFragment();
            fm.beginTransaction().add(workerFragment, TAG_WORKER_FRAGMENT).commit();
        }
        if (savedInstanceState == null) {
            mPowerLineArray = new ArrayList<>();
            mHeartRateLineArray = new ArrayList<>();
            mCadenceLineArray = new ArrayList<>();
        } else {
            mPowerLineArray = workerFragment.getmPowerLineArray();
            mHeartRateLineArray = workerFragment.getmHeartRateLineArray();
            mCadenceLineArray = workerFragment.getmCadenceLineArray();
        }
        fitAudio = new FITAudio(getApplicationContext());
        sharedPreferences = getSharedPreferences(SettingsActivity.getSettingsNamespace(), MODE_PRIVATE);
        hidePopUps = sharedPreferences.getBoolean(SettingsActivity.HIDE_POPUPS + Profile.getUUID(), false);
        voiceOver = sharedPreferences.getBoolean(SettingsActivity.VOICE_OVERS_ON + Profile.getUUID(), false);
        eventCues = sharedPreferences.getBoolean(SettingsActivity.EVENT_CUES_ON + Profile.getUUID(), false);
        autoLap = sharedPreferences.getBoolean(SettingsActivity.AUTO_LAP_INDICATORS + Profile.getUUID(), false);
        zoneCues = sharedPreferences.getBoolean(SettingsActivity.ZONE_CUES_ON + Profile.getUUID(), false);

        mContext = this;
        realm = Realm.getDefaultInstance();
        mProfile = Profile.current();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(SessionController_.intent(this).get());
        bindService(SessionController_.intent(this).get(), mSessionConnection, BIND_AUTO_CREATE);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoController.getVideo() != null && !isRemoteDisplaying()) {
            middleButton.setVisibility(Button.VISIBLE);
        } else {
            middleButton.setVisibility(Button.GONE);
        }
    }

    @AfterViews
    public void afterViews() {
        savedinstance = false;
        Crashlytics.log(0, TAG, "afterViews() called");
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        middleButton.setText(getString(R.string.video));
        leftButton.setVisibility(View.INVISIBLE);
        leftButton.setText(getString(R.string.end));
        rightButton.setText(getString(R.string.pause));
        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        if (videoController.getVideo() != null && FitCastService.getInstance() == null) {
            CountDownTimer timer = new CountDownTimer(1500, 1500) {
                @Override
                public void onTick(long millisUntilFinished) {/*nothing here*/}

                @Override
                public void onFinish() {
                    if(!savedinstance) {
                        setUpVideo();
                        flipVideo();
                    }
                }
            };
            timer.start();
        } else {
            middleButton.setVisibility(View.GONE);
            videoFrameLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        workerFragment.setmPowerLineArray(mPowerLineArray);
        workerFragment.setmHeartRateLineArray(mHeartRateLineArray);
        workerFragment.setmCadenceLineArray(mCadenceLineArray);
        savedinstance = true;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mLapDialog != null) {
            mLapDialog.dismiss();
        }
        if (mSessionController != null) {
            mSessionController.unregisterObserver(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSessionConnection != null) {
            unbindService(mSessionConnection);
        }
        if (isFinishing()) {
            stopService(SessionController_.intent(this).get());
            mSessionConnection = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            if (videoController.getVideo() != null) {
                videoController.setVideo(null);
                fitAudio.releaseSoundPool();
            }
        }
        realm.close();
    }

    @Click(R.id.button_left)
    public void onButtonLeft() {
        if (mSessionController.sessionRunning()) {
            if (mSessionController.getWorkout() == null || !mSessionController.getWorkout().isFTPTest()) {
                mSessionController.markLap();
            }
        } else {
            if (!mSessionController.sessionComplete()) {
                if (videoFrag != null) {
                    videoFrag.pause();
                }
                if (eventCues) {
                    mp = MediaPlayer.create(this, R.raw.end_of_workout_sound);
                    mp.start();
                }
                mSessionController.completeSession();
            }
        }
    }

    @Click(R.id.button_right)
    public void onButtonRight() {
        if (mSessionController.sessionRunning()) {
            mSessionController.pauseWorkout();
            if (videoFrag != null && !(videoFrag instanceof YouTubeVideoOverlayFragment)) {
                videoFrag.pause();
            }
        } else if (mSessionController.sessionComplete()) {
            workoutSessionComplete();
        } else {
            mSessionController.startResumeWorkout();
            if (videoFrag != null && !(videoFrag instanceof YouTubeVideoOverlayFragment)) {
                videoFrag.resume();
            }
        }
    }

    @Click(R.id.button_middle)
    public void onButtonMiddle() {
        flipVideo();
    }

    protected void updateValues() {
        String workoutString = ViewStyling.timeToStringMSF(mSessionController.getDurations().workoutDuration);
        sessionTimeText.setText(workoutString);
        String lapString = ViewStyling.timeToStringMSF(mSessionController.getDurations().lapDuration);
        lapTimeText.setText(lapString);
        if (videoController.getVideo() != null && videoFrag != null) {
            videoFrag.updateValues();
        }
        if (mSessionController.getWorkout() != null) {
            float pctDone = (float) (mSessionController.getDurations().workoutDuration /
                    mSessionController.getWorkout().getDuration());
            graph.updateScroller(pctDone);
            mPowerLineArray.add(new PointF(pctDone, mSessionController.getSensorValues().currentPower));
            graph.updatePowerLineArray(mPowerLineArray);
            mHeartRateLineArray.add(new PointF(pctDone, mSessionController.getSensorValues().currentHeartRate));
            graph.updateHeartRateLineArray(mHeartRateLineArray);
            mCadenceLineArray.add(new PointF(pctDone, (float) mSessionController.getSensorValues().currentCadence));
            graph.updateCadenceLineArray(mCadenceLineArray);
            graph.invalidate();
        }
    }

    @Override
    public void newWorkoutTextAndTime(final WorkoutTextAndTime tat) {
        if (tat == null) {
            return;
        }
        if (!hidePopUps) {
            tatDialog(tat);
            if (voiceOver) {
                fitAudio.playVoiceOver(tat.getText());
            }
        }
        if (eventCues) {
            if (firstPopUp) {
                fitAudio.playFITSound(FITAudio.SoundId.StartWorkout);
            } else {
                fitAudio.playFITSound(FITAudio.SoundId.ZoneStart);
            }
        }
        firstPopUp = false;
    }

    private void tatDialog(WorkoutTextAndTime tat) {
        mLapDialog = new WidgetWorkoutLapDialog(this, tat);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mLapDialog.getWindow().getAttributes());
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            lp.width = 1500;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        mLapDialog.getWindow().setAttributes(lp);
        mLapDialog.show();
    }

    @Override
    public void sessionTick(double timeDelta) {
        updateValues();
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {
        switch (state) {
            case Complete: {
                leftButton.setVisibility(View.INVISIBLE);
                rightButton.setText(getString(R.string.results));
                rightButton.setFitButtonStyle(FitButton.DEFAULT);
                rightButton.setVisibility(View.VISIBLE);
                middleButton.setVisibility(View.INVISIBLE);
            }
            break;
            case Workout: {
                rightButton.setText(getString(R.string.pause));
                rightButton.setFitButtonStyle(FitButton.BASIC);
                if (mSessionController.getWorkout() == null) {
                    leftButton.setText(getString(R.string.lap));
                    leftButton.setVisibility(View.VISIBLE);
                } else {
                    leftButton.setVisibility(View.INVISIBLE);
                }
                if (eventCues) {
//                TODO need new beeper here
                    fitAudio.playFITSound(FITAudio.SoundId.StartWorkout);
                }
            }
            break;
            case WorkoutPaused: {
                rightButton.setText(getString(R.string.resume));
                rightButton.setFitButtonStyle(FitButton.DEFAULT);
                leftButton.setText(getString(R.string.end));
                leftButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
                leftButton.setVisibility(View.VISIBLE);
                if (eventCues) {
//                TODO need new beeper here
                    fitAudio.playFITSound(FITAudio.SoundId.EndWorkout);
                }
            }
            break;
        }
    }

    public void setUpVideo() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (videoController.getVideo().youTubeId != null) {
            videoFrag = new YouTubeVideoOverlayFragment();
        } else {
            videoFrag = new WorkoutVideoOverlayFragment();
        }
        ft.replace(videoFrameLayout.getId(), videoFrag).commit();
    }

    public void flipVideo() {
        if (videoController.getVideo() != null) {
            middleButton.setVisibility(Button.VISIBLE);
            videoFrameLayout.setVisibility(View.VISIBLE);
            hudLayout.setVisibility(View.GONE);
        } else {
            middleButton.setVisibility(Button.GONE);
            videoFrameLayout.setVisibility(View.INVISIBLE);
            hudLayout.setVisibility(View.VISIBLE);
        }
    }

    public int getYouTubeSeekTo() {
        return youTubeSeekTo;
    }

    public void setYouTubeSeekTo(int youTubeSeekTo) {
        this.youTubeSeekTo = youTubeSeekTo;
    }

    public int getYouTubeIndex() {
        return youTubeIndex;
    }

    public void setYouTubeIndex(int youTubeIndex) {
        this.youTubeIndex = youTubeIndex;
    }

    public VideoController getVideoController() {
        return videoController;
    }

    private boolean isRemoteDisplaying() {
        return CastRemoteDisplayLocalService.getInstance() != null;
    }

    @Override
    public JSONArray getHudPagerData() {
        return mProfile.getCustomHuds().equals(new JSONArray()) ?
                StandardHuds.getStandardHudPagerData() : mProfile.getCustomHuds();
    }

    @Override
    public JSONArray getHudData() {
        return StandardHuds.getSingleHudArray();
    }

    public void workoutSessionComplete() {
        showEndOfSessionProgressDialog();
        final Session session = mSessionController.finishAndCleanup(TAG + "workoutSessionComplete");
        realm.beginTransaction();
        session.setCalculatedFTP(mSessionController.getNewFTP());
        realm.commitTransaction();
        finishAndLeaveActivity(session);
    }

    @Override
    public void onBackPressed() {
        if (mSessionController.getState() == null) {
            mSessionController.deleteSession();
            super.onBackPressed();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stop Workout?");
            builder.setMessage("Are you sure you want to cancel this workout?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mSessionController.stopTimer();
                    mSessionController.deleteSession();
                    RootActivity_.intent(WorkoutActivity.this)
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

    public void finishAndLeaveActivity(Session session) {
        sendEndOfSessionKPI(session);
        final String objectId = session.getUuid();
        leaveActivity(objectId);
    }

    @UiThread
    void showEndOfSessionProgressDialog() {
        mProgressDialog = ProgressDialog.show(this, "Great Workout!", "One moment while we get that ready to review!", true);
    }

    public void sendEndOfSessionKPI(Session session) {
        FitAnalytics.sendWorkoutSessionKPI(session.getUuid(),
                mSessionController.getPowerSensorName(), mSessionController.getCadenceSensorName(),
                mSessionController.getSpeedSensorName(), mSessionController.getHeartSensorName(),
                videoController.getVideoTitle());
    }

    void leaveActivity(String objectId) {
        Intent i = new Intent(WorkoutActivity.this, AnalysisActivity.class)
                .putExtra(AnalysisActivity.EXTRA_SESSION_UUID, objectId)
                .putExtra(AnalysisActivity.END_OF_WORKOUT, true);
        startActivity(i);
        finish();
    }
}
