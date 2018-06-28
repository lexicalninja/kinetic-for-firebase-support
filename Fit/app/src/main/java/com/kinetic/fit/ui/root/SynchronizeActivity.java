package com.kinetic.fit.ui.root;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.kinetic.fit.R;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.ui.widget.FitProgressDialog;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;

@EActivity(R.layout.activity_synchronize)
public class SynchronizeActivity extends Activity {

    private static final String TAG = "SynchronizeActivity";
    ProgressDialog mProgressDialog;


    private DataSync.DataSyncBinder mDataSyncBinder;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSyncBinder = (DataSync.DataSyncBinder) service;
            mDataSyncBinder.refreshAll(false);
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSyncBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO not working without internet, how fix?
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            mProgressDialog = FitProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_wait_account));
            bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {

        if (mDataSyncBinder != null) {
            unbindService(mDataSyncConnection);
        }
        super.onDestroy();
    }

    @Receiver(actions = DataSync.REFRESH_COMPLETE)
    protected void onRefreshComplete() {
        RootActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK).start();
    }
}
