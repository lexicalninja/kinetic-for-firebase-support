package com.kinetic.fit.ui.displays;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.objects.StandardHuds;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Saxton on 5/19/17.
 */


@EActivity(R.layout.activity_displays)
public class DisplaysActivity extends FitActivity implements ViewPager.OnPageChangeListener, DisplayFragment.HUDUpdateListener {


    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;
    @ViewById(R.id.button_right)
    FitButton rightButton;
    @ViewById(R.id.displays_tabview)
    TabLayout tabView;
    ViewPager viewPager;
    JSONArray huds;
    ArrayList<JSONArray> hudArrayList;
    DisplaysPagerAdapter mAdapter;
    Realm realm;
    Profile mProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        mProfile = Profile.current();
        huds = mProfile.getCustomHuds();
        hudArrayList = new ArrayList<>();
        if (huds.equals(new JSONArray())) {
            huds = StandardHuds.getStandardHudPagerData();
        }
        hudsToArray();
    }

    @AfterViews
    public void AfterViews() {
        leftButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
        leftButton.setText(getString(R.string.reset_huds));
        middleButton.setText(getString(R.string.remove));
        middleButton.setFitButtonStyle(FitButton.DISABLED);
        rightButton.setFitButtonStyle(FitButton.DEFAULT);
        rightButton.setText(getString(R.string.add));
        mAdapter = new DisplaysPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.displays_pager);
        viewPager.setAdapter(mAdapter);
//        viewPager.setOffscreenPageLimit(1);
        tabView.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabView.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(0);
        tabView.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabView.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Click(R.id.button_left)
    void resetHUDS() {
        hudArrayList.clear();
        huds = StandardHuds.getStandardHudPagerData();
        hudsToArray();
        saveHuds();
        mAdapter.notifyDataSetChanged();
    }

    @Click(R.id.button_right)
    void newHud() {
        addHud();
    }

    @Click(R.id.button_middle)
    void trachHud() {
        removeHud();
    }

    public class DisplaysPagerAdapter extends FragmentStatePagerAdapter {

        int baseId = 0;

        public DisplaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return LegendFragment_.builder().build();
            } else {
                JSONArray a = new JSONArray();
                DisplayFragment frag = DisplayFragment_.builder().build();
                try {
                    a = huds.getJSONArray(position - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                frag.setHudArray(a);
                frag.setHudNumber(position - 1);
                frag.registerListener(DisplaysActivity.this);
                return frag;
            }
        }

        @Override
        public int getCount() {
            return huds.length() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Legend";
            } else {
                return "Display " + position;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return FragmentStatePagerAdapter.POSITION_NONE;
        }

    }

    public void addHud() {
        JSONArray newArray = new JSONArray();
        JSONArray rowArray = new JSONArray();
        newArray.put(rowArray);
        newArray.put(rowArray);
        newArray.put(rowArray);
        huds.put(newArray);
        hudArrayList.add(newArray);
        saveHuds();
        mAdapter.notifyDataSetChanged();
        viewPager.refreshDrawableState();
        tabView.getTabAt(tabView.getSelectedTabPosition());
    }


    public void removeHud() {
        int hudId = viewPager.getCurrentItem();
        huds.remove(hudId - 1);
        hudArrayList.remove(hudId - 1);
        saveHuds();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            middleButton.setFitButtonStyle(FitButton.DISABLED);
        } else {
            middleButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void updateHud(int hudNumber, JSONArray hudArrray) {
        hudArrayList.set(hudNumber, hudArrray);
        saveHuds();
        mAdapter.notifyDataSetChanged();
    }

    public void saveHuds() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                JSONArray temp = new JSONArray();
                for (JSONArray a : hudArrayList) {
                    temp.put(a);
                }
                mProfile.setCustomHuds(temp);
                mProfile = realm.copyToRealmOrUpdate(mProfile);
                huds = temp;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hudsToArray() {
        for (int i = 0; i < huds.length(); i++) {
            try {
                hudArrayList.add(huds.getJSONArray(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
