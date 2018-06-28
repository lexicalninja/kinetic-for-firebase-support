package com.kinetic.fit.ui.subscriptions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.SpinnerLoaderView;
import com.kinetic.fit.util.ViewStyling;
import com.kinetic.fit.util.ViewSwapper;
import com.koushikdutta.async.future.FutureCallback;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 5/11/17.
 */

@EActivity(R.layout.activity_confirmation)
public class ConfirmationActivty extends FitActivity {
    private static final String TAG = "Confirmation";
    public static final String ROLE_REBUILD_COMPLETE = "confirmation.rolesRebuilt";
    static final int spinnerSizeDP = 36;

    @ViewById(R.id.button_left)
    FitButton buttonLeft;
    @ViewById(R.id.button_middle)
    FitButton buttonMiddle;
    @ViewById(R.id.button_right)
    FitButton buttonRight;

    float scale;
    int spinnerSizePixels;

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;
            mDataSync.refreshSubscriptions();
            buttonMiddle.setFitButtonStyle(FitButton.BASIC);
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSync = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scale = getResources().getDisplayMetrics().density;
        spinnerSizePixels = (int) (spinnerSizeDP * scale + .5f);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
    }

    @AfterViews
    void afterView() {
        buttonLeft.setVisibility(View.INVISIBLE);
        buttonRight.setVisibility(View.INVISIBLE);
        buttonMiddle.setFitButtonStyle(FitButton.DISABLED);
        buttonMiddle.setText(getString(R.string.okay));
        buttonMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDataSync != null) {
                    SpinnerLoaderView spinner = new SpinnerLoaderView(ConfirmationActivty.this);
                    spinner.setLayoutParams(new LinearLayout.LayoutParams(spinnerSizePixels, spinnerSizePixels));
                    ViewSwapper.replaceView(buttonMiddle, spinner);
                    mDataSync.refreshSubscriptions2(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e == null) {
                                RootActivity_.intent(ConfirmationActivty.this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
                                finish();
                            } else {
                                ViewStyling.getCustomToast(getApplicationContext(), getLayoutInflater(), "Something went wrong. Please close the app and try your purchase again.");
                            }
                        }
                    });
                }

            }
        });
    }

    @Receiver(actions = ROLE_REBUILD_COMPLETE)
    protected void rebuildComplete() {
        buttonMiddle.setFitButtonStyle(FitButton.BASIC);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mDataSyncConnection);
    }
}

