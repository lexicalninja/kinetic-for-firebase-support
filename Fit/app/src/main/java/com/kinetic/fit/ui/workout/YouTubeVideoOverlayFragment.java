package com.kinetic.fit.ui.workout;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
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
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;

/**
 * Created by Saxton on 3/24/16.
 */

@EFragment
public class YouTubeVideoOverlayFragment extends VideoFrag implements SessionController.SessionControllerObserver {


    public final static String TAG = "WorkoutVidOverlay";
    private static final int VIDEO_HUD_TEXT_SIZE = 32;
    private static final String YOUTUBE_API_KEY = "AIzaSyCIjtl9s5dShRG6iMTfN2rYjY09-x9jh2Q";

    private int mIntervalRemaining = 0;
    WorkoutActivity mParentActivity;

    VideoController videoController;

    LinearLayout youTubeControls;

    ImageButton youTubePrevButton;
    ImageButton youTubePausePlayButton;
    ImageButton youTubeNextButton;
    ImageButton youTubeShuffleButton;
    TextView youTubeVideoTitle;

    ProgressBar progressBarYouTube;
    YouTubePlayerSupportFragment youTubePlayerFragment;
    YouTubePlayer youTubePlayer;
    int playlistIndex;
    int youtubeTime;
    ProgressBar progressBarInterval;

    ImageButton pausePlayButton;

    HUDpropertyViewMini powerView;
    HUDpropertyViewMini speedView;
    HUDpropertyViewMini cadenceView;
    HUDpropertyViewMini heartRateView;

    LinearLayout leftHud;
    LinearLayout rightHud;

    ArrayList<HUDPropertyView> propertyViews = new ArrayList<>();

    YouTubePlayer.OnInitializedListener onInitializedListener = new YouTubePlayer.OnInitializedListener() {
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean b) {
            youTubePlayer = player;
            youTubePlayer.setPlaylistEventListener(new YouTubePlayer.PlaylistEventListener() {
                @Override
                public void onPrevious() {
                }

                @Override
                public void onNext() {
                    mParentActivity.setYouTubeIndex(++playlistIndex);
                }

                @Override
                public void onPlaylistEnded() {
                }
            });
            setUpVideo();
        }

        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
            ViewStyling.getCustomToast(mParentActivity, mParentActivity.getLayoutInflater(), getString(R.string.youtube_initialize_error)).show();
        }
    };

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            mSessionController.registerObserver(YouTubeVideoOverlayFragment.this);
            setClickListeners();
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
        playlistIndex = mParentActivity.getYouTubeIndex();
        youtubeTime = mParentActivity.getYouTubeSeekTo();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void updateValues() {
        if (mSessionController != null) {
            for (HUDPropertyView view : propertyViews) {
                view.updateValue(mSessionController, mSessionController.getSensorValues());
            }

            if (mSessionController.getCurrentInterval() != null) {
                mIntervalRemaining = (int) (mSessionController.getDurations().intervalTimeRemaining
                        / mSessionController.getCurrentInterval().duration * 100);
                progressBarInterval.setProgress(mIntervalRemaining);
            }
            try {
                if (youTubePlayer != null && youTubePlayer.isPlaying()) {
                    setYouTubeProgress();
                    mParentActivity.setYouTubeSeekTo(youTubePlayer.getCurrentTimeMillis());
                }
            } catch (IllegalStateException e) {
                youTubePlayerFragment.initialize(YOUTUBE_API_KEY, onInitializedListener);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_youtube_video_overlay, container, false);

        youTubeControls = (LinearLayout) v.findViewById(R.id.youtube_controls);

        youTubePrevButton = (ImageButton) v.findViewById(R.id.youtube_prev_button);
        youTubePausePlayButton = (ImageButton) v.findViewById(R.id.youtube_pause_play_button);
        youTubeNextButton = (ImageButton) v.findViewById(R.id.youtube_next_button);
        youTubeShuffleButton = (ImageButton) v.findViewById(R.id.youtube_shuffle_button);

        progressBarYouTube = (ProgressBar) v.findViewById(R.id.youtube_progress_bar);

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

        youTubePlayerFragment = (YouTubePlayerSupportFragment) getChildFragmentManager().findFragmentById(R.id.youtubeFrag);
        youTubePlayerFragment.initialize(YOUTUBE_API_KEY, onInitializedListener);
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
    }

    @Override
    public void onDestroy() {
        if (mSessionConnection != null) {
            getActivity().unbindService(mSessionConnection);
        }
        if (youTubePlayer != null) {
            youTubePlayer.release();
        }
        super.onDestroy();
    }

    void setClickListeners() {
        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionController.getState()
                        == SessionController.SessionState.WorkoutPaused) {
                    mSessionController.startResumeWorkout();
                    pausePlayButton.setImageResource(R.mipmap.button_video_workout_pause);
                } else {
                    mSessionController.pauseWorkout();
                    pausePlayButton.setImageResource(R.mipmap.button_video_workout_start);
                }
            }
        });
    }

    public void initVideo(SessionController.SessionControllerBinder sessionController) {
        String toastString = getString(R.string.youtube_video_toast);
        Toast toast = ViewStyling.getCustomToast(getContext(), getActivity().getLayoutInflater(), toastString);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public void hideVideoFragment() {
        mParentActivity.videoFrameLayout.setVisibility(View.INVISIBLE);
        mParentActivity.hudLayout.setVisibility(View.VISIBLE);
    }

    public void setYouTubeProgress() {
        if (youTubePlayer.getDurationMillis() == 0) {
            progressBarYouTube.setProgress(0);
        } else {
            int p = youTubePlayer.getCurrentTimeMillis() / youTubePlayer.getDurationMillis() * 100;
            progressBarYouTube.setProgress(p);
        }
    }

    @UiThread
    public void setYouTubeTitleText(String title) {
//        Log.d(TAG, title);
        youTubeVideoTitle.setText(title);
        youTubeVideoTitle.setSelected(true);
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {
    }

    @Override
    public void sessionTick(double timeDelta) {
        updateValues();
    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {
    }

    @Override
    public void start() {
        youTubePlayer.play();
    }

    @Override
    public void pause() {
        youTubePlayer.pause();
    }

    @Override
    public void resume() {
        youTubePlayer.play();
    }

    private void setUpVideo() {
        videoController = VideoController_.getInstance_(getContext());
        if (videoController.getVideo().youTubeId != null) {
            progressBarYouTube.setVisibility(View.VISIBLE);
            if (playlistIndex != -1) {
                youTubePlayer.cuePlaylist(videoController.getVideo().youTubeId, playlistIndex, youtubeTime);
            } else {
                youTubePlayer.cuePlaylist(videoController.getVideo().youTubeId);
            }
            setYouTubeProgress();
            if(getActivity() != null) {
                ViewStyling.getCustomToast(getContext(), getActivity().getLayoutInflater(), getString(R.string.youtube_video_toast)).show();
            }
            youTubePlayer.play();
//            TODO maybe use an load listener here
        }
    }
}

