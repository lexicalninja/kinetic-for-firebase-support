package com.kinetic.fit.ui.root;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.workout.OverviewActivity_;
import com.kinetic.fit.util.WorkoutParser;
import com.koushikdutta.async.future.FutureCallback;

import io.realm.Realm;

/**
 * Created by Saxton on 9/20/17.
 * asdkfjkajsdf
 */

public class ImportWorkoutActivity extends Activity {
    WorkoutParser.WorkoutDefinition mDef;
    Realm realm;
    private DataSync.DataSyncBinder mDataSyncBinder;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSyncBinder = (DataSync.DataSyncBinder) service;
            makeThatWorkout();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mDataSyncConnection != null){
            unbindService(mDataSyncConnection);
        }
    }

    private void makeThatWorkout(){
        try {
            mDef = WorkoutParser.parse(getIntent().getData(), null, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(final Realm realm) {
                Workout w = new Workout(mDef);
                mDataSyncBinder.saveImportedWorkout(w, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if(e == null){
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    saveWorkoutAndFinish(result);
                                }
                            });
                        }else {
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        });
    }

    void saveWorkoutAndFinish(JsonObject result){
        Workout w = new Workout(mDef);
        w.setObjectId(result.get("objectId").getAsString());
        realm.copyToRealmOrUpdate(w);
        OverviewActivity_.intent(this)
                .extra("workoutId", w.getObjectId())
                .start();
        finish();
    }
}
