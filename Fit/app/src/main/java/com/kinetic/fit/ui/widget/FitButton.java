package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.kinetic.fit.R;
import com.kinetic.fit.util.ViewStyling;

/**
 * Created by Saxton on 12/6/16.
 */

public class FitButton extends AppCompatButton {

    public static final String DESTRUCTIVE = "destructive";
    public static final String BASIC = "basic";
    public static final String DEFAULT = "default";
    public static final String DISABLED = "disabled";

    String buttonStyle;
    static final String STYLE_ATTR_NAME = "buttonType";
    static final String NAME_SPACE = "http://schemas.android.com/apk/res/com.kinetic.fit";

    public FitButton(Context context) {
        super(context);
    }

    public FitButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        buttonStyle = attributeSet.getAttributeValue(NAME_SPACE, STYLE_ATTR_NAME);
        if (buttonStyle != null) {
            setFitButtonStyle(buttonStyle);
        } else {
            setFitButtonStyle("Basic");
        }
    }

    public void setFitButtonStyle(String styleString) {
        styleString = styleString.toLowerCase();
        switch (styleString) {
            case BASIC: {
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_basic));
                setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, getContext()));
                setClickable(true);
                break;
            }
            case DEFAULT: {
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_default));
                setTextColor(ViewStyling.getColor(R.attr.colorFitBg0, getContext()));
                setClickable(true);
                break;
            }
            case DESTRUCTIVE: {
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_destructive));
                setTextColor(ViewStyling.getColor(R.attr.colorFitDestructive, getContext()));
                setClickable(true);
                break;
            }
            case DISABLED: {
                setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_tblr_gray));
                setTextColor(ContextCompat.getColor(getContext(), R.color.fit_light_gray));
                setClickable(false);
            }
        }
    }
}


