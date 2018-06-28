package com.kinetic.fit.ui.trainingplans;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanDay;
import com.kinetic.fit.data.realm_objects.TrainingPlanProgress;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.CalendarInformationUpdateView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Saxton on 3/14/17.
 */


@EActivity(R.layout.activity_training_plan_calendar)
public class TrainingPlanCalendarActivity extends FitActivity implements CalendarInformationUpdateView.CalendarInformationUpdateViewListener {

    private static final int MAX_OFF_SCREEN = 100;

    @ViewById(R.id.calendarView)
    CalendarInformationUpdateView calendarInformationUpdateView;

    @ViewById(R.id.recyclerview_calendar_days)
    RecyclerView recyclerView;

    Realm realm;
    TrainingPlan mPlan;
    ArrayList<TrainingPlanDay> mDays;
    int currentDay;

    TrainingPlanCalendarRecyclerViewAdapter mAdapter;

    @Extra("planId")
    String planId;

    CalendarInformationUpdateView.SortGroups currentInfo;

    interface PastCalendarListener {
        void deactivateDay();
    }

    List<PastCalendarListener> pastDays;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        mPlan = realm.where(TrainingPlan.class).equalTo("objectId", planId).findFirst();
        mDays = new ArrayList<>(mPlan.getTotalDays());
        for (int j = 0; j < mPlan.getTotalDays(); j++) {
            mDays.add(null);
        }
        RealmList<TrainingPlanDay> temp = mPlan.getTrainingPlanDays();
        for (int i = 0; i < mPlan.getTotalDays(); i++) {
            for (TrainingPlanDay day : temp) {
                if (day.getDay() == i + 1) {
                    mDays.set(i, day);
                    break;
                }
            }
        }
        currentInfo = CalendarInformationUpdateView.SortGroups.DURATION;
        pastDays = new ArrayList<>();
        TrainingPlanProgress tpp = realm.where(TrainingPlanProgress.class).isNull("finishDate").findFirst();
        if (tpp != null) {
            DateTime start = new DateTime(tpp.getStartDate());
            DateTime now = new DateTime();
            currentDay = Days.daysBetween(start.toDateMidnight(), now.toDateMidnight()).getDays() + 1;
        } else {
            currentDay = 0;
        }
    }

    @AfterViews
    void afterviews() {
        instantiateAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    void instantiateAdapter() {
        mAdapter = new TrainingPlanCalendarRecyclerViewAdapter(mDays, currentDay);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }

    public CalendarInformationUpdateView.SortGroups getCurrentInfo() {
        return currentInfo;
    }

    public void setCurrentInfo(CalendarInformationUpdateView.SortGroups currentInfo) {
        this.currentInfo = currentInfo;
    }

    @Override
    public void infoTypeSelected(CalendarInformationUpdateView.SortGroups infoType) {
        setCurrentInfo(infoType);
    }
}
