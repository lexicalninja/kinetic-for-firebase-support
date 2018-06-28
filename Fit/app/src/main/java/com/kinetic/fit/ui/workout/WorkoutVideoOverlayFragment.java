package com.kinetic.fit.ui.workout;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.kinetic.fit.R;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.ui.hud.HUDPropertyView;
import com.kinetic.fit.ui.hud.HUDpropertyViewMini;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.video.VideoController_;
import com.kinetic.fit.ui.widget.OnDoubleTapListener;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;

/**
 * Created by Saxton on 3/24/16.
 * /
 */

@EFragment
public class WorkoutVideoOverlayFragment extends VideoFrag implements SessionController.SessionControllerObserver {
    public final static String TAG = "WorkoutVidOverlay";
    private static final int VIDEO_HUD_TEXT_SIZE = 32;

    WorkoutActivity mParentActivity;
    VideoController videoController;
    VideoView videoPlayer;
    boolean videoComplete = false;
    ProgressBar progressBarInterval;
    ImageButton pausePlayButton;
    HUDpropertyViewMini powerView;
    HUDpropertyViewMini speedView;
    HUDpropertyViewMini cadenceView;
    HUDpropertyViewMini heartRateView;
    LinearLayout leftHud;
    LinearLayout rightHud;
    ArrayList<HUDPropertyView> propertyViews = new ArrayList<>();

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            mSessionController.registerObserver(WorkoutVideoOverlayFragment.this);
            setClickListeners();
            setVideoURI();
            initVideo(mSessionController);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().bindService(SessionController_.intent(getActivity()).get(), mSessionConnection, Context.BIND_AUTO_CREATE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        mParentActivity = (WorkoutActivity) getActivity();
        videoController = VideoController_.getInstance_(getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoController.getVideo() != null) {
            setVideoURI();
        }
    }

    @Override
    public void updateValues() {
        int intervalRemaining;
        if (mSessionController != null) {
            for (HUDPropertyView view : propertyViews) {
                view.updateValue(mSessionController, mSessionController.getSensorValues());
            }

            if (mSessionController.getCurrentInterval() != null) {
                intervalRemaining = (int) (mSessionController.getDurations().intervalTimeRemaining
                        / mSessionController.getCurrentInterval().duration * 100);
                progressBarInterval.setProgress(intervalRemaining);
            }

            int currentPos = videoPlayer.getCurrentPosition();
            int workoutPos = (int) (mSessionController.getDurations().workoutDuration * 1000);
            if (workoutPos < videoPlayer.getDuration()) {
                if (mSessionController.getState() == SessionController.SessionState.Workout) {
                    videoPlayer.start();
                }
                int delta = Math.abs(currentPos - workoutPos);
                if (delta > 1000) {
                    videoPlayer.seekTo(workoutPos);
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_video_overlay, container, false);
        videoPlayer = (VideoView) v.findViewById(R.id.videoView1);
        progressBarInterval = (ProgressBar) v.findViewById(R.id.interval_progress_bar);
        pausePlayButton = (ImageButton) v.findViewById(R.id.video_overlay_pause_button);
        powerView = (HUDpropertyViewMini) v.findViewById(R.id.videohud1);
        heartRateView = (HUDpropertyViewMini) v.findViewById(R.id.videohud2);
        speedView = (HUDpropertyViewMini) v.findViewById(R.id.videohud3);
        cadenceView = (HUDpropertyViewMini) v.findViewById(R.id.videohud4);
        powerView.setProperty(FitProperty.Power);
        speedView.setProperty(FitProperty.SpeedKPH);
        heartRateView.setProperty(FitProperty.HeartRate);
        cadenceView.setProperty(FitProperty.Cadence);
        propertyViews.add(powerView);
        propertyViews.add(speedView);
        propertyViews.add(heartRateView);
        propertyViews.add(cadenceView);
        leftHud = (LinearLayout) v.findViewById(R.id.video_hud_left);
        rightHud = (LinearLayout) v.findViewById(R.id.video_hud_right);
        videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoComplete = true;
            }
        });
        v.setOnTouchListener(new OnDoubleTapListener(getContext()) {
            @Override
            public void onDoubleTap(MotionEvent e) {
                hideVideoFragment();
            }
        });
        return v;
    }

    @AfterViews
    public void afterViews() {
        powerView.setTextSize(VIDEO_HUD_TEXT_SIZE);
        speedView.setTextSize(VIDEO_HUD_TEXT_SIZE);
        heartRateView.setTextSize(VIDEO_HUD_TEXT_SIZE);
        cadenceView.setTextSize(VIDEO_HUD_TEXT_SIZE);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLandscapeHudParams(leftHud);
            setLandscapeHudParams(rightHud);
        }
    }

    @Override
    public void onDestroy() {
        if (mSessionConnection != null) {
            getActivity().unbindService(mSessionConnection);
        }
        super.onDestroy();
    }

    public void seekToPosition(SessionController.SessionControllerBinder sessionController) {
        if (videoPlayer == null || sessionController == null) {
            return;
        }
        int workoutPos = (int) (sessionController.getDurations().workoutDuration * 1000);
        if (!videoComplete) {
            videoPlayer.seekTo(workoutPos);
        }
        if (sessionController.getState() == SessionController.SessionState.Workout) {
            videoPlayer.start();
        } else {
            if (sessionController.getState() != SessionController.SessionState.Idle) {
                pausePlayButton.setImageResource(R.mipmap.button_video_workout_start);
            }
        }
    }

    public void setVideoURI() {
        if (videoController.getVideo().streamingURL != null) {
            String streamingURL = videoController.getVideo().streamingURL;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                videoPlayer.setVideoURI(Uri.parse(streamingURL), videoController.getVideo().cookies);
            } else {
                ViewStyling.getCustomToast(getContext(), getActivity().getLayoutInflater(),
                        "Video streaming is only available on Android 5.0+ (Lollipop)").show();
            }
        } else if (videoController.getVideo().dropboxUrl != null) {
            videoPlayer.setVideoURI(Uri.parse(videoController.getVideo().dropboxUrl));
        } else {
            videoPlayer.setVideoURI(videoController.getVideo().uri);
        }
    }

    void setClickListeners() {
        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionController.getState()
                        == SessionController.SessionState.WorkoutPaused) {
                    resume();
                    mSessionController.startResumeWorkout();
                } else {
                    pause();
                    mSessionController.pauseWorkout();
                }
            }
        });
    }

    public void initVideo(SessionController.SessionControllerBinder sessionController) {
        seekToPosition(sessionController);
        ViewStyling.getCustomToast(getContext(), getActivity().getLayoutInflater(), getString(R.string.video_toast)).show();
    }

    public void hideVideoFragment() {
        mParentActivity.videoFrameLayout.setVisibility(View.GONE);
        mParentActivity.hudLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void start() {
        videoPlayer.start();
    }

    @Override
    public void pause() {
        videoPlayer.pause();
    }

    @Override
    public void resume() {
        videoPlayer.resume();
    }

    public void setLandscapeHudParams(LinearLayout hud) {
        hud.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) hud.getChildAt(0).getLayoutParams();
        params.width = 0;
        params.weight = 1;
        hud.getChildAt(0).setLayoutParams(params);
        params = (LinearLayout.LayoutParams) hud.getChildAt(1).getLayoutParams();
        params.width = 0;
        params.weight = 1;
        hud.getChildAt(1).setLayoutParams(params);
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {
        switch (state) {
            case Complete: {
                pause();
            }
            break;
            case Workout: {
                pausePlayButton.setImageResource(R.mipmap.button_video_workout_pause);
                resume();
            }
            break;
            case WorkoutPaused: {
                pausePlayButton.setImageResource(R.mipmap.button_video_workout_start);
                pause();
            }
            break;
        }
    }

    @Override
    public void sessionTick(double timeDelta) {
        updateValues();
    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {

    }
}

