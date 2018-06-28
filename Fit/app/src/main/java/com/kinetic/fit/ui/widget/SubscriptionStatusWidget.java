package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 5/8/17.
 */

public class SubscriptionStatusWidget extends LinearLayout {
    public SubscriptionStatusWidget(Context context) {
        super(context);
        init();

    }

    public SubscriptionStatusWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubscriptionStatusWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SubscriptionStatusWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init(){
        View.inflate(getContext(), R.layout.widget_subscription_status, this);
    }
}
