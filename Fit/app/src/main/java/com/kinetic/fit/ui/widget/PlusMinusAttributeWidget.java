package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 1/11/17.
 */


public class PlusMinusAttributeWidget extends LinearLayout {
    TextView attributeDown;
    TextView attributeUp;
    TextView attributeValue;

    public PlusMinusAttributeWidget(Context context) {
        super(context);
        init(context);
    }

    public PlusMinusAttributeWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlusMinusAttributeWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlusMinusAttributeWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public int getAttributeValue(){
        return Integer.parseInt(attributeValue.getText().toString());
    }

    public void setAttributeValue(int newVal){
        attributeValue.setText(String.valueOf(newVal));
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View.inflate(getContext(), R.layout.plus_minus_attribute_widget, this);
        attributeValue = (TextView) findViewById(R.id.plus_minus_attribute_value);
        attributeDown = (TextView) findViewById(R.id.plus_minus_attribute_down);
        attributeUp = (TextView) findViewById(R.id.plus_minus_attribute_up);
        attributeDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                int newattributeValue = getAttributeValue();
                newattributeValue--;
                setAttributeValue(newattributeValue);
            }
        });
        attributeUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                int newAttributeValue = getAttributeValue();
                newAttributeValue++;
                setAttributeValue(newAttributeValue);
            }
        });
    }

    void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

}
