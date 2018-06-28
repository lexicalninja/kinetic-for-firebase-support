package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.garmin.fit.FitRuntimeException;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.exporting.KINKineticClient;
import com.kinetic.fit.ui.settings.profile.ProfileActivity_;
import com.kinetic.fit.util.FitAnalytics;
import com.kinetic.fit.util.FitSystemNotifications;
import com.kinetic.fit.util.ViewStyling;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@EBean(scope = EBean.Scope.Default)
public class StravaClient {

    // TODO: This could be "cleaner"
    public static final String STRAVA_CLIENT_ID = "<api-key>";

    protected static final String TAG = "StravaClient";

    public static final String STATUS_CHANGED = "Strava.CONNECTION_CHANGED";

    private static final String STRAVA_CLIENT_SECRET = "<api-key>";
    private static final String PREF_ACCESS_TOKEN = "AccessToken";
    private static final String PREF_SHARE_PUBLIC = "Public";
    private static final String PREF_AUTO_SHARE = "AutoShare";

    private SharedPreferences sharedPreferences;

    public boolean getSharePublic() {
        return sharedPreferences.getBoolean(PREF_SHARE_PUBLIC + userUuid(), false);
    }

    public void setSharePublic(boolean sharePublic) {
        sharedPreferences.edit().putBoolean(PREF_SHARE_PUBLIC + userUuid(), sharePublic).commit();
    }

    public boolean getAutoShare() {
        return sharedPreferences.getBoolean(PREF_AUTO_SHARE + userUuid(), false);
    }

    public void setAutoShare(boolean autoShare) {
        sharedPreferences.edit().putBoolean(PREF_AUTO_SHARE + userUuid(), autoShare).commit();
    }

    public boolean isConnected() {
        return sharedPreferences.getString(PREF_ACCESS_TOKEN + userUuid(), null) != null;
    }

    public void disconnect() {
        sharedPreferences.edit().remove(PREF_ACCESS_TOKEN + userUuid()).commit();
        context.sendBroadcast(new Intent(STATUS_CHANGED));
    }

    @RootContext
    Context context;

    @AfterInject
    public void postInjection() {
        // This is run after context is injected (via the annotation)
        sharedPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    private String userUuid() {
        return Profile.getUUID();
    }

    @Background
    public void exchangeAuthToken(String authToken) {
        final Future<JsonObject> response = Ion.with(context)
                .load("https://www.strava.com/oauth/token")
                .setBodyParameter("client_id", STRAVA_CLIENT_ID)
                .setBodyParameter("client_secret", STRAVA_CLIENT_SECRET)
                .setBodyParameter("code", authToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            String accessToken = result.get("access_token").getAsString();
                            if (accessToken != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(PREF_ACCESS_TOKEN + userUuid(), accessToken);
                                editor.apply();

                                toastConnected(true);
                            } else {
                                toastConnected(false);
                            }
                            context.sendBroadcast(new Intent(STATUS_CHANGED));
                        } else {
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
                });
    }

    @UiThread
    protected void toastConnected(boolean success) {
        if (success) {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(), "Strava Connected").show();
        } else {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(), "Strava Connection Error").show();
        }
    }

    @Background
    public void uploadSession(String uuid, String workoutName, String workoutDescription) {
        try {
            Uri uri = KINKineticClient.encodeSession(context, uuid, false);

            String privateShare = "1";
            if (getSharePublic()) {
                privateShare = "0";
            }
            Future<JsonObject> response = Ion.with(context)
                    .load("https://www.strava.com/api/v3/uploads")
                    .setHeader("Authorization", "Bearer " + sharedPreferences.getString(PREF_ACCESS_TOKEN + userUuid(), "Not stored"))
                    .setMultipartParameter("data_type", "fit")
                    .setMultipartParameter("name", "Kinetic Fit Workout")
                    .setMultipartParameter("description", workoutName + "\n\n" + workoutDescription)
                    .setMultipartParameter("activity_type", "cycling")
                    .setMultipartParameter("private", privateShare)
                    .setMultipartParameter("data_type", "fit")
                    .setMultipartFile("file", new File(uri.getPath()))
                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            toastUploadResponse(result);
                            if (e == null) {
                                FitAnalytics.sendShareKPI(TAG);
                            }
                        }
                    });

            Log.d(TAG, " postData: JSON Response = " + response.get().toString());
        } catch (IOException e) {

        } catch (FitRuntimeException e) {

        } catch (ExecutionException e) {

        } catch (InterruptedException e) {

        }
    }

    @UiThread
    protected void toastUploadResponse(JsonObject response) {
        if (response != null) {
            if ((response.get("error") == null || response.get("error").isJsonNull()) && response.get("status") != null) {
                ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                        "Strava: " + response.get("status").toString()).show();
            } else if (response.get("error") != null) {
                Crashlytics.log(response.toString());
                new FitSystemNotifications(context, "Strava Error", response.get("error").toString(),
                        ProfileActivity_.class);

            } else if (response.getAsJsonArray("errors") != null) {
                String s = response.getAsJsonArray("errors").get(0).getAsJsonObject().get("code").getAsString();
                if (s.equals("invalid")) {
                    new FitSystemNotifications(context, "Strava Error, Slide down for more info.",
                            "There is an error with Strava. Please go to your profile and log in again.",
                            ProfileActivity_.class);

                    disconnect();
                }
            }
        } else {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                    "Bad Response. Please check your network connection and try again").show();
        }
    }

    public void autoShareWorkout(String uuid, String workoutName, String workoutDescription) {
        if (getAutoShare()) {
            uploadSession(uuid, workoutName, workoutDescription);
        }
    }
}