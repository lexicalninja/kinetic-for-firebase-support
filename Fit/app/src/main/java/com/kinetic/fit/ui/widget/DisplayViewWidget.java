package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.util.ViewStyling;

/**
 * Created by Saxton on 5/18/17.
 */

public class DisplayViewWidget extends RelativeLayout {

    ImageView attributeImage;
    TextView attributeName;
    ImageView lapModifier;
    ImageView valueModifier;
    FitProperty attribute;
    DisplayOnTouchListener mListener;

    public DisplayViewWidget(Context context) {
        super(context);
        init();
    }

    public DisplayViewWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DisplayViewWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DisplayViewWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public interface DisplayOnTouchListener {
        public void changeDisplay(View view);
    }

    public void setDisplayOnTouchListener(DisplayOnTouchListener listener) {
        mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    void init() {
        View.inflate(getContext(), R.layout.widget_display_view, this);
        attributeImage = (ImageView) findViewById(R.id.attribute_image);
        attributeName = (TextView) findViewById(R.id.attribute_name);
        lapModifier = (ImageView) findViewById(R.id.lap_modifier);
        valueModifier = (ImageView) findViewById(R.id.value_modifier);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.changeDisplay(DisplayViewWidget.this);
            }
        });
    }

    public void setFitProperty(FitProperty attribute) {
        this.attribute = attribute;
        updateViews();
    }

    public void setFitPropertyByOrdinal(int ordinal) {
        this.attribute = FitProperty.values()[ordinal];
    }

    private void updateViews() {
        if (attribute != FitProperty.None) {
            attributeImage.setImageResource(attribute.image);
            DrawableCompat.setTint(attributeImage.getDrawable(), attribute.getColor(getContext()));
            attributeName.setText(attribute.keyword);
            attributeName.setTextColor(attribute.getColor(getContext()));
            if (attribute.icons.lapIcon != null) {
                lapModifier.setImageResource(attribute.icons.getLapIcon());
                lapModifier.setVisibility(VISIBLE);
            }
            if (attribute.icons.valueIcon != null) {
                valueModifier.setImageResource(attribute.icons.getValueIcon());
                valueModifier.setVisibility(VISIBLE);
            }
        } else {
            attributeImage.setImageResource(R.drawable.material_icon_cancel);
            attributeName.setText("");
            lapModifier.setVisibility(GONE);
            valueModifier.setVisibility(GONE);
            DrawableCompat.setTint(attributeImage.getDrawable(), ViewStyling.getColor(R.attr.colorFitDisabled,getContext()));
        }
    }

    public FitProperty getAttribute() {
        return attribute;
    }
}
