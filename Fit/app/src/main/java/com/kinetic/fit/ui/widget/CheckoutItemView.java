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

import java.util.Locale;

/**
 * Created by Saxton on 5/8/17.
 */

public class CheckoutItemView extends LinearLayout {

    TextView itemName;
    TextView itemCost;

    public CheckoutItemView(Context context) {
        super(context);
        init();
    }

    public CheckoutItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckoutItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CheckoutItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init(){
        View.inflate(getContext(), R.layout.list_item_checkout, this);
        itemCost = (TextView) findViewById(R.id.itemCost);
        itemName = (TextView) findViewById(R.id.itemName);
    }

    public void setItemCost(int costInPennies){
        double price = (double)costInPennies / 100.00;
        String priceString = "$" + String.format(Locale.getDefault(), "%,.2f", price);
        itemCost.setText(priceString);
    }

    public void setItemName(String name) {
        itemName.setText(name);
    }
}
