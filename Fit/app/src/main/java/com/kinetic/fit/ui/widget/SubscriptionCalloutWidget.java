package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.ui.subscriptions.SubscriptionStatusActivity_;

/**
 * Created by Saxton on 5/11/17.
 */

public class SubscriptionCalloutWidget extends LinearLayout {

    TextView calloutText;
    FitButton subscribeButton;

    public SubscriptionCalloutWidget(Context context) {
        super(context);
        init();
    }

    public SubscriptionCalloutWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SubscriptionCalloutWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SubscriptionCalloutWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        View.inflate(getContext(), R.layout.widget_subscription_callout, this);
        calloutText = (TextView) findViewById(R.id.callout_text);
        subscribeButton = (FitButton) findViewById(R.id.subscribe_button);
        subscribeButton.setFitButtonStyle(FitButton.DEFAULT);
        subscribeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SubscriptionStatusActivity_.intent(getContext()).start();
            }
        });

    }

    public void setCalloutText(String callout) {
        subscribeButton.setText(callout);
    }


}
