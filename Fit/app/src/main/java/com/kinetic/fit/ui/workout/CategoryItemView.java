package com.kinetic.fit.ui.workout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.list_item_category)
public class CategoryItemView extends LinearLayout {

    @ViewById
    TextView categoryCount;

    @ViewById
    TextView categoryName;

    @ViewById
    TextView categoryDescription;

    @ViewById
    ImageView categoryThumb;

    public CategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
}