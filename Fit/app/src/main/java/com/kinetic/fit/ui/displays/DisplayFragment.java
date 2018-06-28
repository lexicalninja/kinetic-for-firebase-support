package com.kinetic.fit.ui.displays;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.widget.DisplayViewWidget;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.realm.Realm;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Saxton on 5/19/17.
 */


@EFragment(value = R.layout.fragment_display, forceLayoutInjection = true)
public class DisplayFragment extends Fragment implements DisplayViewWidget.DisplayOnTouchListener {

    static final int DISPLAY_SELLECT_REQ = 555;

    @ViewById(R.id.display1)
    DisplayViewWidget display1;
    @ViewById(R.id.display2)
    DisplayViewWidget display2;
    @ViewById(R.id.display3)
    DisplayViewWidget display3;
    @ViewById(R.id.display4)
    DisplayViewWidget display4;
    @ViewById(R.id.display5)
    DisplayViewWidget display5;
    @ViewById(R.id.display6)
    DisplayViewWidget display6;
    @ViewById(R.id.display7)
    DisplayViewWidget display7;
    @ViewById(R.id.display8)
    DisplayViewWidget display8;
    @ViewById(R.id.display9)
    DisplayViewWidget display9;

    ArrayList<DisplayViewWidget> row1 = new ArrayList<>();
    ArrayList<DisplayViewWidget> row2 = new ArrayList<>();
    ArrayList<DisplayViewWidget> row3 = new ArrayList<>();

    JSONArray rowArray1 = new JSONArray();
    JSONArray rowArray2 = new JSONArray();
    JSONArray rowArray3 = new JSONArray();

    JSONArray mHudArray;
    Profile mProfile;
    int hudNumber;
    HUDUpdateListener mListener;


    public static DisplayFragment newInstance(JSONArray hudArray, int hudNumber) {
        Bundle args = new Bundle();
        DisplayFragment fragment = new DisplayFragment();
        fragment.setArguments(args);
        fragment.setRetainInstance(false);
        fragment.setHudArray(hudArray);
        fragment.mProfile  = Profile.current();
        fragment.hudNumber = hudNumber;
        return fragment;
    }

    @AfterViews
    void afterFragmentViews() {
        setUpRows();
        setUpHudData();
        setRowViews(row1, rowArray1);
        setRowViews(row2, rowArray2);
        setRowViews(row3, rowArray3);

    }

    @Override
    public void changeDisplay(View view) {
        PropertySelectActivity_.intent(this).extra("viewId", view.getId()).startForResult(DISPLAY_SELLECT_REQ);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 555) {
                int viewId = data.getIntExtra("viewId", -1);
                if (viewId != -1) {
                    FitProperty property = (FitProperty) data.getSerializableExtra("property");
                    DisplayViewWidget widget = (DisplayViewWidget) getView().findViewById(viewId);
                    widget.setFitProperty(property);
                    updateJsonArray();
                }
            }
        }
    }

    public void setHudArray(JSONArray hudArray) {
        this.mHudArray = hudArray;
    }

    public void setHudNumber(int hudNumber) {
        this.hudNumber = hudNumber;
    }

    public void setUpHudData() {
        try {
            rowArray1 = mHudArray.getJSONArray(0);
            rowArray2 = mHudArray.getJSONArray(1);
            rowArray3 = mHudArray.getJSONArray(2);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void setUpRows() {
        row1.clear();
        row2.clear();
        row3.clear();
        row1.add(display1);
        row1.add(display2);
        row1.add(display3);
        row2.add(display4);
        row2.add(display5);
        row2.add(display6);
        row3.add(display7);
        row3.add(display8);
        row3.add(display9);
        for (DisplayViewWidget v : row1) {
            v.setDisplayOnTouchListener(this);
        }
        for (DisplayViewWidget v : row2) {
            v.setDisplayOnTouchListener(this);
        }
        for (DisplayViewWidget v : row3) {
            v.setDisplayOnTouchListener(this);
        }
    }

    private void setRowViews(ArrayList<DisplayViewWidget> list, JSONArray array) {
        for (int i = 0; i < 3; i++) {
            try {
                if (i < array.length() && array.get(i) != null) {
                    list.get(i).setFitProperty(FitProperty.values()[array.getInt(i)]);
                } else {
                    list.get(i).setFitProperty(FitProperty.None);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void updateJsonArray(){
        rowArray1 = new JSONArray();
        for(DisplayViewWidget v : row1){
            if(v.getAttribute() != FitProperty.None){
                rowArray1.put(v.getAttribute().ordinal());
            }
        }
        rowArray2 = new JSONArray();
        for(DisplayViewWidget v : row2){
            if(v.getAttribute() != FitProperty.None){
                rowArray2.put(v.getAttribute().ordinal());
            }
        }
        rowArray3 = new JSONArray();
        for(DisplayViewWidget v : row3){
            if(v.getAttribute() != FitProperty.None){
                rowArray3.put(v.getAttribute().ordinal());
            }
        }
        mHudArray = new JSONArray();
        mHudArray.put(rowArray1);
        mHudArray.put(rowArray2);
        mHudArray.put(rowArray3);

        mListener.updateHud(hudNumber, mHudArray);
    }

    public interface HUDUpdateListener{
        void updateHud(int hudNumber, JSONArray hudArrray);
    }

    void registerListener(HUDUpdateListener listener){
        mListener = listener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
