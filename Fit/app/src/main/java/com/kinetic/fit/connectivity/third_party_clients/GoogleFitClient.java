package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;

/**
 * Created by Saxton on 5/31/17.
 */

@EBean(scope = EBean.Scope.Singleton)
public class GoogleFitClient {
    private static final String TAG = "FitClient";
    GoogleApiClient mClient;

    @RootContext
    FitActivity mContext;

    @AfterInject
    public void postInjection() {
        // This is run after context is injected (via the annotation)
        buildFitnessClient();
    }

    public void setContext(FitActivity context) {
        this.mContext = context;
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        if (mClient == null && mContext != null) {
            mClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Fitness.SESSIONS_API)
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.  What to do?
                                    // Play with some sessions!!
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage((FitActivity) mContext, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                            ViewStyling.getCustomToast(mContext,
                                    ((Activity) mContext).getLayoutInflater(),
                                    "Exception while connecting to Google Play services: " + result.getErrorMessage())
                                    .show();
                        }
                    })
                    .build();
        }
    }

    @Background
    public void sendToGoogleFit(String sessionId) {
        buildFitnessClient();
        Realm realm = Realm.getDefaultInstance();
        Session session = realm.where(Session.class).equalTo("uuid", sessionId).findFirst();
        com.google.android.gms.fitness.data.Session fitSession =
                new com.google.android.gms.fitness.data.Session.Builder()
                        .setName("Kinetic Ride")
                        .setIdentifier(sessionId)
                        .setActiveTime((long) session.getDuration(), TimeUnit.SECONDS)
                        .setActivity(FitnessActivities.BIKING)
                        .setStartTime(session.getWorkoutDate().getTime(), TimeUnit.MILLISECONDS)
                        .setEndTime(session.getWorkoutDate().getTime() + (long) (session.getDuration() * 1000), TimeUnit.MILLISECONDS)
                        .setDescription(session.getWorkoutName())
                        .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(fitSession)
                .build();

        // Then, invoke the Sessions API to insert the session and await the result,
        // which is possible here because of the AsyncTask. Always include a timeout when
        // calling await() to avoid hanging that can occur from the service being shutdown
        // because of low memory or other conditions.
        Log.i(TAG, "Inserting the session in the History API");
        Fitness.SessionsApi.insertSession(mClient, insertRequest).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    showSuccessToast();
                } else {
                    Log.i(TAG, "There was a problem inserting the session: " +
                            status.getStatusMessage());
                }
            }
        });

        realm.close();

    }

    @UiThread
    void showSuccessToast() {
        ViewStyling.getCustomToast(mContext, mContext.getLayoutInflater(), "Session insert was successful!").show();
    }


}
