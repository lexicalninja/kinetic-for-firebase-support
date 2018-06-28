package com.kinetic.fit.cast;

import android.content.Context;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.VideoView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.hud.HUDPropertyView;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.video.VideoControllerItem;
import com.kinetic.fit.ui.video.VideoController_;
import com.kinetic.fit.ui.widget.WorkoutGraphView;

import java.util.ArrayList;

public class SessionCastPresentation extends FitCastPresentation {

    private final String TAG = "SessionCastPresentation";

    private long countdown;
    private long start;
    private long now;
    private long finish;

    private ArrayList<PointF> mPowerLineArray;
    private ArrayList<PointF> mHeartRateLineArray;
    private ArrayList<PointF> mCadenceLineArray;

    public SessionCastPresentation(Context context, Display display) {
        super(context, display);
    }

    VideoView videoPlayer;

    HUDPropertyView powerView;
    HUDPropertyView speedView;
    HUDPropertyView cadenceView;
    HUDPropertyView heartView;

    HUDPropertyView powerTargetView;
    HUDPropertyView cadenceTargetView;
    HUDPropertyView intervalTimeView;
    HUDPropertyView distanceView;

    WorkoutGraphView graphView;
    SessionCastDialogView dialogView;

    ArrayList<HUDPropertyView> propertyViews = new ArrayList<>();

    VideoController videoController = VideoController_.getInstance_(getContext());

    Workout mWorkout;
    boolean videoComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cast_session_video);

        videoPlayer = (VideoView) findViewById(R.id.videoView);

        powerView = (HUDPropertyView) findViewById(R.id.powerPropertyView);
        speedView = (HUDPropertyView) findViewById(R.id.speedPropertyView);
        cadenceView = (HUDPropertyView) findViewById(R.id.cadencePropertyView);
        heartView = (HUDPropertyView) findViewById(R.id.heartPropertyView);

        powerTargetView = (HUDPropertyView) findViewById(R.id.powerTargetPropertyView);
        distanceView = (HUDPropertyView) findViewById(R.id.distancePropertyView);
        cadenceTargetView = (HUDPropertyView) findViewById(R.id.cadenceTargetPropertyView);
        intervalTimeView = (HUDPropertyView) findViewById(R.id.intervalTimePropertyView);

        powerView.setProperty(FitProperty.Power);
        speedView.setProperty(FitProperty.SpeedKPH);
        cadenceView.setProperty(FitProperty.Cadence);
        heartView.setProperty(FitProperty.HeartRate);

        powerTargetView.setProperty(FitProperty.PowerTarget);
        distanceView.setProperty(FitProperty.Distance);
        cadenceTargetView.setProperty(FitProperty.CadenceTarget);
        intervalTimeView.setProperty(FitProperty.WorkoutIntervalDurationToGo);
        intervalTimeView.getmPropertyValue().setTextSize(16);//changes text size to accomodate 16:9 on chromecast

        graphView = (WorkoutGraphView) findViewById(R.id.session_cast_graph_view);
        graphView.setCurrentTimeLineVisibility(true);
        if (mWorkout != null) {
            graphView.drawEntireWorkoutPower(mWorkout);
        }

        dialogView = (SessionCastDialogView) findViewById(R.id.cast_dialog_view);
        dialogView.setVisibility(View.INVISIBLE);

        propertyViews.add(powerView);
        propertyViews.add(speedView);
        propertyViews.add(cadenceView);
        propertyViews.add(heartView);
        propertyViews.add(powerTargetView);
        propertyViews.add(distanceView);
        propertyViews.add(cadenceTargetView);
        propertyViews.add(intervalTimeView);
        propertyViews.add(dialogView.powerHUD);

        mPowerLineArray = new ArrayList<>();
        mHeartRateLineArray = new ArrayList<>();
        mCadenceLineArray = new ArrayList<>();
        videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoComplete = true;
                videoPlayer.clearAnimation();
            }
        });
    }


    @Override
    protected void onStart() {
        VideoControllerItem video = videoController.getVideo();
        if (video != null) {
            if (video.uri != null) {
                videoPlayer.setVideoURI(video.uri);
            }
        }

    }

    @Override
    void sessionStateChanged(SessionController.SessionControllerBinder controller, SessionController.SessionState state) {
        if (state != SessionController.SessionState.Workout) {
            videoPlayer.pause();
        }
    }

    @Override
    void sessionTick(SessionController.SessionControllerBinder controller, double timeDelta) {
        for (HUDPropertyView view : propertyViews) {
            view.updateValue(controller, null);
        }
        if (!videoComplete) {
            if (!videoPlayer.isPlaying() && controller.getState() == SessionController.SessionState.Workout) {
                videoPlayer.start();
            }
            int currentPos = videoPlayer.getCurrentPosition();
            int workoutPos = (int) (controller.getDurations().workoutDuration * 1000);
            int delta = Math.abs(currentPos - workoutPos);
            if (delta > 1000) {
                videoPlayer.seekTo(workoutPos);
            }
        }
        if (controller.getWorkout() != null) {
            float pctDone = (float) (controller.getDurations().workoutDuration /
                    controller.getWorkout().getDuration());
            graphView.updateScroller(pctDone);

            mPowerLineArray.add(new PointF(pctDone, controller.getSensorValues().currentPower));
            graphView.updatePowerLineArray(mPowerLineArray);

            mHeartRateLineArray.add(new PointF(pctDone, controller.getSensorValues().currentHeartRate));
            graphView.updateHeartRateLineArray(mHeartRateLineArray);

            mCadenceLineArray.add(new PointF(pctDone, (float) controller.getSensorValues().currentCadence));
            graphView.updateCadenceLineArray(mCadenceLineArray);

            graphView.invalidate();
        }

        now = System.currentTimeMillis();

        if (now < finish) {
            long timer = ((finish - now) / 1000);
            dialogView.lapHUD.changeTimer(timer);
        } else {
            dialogView.lapHUD.changeTimer(0);
            graphView.setVisibility(View.VISIBLE);
            dialogView.setVisibility(View.GONE);
        }

    }

    @Override
    void sensorValues(SessionController.SessionControllerBinder controller, SensorValues values) {
        for (HUDPropertyView view : propertyViews) {
            view.updateValue(controller, values);
        }
    }

    @Override
    void newWorkoutTextAndTime(SessionController.SessionControllerBinder controller, WorkoutTextAndTime tat) {

        if (tat != null) {
            graphView.setVisibility(View.GONE);
            dialogView.setVisibility(View.VISIBLE);
            dialogView.newTextAndTime(tat);
            start = System.currentTimeMillis();
            countdown = ((long) tat.getCountdown() * 1000);
            finish = start + countdown;
        } else if (now > finish) {
            graphView.setVisibility(View.VISIBLE);
            dialogView.setVisibility(View.GONE);
            dialogView.lapHUD.changeTimer(0);
        }
    }

    public void setWorkout(Workout workout) {
        if (workout != null) {
            mWorkout = workout;
        }
    }

//    public void youtubeUnavailableToast() {
//        ViewStyling.getCustomToast(mContext, getLayoutInflater(),
//                mContext.getString(R.string.youtube_unavailable)).show();
//
//    }
}
