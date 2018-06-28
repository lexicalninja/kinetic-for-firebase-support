package com.kinetic.fit.ui.displays;

import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

/**
 * Created by Saxton on 5/24/17.
 */


@EActivity(R.layout.activity_property_select)
public class PropertySelectActivity extends FitActivity implements PropertyRecyclerAdapter.PropertySelectListener {
    private static final String TAG = "PropSelect";


    @ViewById(R.id.property_recycler)
    RecyclerView recyclerView;
    @ViewById(R.id.button_left)
    FitButton buttonLeft;
    @ViewById(R.id.button_middle)
    FitButton buttonMiddle;
    @ViewById(R.id.button_right)
    FitButton buttonRight;
    PropertyRecyclerAdapter mAdapter;
    Integer ordinal;
    View selectedView;

    @AfterViews
    void afterViews() {
        mAdapter = new PropertyRecyclerAdapter(this);
        mAdapter.resisterListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemViewCacheSize(200);
        recyclerView.setAdapter(mAdapter);
        buttonLeft.setVisibility(View.INVISIBLE);
        buttonMiddle.setFitButtonStyle(FitButton.DESTRUCTIVE);
        buttonMiddle.setText(getString(R.string.remove));
        buttonRight.setFitButtonStyle(FitButton.BASIC);
        buttonRight.setText(R.string.select);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endSelection();
            }
        });
        buttonMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                int viewId = getIntent().getIntExtra("viewId", -1);
                result.putExtra("viewId", viewId)
                        .putExtra("property", FitProperty.None);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }


    @Override
    public void selectProperty(int pos, View v) {
        Log.d(TAG, "pos: " + pos + " ChildAt: " + recyclerView.getChildAt(pos));
        if (ordinal != null) {
            selectedView.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitBg0, this));
            ((TextView) ((LinearLayout) selectedView).getChildAt(0)).setTextColor(ViewStyling.getColor(R.attr.colorFitBody, this));
        }
        if (selectedView == null || v != selectedView) {
            ordinal = mAdapter.listItems.get(pos).getOrdinal();
            selectedView = v;
            v.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            ((TextView) ((LinearLayout) v).getChildAt(0)).setTextColor(ViewStyling.getColor(R.attr.colorFitBg0, this));
        } else {
            ordinal = null;
            selectedView = null;
        }
    }

    @Override
    public void onBackPressed() {
        endSelection();
    }

    void endSelection() {
        Intent result = new Intent();
        int viewId = getIntent().getIntExtra("viewId", -1);
        FitProperty prop;
        if (ordinal == null) {
            prop = FitProperty.None;
        } else {
            prop = FitProperty.values()[ordinal];
        }
        result.putExtra("viewId", viewId)
                .putExtra("property", prop);
        setResult(RESULT_OK, result);
        finish();

    }
}
