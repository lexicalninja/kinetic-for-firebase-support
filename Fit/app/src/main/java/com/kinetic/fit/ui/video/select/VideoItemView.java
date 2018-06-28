package com.kinetic.fit.ui.video.select;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.list_item_video)
public class VideoItemView extends LinearLayout {

    @ViewById
    TextView videoTitle;
    @ViewById
    TextView authorName;
    @ViewById
    TextView duration;
    @ViewById
    ImageView videoThumb;
//    @ViewById
//    ImageView authorThumb;

    public VideoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
