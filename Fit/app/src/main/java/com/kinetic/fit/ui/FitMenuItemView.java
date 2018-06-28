package com.kinetic.fit.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.list_drawer_item)
public class FitMenuItemView extends LinearLayout {

    @ViewById(R.id.menu_item_icon)
    ImageView mItemIcon;

    @ViewById(R.id.menu_item_title)
    TextView mItemTitle;


    public FitMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}