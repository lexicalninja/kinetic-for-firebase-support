package com.kinetic.fit.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.ShareEvent;
import com.kinetic.fit.data.realm_objects.Session;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import io.realm.Realm;

/**
 * Created by Saxton on 5/23/17.
 */

public class FitAnalytics {

    public static void sendWorkoutSessionKPI(final String sessionId, final String powerSensor, final String cadenceSensor,
                                             final String speedSensor, final String heartSensor, final String video) {

        if (sessionId != null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Realm realm = Realm.getDefaultInstance();
                    Session session = realm.where(Session.class).equalTo("uuid", sessionId).findFirst();
                    Answers.getInstance().logCustom(new CustomEvent("wokoutSession")
                            .putCustomAttribute("workoutName", session.getWorkoutName())
                            .putCustomAttribute("kilojoules", session.getKilojoules())
                            .putCustomAttribute("duration", session.getDuration())
                            .putCustomAttribute("powerSensor", powerSensor)
                            .putCustomAttribute("cadenceSensor", cadenceSensor)
                            .putCustomAttribute("speedSensor", speedSensor)
                            .putCustomAttribute("heartSensor", heartSensor)
                            .putCustomAttribute("distanceKM", session.getDistanceKM())
                            .putCustomAttribute("video", video));
                    realm.close();
                }
            });
        }
    }

    public static void sendShareKPI(final String shareMethod) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Answers.getInstance().logShare(new ShareEvent().putMethod(shareMethod));
            }
        });
    }
}
