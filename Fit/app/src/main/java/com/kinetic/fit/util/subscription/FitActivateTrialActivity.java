package com.kinetic.fit.util.subscription;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.util.ViewStyling;
import com.koushikdutta.async.future.FutureCallback;


public class FitActivateTrialActivity extends Activity {

    public static final String INRIDE_ID = "INRIDE_ID";
    public static final String TRIAL_MONTHS = "TRIAL_MONTHS";
    private static final String FUNCTION_NAME = "kineticDeviceActivate";

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;
            activate();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mDataSyncConnection);
    }

    private void activate() {
        final String inRideId = getIntent().getStringExtra(INRIDE_ID);
        String trialMonths = String.valueOf(getIntent().getIntExtra(TRIAL_MONTHS, 0));
        if (inRideId != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Activate Fit Subscription?");
            builder.setMessage("Your inRide sensor gives you early access to upcoming Fit features. Would you like to activate this account with an early access Subscription for " + trialMonths + " months?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDataSync.sendTrialParseFunction(inRideId, FUNCTION_NAME, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e == null) {
                                if (result.get("activated") != null && result.get("activated").getAsBoolean()) {
                                    ViewStyling.getCustomToast(FitActivateTrialActivity.this,
                                            getLayoutInflater(), "Subscription Activated").show();
                                } else {
                                    ViewStyling.getCustomToast(FitActivateTrialActivity.this,
                                            getLayoutInflater(), "Not eligible for trial").show();
                                }
                            } else {
                                ViewStyling.getCustomToast(FitActivateTrialActivity.this, getLayoutInflater(), e.getLocalizedMessage()).show();
                                Crashlytics.logException(e);
                            }
                        }
                    });
                    finish();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            });
            builder.show();
        } else {
            finish();
        }
    }
}
