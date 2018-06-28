package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.garmin.fit.FitRuntimeException;
import com.google.gson.JsonArray;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@EBean(scope = EBean.Scope.Default)
public class TrainingPeaksClient {

    // TODO: This could be "cleaner"
    static final String TP_CLIENT_ID = "<api-key>";
    static final String REDIRECT_URI = "<api-key>";

    private static final String TAG = "TrainingPeaksClient";

    public static final String STATUS_CHANGED = "TrainingPeaks.CONNECTION_CHANGED";

    private static final String TP_CLIENT_SECRET = "<api-key>";
    private static final String TP_GRANT_TYPE = "<api-key>";

    private static final String PREF_ACCESS_TOKEN = "AccessToken";
    private static final String PREF_SHARE_PUBLIC = "Public";
    private static final String PREF_AUTO_SHARE = "AutoShare";

    private SharedPreferences sharedPreferences;

    public boolean getSharePublic() {
        return sharedPreferences.getBoolean(PREF_SHARE_PUBLIC + userUuid(), false);
    }

    public void setSharePublic(boolean sharePublic) {
        sharedPreferences.edit().putBoolean(PREF_SHARE_PUBLIC + userUuid(), sharePublic).apply();
    }

    public boolean getAutoShare() {
        return sharedPreferences.getBoolean(PREF_AUTO_SHARE + userUuid(), false);
    }

    public void setAutoShare(boolean autoShare) {
        sharedPreferences.edit().putBoolean(PREF_AUTO_SHARE + userUuid(), autoShare).apply();
    }

    public boolean isConnected() {
        return sharedPreferences.getString(PREF_ACCESS_TOKEN + userUuid(), null) != null;
    }

    public void disconnect() {
        sharedPreferences.edit().remove(PREF_ACCESS_TOKEN + userUuid()).apply();
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
                .load("https://oauth.trainingpeaks.com/oauth/token")
                .setBodyParameter("client_id", TP_CLIENT_ID)
                .setBodyParameter("client_secret", TP_CLIENT_SECRET)
                .setBodyParameter("grant_type", TP_GRANT_TYPE)
                .setBodyParameter("redirect_uri", REDIRECT_URI)
                .setBodyParameter("code", authToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
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
                    }
                });
    }

    @UiThread
    protected void toastConnected(boolean success) {
        if (success) {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(), "Training Peaks Connected").show();
        } else {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(), "Training Peaks Connection Error").show();
        }
    }

    @Background
    public void uploadSession(String uuid, String workoutName, String uploadFileName) {
        try {
            Uri uri = KINKineticClient.encodeSession(context, uuid, false);
            String fit64 = encodeFileToBase64Binary(new File(uri.getPath()));
            String privateShare = "1";
            if (getSharePublic()) {
                privateShare = "0";
            }
            final Future<JsonArray> response = Ion.with(context)
                    .load("https://api.trainingpeaks.com/v1/file")
                    .addHeader("Authorization", "Bearer " +
                            sharedPreferences.getString(PREF_ACCESS_TOKEN + userUuid(), "Not stored"))
                    .setBodyParameter("Filename", uploadFileName + ".fit")
                    .setBodyParameter("SetWorkoutPublic", privateShare)
                    .setBodyParameter("Comment", workoutName)
                    .setBodyParameter("UploadClient", "Kinetic Fit")
                    .setBodyParameter("Data", fit64)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                                     @Override
                                     public void onCompleted(Exception e, JsonArray result) {
                                         if (e == null) {
//                                                 Log.d(TAG, result.get(0).getAsJsonObject().toString());
                                             toastUploadResponse(result.get(0).getAsJsonObject());
                                             FitAnalytics.sendShareKPI(TAG);
                                         } else {
                                             Crashlytics.logException(e);
                                         }
                                     }
                                 }
                    );

        } catch (FitRuntimeException e) {

        } catch (IOException e) {
        }
    }

    @UiThread
    protected void toastUploadResponse(JsonObject response) {
        if (!response.has("error")) {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                    "Training Peaks upload successful").show();
        } else {
            new FitSystemNotifications(context, "Training Peaks Error", response.get("error").toString(),
                    ProfileActivity_.class);

        }
    }

    private String encodeFileToBase64Binary(File file) {
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.encodeToString(bytes, 0);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }

    public void autoShareWorkout(String uuid, String workoutName, String uploadFileName) {
        if (getAutoShare()) {
            uploadSession(uuid, workoutName, uploadFileName);
        }
    }
}