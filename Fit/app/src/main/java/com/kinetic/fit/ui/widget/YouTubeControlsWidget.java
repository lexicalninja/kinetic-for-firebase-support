package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 5/19/16.
 */
public class YouTubeControlsWidget extends RelativeLayout {

    LayoutInflater mInflater;

    public ImageButton youTubePrevButton;
    public ImageButton youTubePausePlayButton;
    public ImageButton youTubeNextButton;
    public ImageButton youTubeShuffleButton;

    public YouTubeControlsWidget(Context context) {
        super(context);
        mInflater = LayoutInflater.from(getContext());
        init();
    }

    public YouTubeControlsWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(getContext());
        init();
    }

    public YouTubeControlsWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(getContext());
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public YouTubeControlsWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mInflater = LayoutInflater.from(getContext());
        init();
    }

    public void init() {

        mInflater.inflate(R.layout.widget_youtube_controls,(ViewGroup)getParent(), true);

        youTubePrevButton = (ImageButton) findViewById(R.id.youtube_prev_button);
        youTubePausePlayButton = (ImageButton) findViewById(R.id.youtube_pause_play_button);
        youTubeNextButton = (ImageButton) findViewById(R.id.youtube_next_button);
        youTubeShuffleButton = (ImageButton) findViewById(R.id.youtube_shuffle_button);
    }
}
