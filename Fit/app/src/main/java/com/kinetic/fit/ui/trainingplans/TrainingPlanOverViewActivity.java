package com.kinetic.fit.ui.trainingplans;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanProgress;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.FitProgressDialog;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;
import com.koushikdutta.async.future.FutureCallback;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

import static java.util.Calendar.DAY_OF_WEEK;

/**
 * Created by Saxton on 10/21/16.
 */

@EActivity
public class TrainingPlanOverViewActivity extends FitActivity {
    private static final String TAG = "TPOverviewAct";

    @Extra("planId")
    String planID;

    TextView mPlanName;
    TextView mPlanDuration;
    TextView mPlanAuthor;
    ImageView mCategoryIcon;
    ImageView mExperienceIcon;
    ImageView mVolumeIcon;
    TextView mTargetRiderText;
    TextView mPlanOverviewText;
    TextView mPlanGoalsText;
    FitButton buttonLeft;
    FitButton buttonMiddle;
    FitButton buttonRight;
    boolean planInProgress = false;

    Realm realm;
    TrainingPlan tp;
    FitAlertDialog mAlertDialog;
    FitProgressDialog mProgressDialog;


    View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!planInProgress) {
                displayStartPlanDialog();
            } else {
                mAlertDialog = FitAlertDialog.show(TrainingPlanOverViewActivity.this, getString(R.string.training_plan_start_day_title),
                        getString(R.string.training_plan_dialog_quit_current_plan_message),
                        getString(R.string.no), getString(R.string.yes),
                        null,
                        cancelCurrentStartNewPlanListener,
                        true);
            }
        }
    };

    View.OnClickListener addToCalendarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            TODO add to calendar and disable button
        }
    };

    View.OnClickListener startPlanTodayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            mProgressDialog = FitProgressDialog.show(TrainingPlanOverViewActivity.this, getString(R.string.training_plan_dialog_adding_plan_title), getString(R.string.training_plan_dialog_adding_plan_message));
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    TrainingPlanProgress newPlan = realm.copyToRealmOrUpdate(new TrainingPlanProgress());
                    newPlan.setStartDate(Calendar.getInstance().getTime());
                    newPlan.setTrainingPlan(tp);
                    mDataSyncBinder.createNewTrainingPlanProgress(newPlan, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e == null) {
                                onPlanProgressParseCallback(result);
                            } else {
                                Crashlytics.logException(e);
                            }
                        }
                    });
                }
            });
        }
    };

    View.OnClickListener cancelPlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            endTPP(realm);
            RootActivity_.intent(TrainingPlanOverViewActivity.this)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
            finish();
        }
    };

    View.OnClickListener displayCancelPlanDialogListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            displayCancelPlanDialog();
        }
    };

    View.OnClickListener cancelCurrentStartNewPlanListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            endTPP(realm);
            displayStartPlanDialog();
        }
    };

    @Click(R.id.button_middle)
    void goToCalendarView() {
        TrainingPlanCalendarActivity_.intent(this).extra("planId", planID).start();
    }

    private DataSync.DataSyncBinder mDataSyncBinder;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSyncBinder = (DataSync.DataSyncBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSyncBinder = null;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
        realm = Realm.getDefaultInstance();
        tp = realm.where(TrainingPlan.class).equalTo("objectId", planID).findFirst();
        RealmResults<TrainingPlanProgress> results = realm.where(TrainingPlanProgress.class).isNull("finishDate").findAll();
        if (results.size() > 0) {
            planInProgress = true;
        }
        setContentView(R.layout.activity_training_plan_overview);
        mTargetRiderText = (TextView) findViewById(R.id.training_plan_target_rider_text_area);
        mPlanOverviewText = (TextView) findViewById(R.id.training_plan_overview_text_area);
        mPlanGoalsText = (TextView) findViewById(R.id.training_plan_overview_goals_text_area);
        mPlanName = (TextView) findViewById(R.id.training_plan_overview_plan_name);
        mPlanName.setSelected(true);
        mPlanDuration = (TextView) findViewById(R.id.training_plan_overview_duration);
        mPlanAuthor = (TextView) findViewById(R.id.training_plan_overview_creator_name);
        mCategoryIcon = (ImageView) findViewById(R.id.training_plan_overview_category_icon);
        mExperienceIcon = (ImageView) findViewById(R.id.training_plan_overview_difficulty_icon);
        mVolumeIcon = (ImageView) findViewById(R.id.training_plan_overview_volume_icon);
        buttonLeft = (FitButton) findViewById(R.id.button_left);
        buttonMiddle = (FitButton) findViewById(R.id.button_middle);
        buttonRight = (FitButton) findViewById(R.id.button_right);
        updateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportActionBar().setTitle("Training Plan Overview");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        unbindService(mDataSyncConnection);
        realm.close();
    }

    private void updateViews() {
        mTargetRiderText.setText(tp.getTargetRider());
        mPlanOverviewText.setText(tp.getPlanOverview());
        mPlanGoalsText.setText(tp.getPlanGoals());
        mPlanName.setText(tp.getPlanName());
        mPlanDuration.setText(getString(R.string.training_plan_duration_string_formatter, tp.getPlanLengthInWeeks()));
        mPlanAuthor.setText(tp.getAuthor());
        mVolumeIcon.setImageResource(tp.getPlanVolumeIconId());
        mCategoryIcon.setImageResource(tp.getCategoryIconResourceId());
        mExperienceIcon.setImageResource(tp.getExperienceLevelIconId());
        buttonMiddle.setFitButtonStyle("basic");
        buttonMiddle.setText(getString(R.string.calendar));
        buttonLeft.setVisibility(View.INVISIBLE);
        if (getCurrentPlanProgress().size() > 0) {
            buttonRight.setFitButtonStyle("destructive");
            buttonRight.setText(getString(R.string.training_plan_overview_cancel_button));
            buttonRight.setOnClickListener(displayCancelPlanDialogListener);
        } else {
            buttonRight.setFitButtonStyle(FitButton.DEFAULT);
            buttonRight.setText(R.string.training_plan_overview_start_button);
            buttonRight.setOnClickListener(startListener);
        }
    }

    void displayStartPlanDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        String positiveButtonText;
        String negativeButtonText = null;
        String titleText;
        String messageText;
        View.OnClickListener positiveListener;
        View.OnClickListener negativeListener = null;
        titleText = getString(R.string.training_plan_start_day_title);
        Calendar calendar = Calendar.getInstance();
        if ((int) calendar.get(DAY_OF_WEEK) == tp.getStartDay()) {
            messageText = getString(R.string.training_plan_start_day_today);
            positiveButtonText = getString(R.string.start);
            positiveListener = startPlanTodayListener;
        } else {
            final String startDay = Conversions.weekdayFromInteger(tp.getStartDay());
            messageText = getString(R.string.training_plan_start_today_not_recommended_day, startDay);
            positiveButtonText = startDay;
            positiveListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    TODO start training plan in realm on recommended day
                    startPlanInFuture(tp.getStartDay());
                }
            };
            negativeButtonText = getString(R.string.today);
            negativeListener = startPlanTodayListener;
        }
        mAlertDialog = FitAlertDialog.show(this, titleText, messageText, negativeButtonText,
                positiveButtonText, negativeListener, positiveListener, false);
    }

    void displayCancelPlanDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        mAlertDialog = FitAlertDialog.show(this, getString(R.string.training_plan_quit_title),
                getString(R.string.training_plan_quit_dialog_message),
                getString(R.string.no), getString(R.string.yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                    }
                },
                cancelPlanListener,
                false);
    }

    RealmResults<TrainingPlanProgress> getCurrentPlanProgress() {
        return realm.where(TrainingPlanProgress.class)
                .equalTo("trainingPlan.objectId", tp.getObjectId())
                .isNull("finishDate")
                .findAll();
    }

    void onPlanProgressParseCallback(final JsonObject result) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TrainingPlanProgress> results = getCurrentPlanProgress();
                TrainingPlanProgress tpp = results.get(0);
                tpp.setObjectId(result.get("objectId").getAsString());
                realm.copyToRealmOrUpdate(tpp);
            }
        });
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        RootActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
        finish();
//        TODO button and UI work to be done based on plan start and what day needs
    }

    private void endTPP(final Realm realm) {
        realm.beginTransaction();
        TrainingPlanProgress tpp = realm.where(TrainingPlanProgress.class)
                .isNull("finishDate")
                .findFirst();
        tpp.setFinishDate(Calendar.getInstance().getTime());
        tpp = realm.copyToRealmOrUpdate(tpp);
        realm.commitTransaction();
        final TrainingPlanProgress completedPlan = tpp;
        mDataSyncBinder.updateTrainingPlanProgress(tpp, new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e == null) {
//                    ViewStyling.getCustomToast(getBaseContext(), getLayoutInflater(), "Training Plan cancel sync successful");
                    showPlanRemovedToast();
                } else {
                    realm.beginTransaction();
                    TrainingPlanProgress prog = completedPlan;
                    prog.setFinishDate(null);
                    realm.copyToRealmOrUpdate(prog);
                    realm.commitTransaction();
                    Crashlytics.logException(e);
//                    Log.e(TAG, e.getLocalizedMessage());
                    ViewStyling.getCustomToast(getApplicationContext(), getLayoutInflater(), getString(R.string.training_plan_cancel_error));
                }
            }
        });
    }

    private void startPlanInFuture(int startDay) {
        Calendar now = Calendar.getInstance();
        while (now.get(DAY_OF_WEEK) != startDay) {
            now.add(Calendar.DATE, 1);
        }
        final Date startdate = now.getTime();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        mProgressDialog = FitProgressDialog.show(TrainingPlanOverViewActivity.this, getString(R.string.training_plan_dialog_adding_plan_title), getString(R.string.training_plan_dialog_adding_plan_message));
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TrainingPlanProgress newPlan = realm.copyToRealmOrUpdate(new TrainingPlanProgress());
                newPlan.setStartDate(startdate);
                newPlan.setTrainingPlan(tp);
                mDataSyncBinder.createNewTrainingPlanProgress(newPlan, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            onPlanProgressParseCallback(result);
                        } else {
                            Crashlytics.logException(e);
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    }
                });
            }
        });

    }

    private void showPlanRemovedToast(){
        ViewStyling.getCustomToast(this, getLayoutInflater(), getString(R.string.training_plan_cancel_error));
    }
}
