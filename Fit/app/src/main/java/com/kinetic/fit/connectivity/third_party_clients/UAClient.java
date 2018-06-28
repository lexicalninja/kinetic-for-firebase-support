package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.goebl.david.Webb;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
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
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.realm.Realm;

@EBean(scope = EBean.Scope.Default)
public class UAClient {

    // TODO: This could be "cleaner"
    public static final String UA_CLIENT_ID = "<api-key>";

    private static final String TAG = "UAClient";

    public static final String STATUS_CHANGED = "UA.CONNECTION_CHANGED";

    private static final String UA_CLIENT_SECRET = "<api-key>";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String PREF_ACCESS_TOKEN = "AccessToken";
    private static final String PREF_SHARE_PUBLIC = "Public";
    private static final String PREF_SHARE_FRIENDS = "Friends";
    private static final String PREF_AUTO_SHARE = "AutoShare";
    private static final String PREF_REFRESH_TOKEN = "RefreshToken";

    private SharedPreferences sharedPreferences;

    public boolean getSharePublic() {
        return sharedPreferences.getBoolean(PREF_SHARE_PUBLIC + userUuid(), false);
    }

    public void setSharePublic(boolean sharePublic) {
        sharedPreferences.edit().putBoolean(PREF_SHARE_PUBLIC + userUuid(), sharePublic).apply();
    }

    public boolean getSharedFriends() {
        return sharedPreferences.getBoolean(PREF_SHARE_FRIENDS + userUuid(), false);

    }

    public void setSharedFriends(boolean autoShare) {
        sharedPreferences.edit().putBoolean(PREF_SHARE_FRIENDS + userUuid(), autoShare).apply();
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
        Webb webb = Webb.create();
        webb.setBaseUri("https://api.ua.com");
        webb.setBaseUri("https://api-ua-com-ttw1r9vp59c0.runscope.net");
        JSONObject apiResult = new JSONObject();
        Log.d(TAG, "authToken: " + authToken);
//        try {
        Future<JsonObject> response = Ion.with(context)
                .load("https://api.ua.com/v7.1/oauth2/access_token/")
                .addHeader("Api-Key", UA_CLIENT_ID)
                .setBodyParameter("grant_type", GRANT_TYPE)
                .setBodyParameter("client_id", UA_CLIENT_ID)
                .setBodyParameter("client_secret", UA_CLIENT_SECRET)
                .setBodyParameter("code", authToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        String accessToken = result.get("access_token").getAsString();
                        String refreshToken = result.get("refresh_token").getAsString();
                        if (accessToken != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(PREF_ACCESS_TOKEN + userUuid(), accessToken);
                            editor.apply();

                            if (refreshToken != null) {
                                editor.putString(PREF_REFRESH_TOKEN + userUuid(), refreshToken);
                                editor.apply();
                            }
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
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                    "MapMyFitness Connected").show();
        } else {
            ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                    "MapMyFitness Connection Error").show();
        }
    }

    @Background
    public void uploadSession(String uuid) {
        Realm realm = Realm.getDefaultInstance();
        Session session = realm.where(Session.class).equalTo("uuid", uuid).findFirst();
        if (session != null) {
            Date date;
            date = session.getWorkoutDate();
            TimeZone tz = Calendar.getInstance().getTimeZone();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            df.setTimeZone(tz);
            String dateAsISO = df.format(date);
            JsonObject upload;
            JsonObject aggregates;
            JsonObject timeSeries;

            boolean hasTimeSeries = getHasTimeSeries(session);

            upload = initUploadString(session, dateAsISO);
            aggregates = setAggregates(session);
            upload.add("aggregates", aggregates);
            upload.addProperty("has_time_series", hasTimeSeries);
            if (hasTimeSeries) {
                timeSeries = setTimeSeries(session);
                upload.add("time_series", timeSeries);
            }
            realm.close();
//                Log.d(TAG, "upload: " + upload);
            Future<JsonObject> response = Ion.with(context)
                    .load("https://api.ua.com/v7.1/workout/")
                    .addHeader("Api-Key", UA_CLIENT_ID)
                    .addHeader("authorization", "Bearer " +
                            sharedPreferences
                                    .getString(PREF_ACCESS_TOKEN + userUuid(), "No Token"))
                    .setJsonObjectBody(upload)
                    .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e == null) {
                                Crashlytics.log(result.toString());
                                toastUploadResponse(result);
                                FitAnalytics.sendShareKPI(TAG);
                            }
                        }
                    });
        }

    }

    @UiThread
    protected void toastUploadResponse(JsonObject response) {
        try {
            if (response.get("is_verified") != null && response.get("is_verified").toString().equals("true")) {
                ViewStyling.getCustomToast(context, ((Activity) context).getLayoutInflater(),
                        "MapMyFitness Upload Successful").show();
            } else {
                if(response.get("validation_failures") != null) {
                    new FitSystemNotifications(context, "MapMyFitness Error",
                            response.get("validation_failures").toString(),
                            ProfileActivity_.class);
                } else {
                    new FitSystemNotifications(context, "MapMyFitness Error (Slide Down for more)",
                            "Please check your connection and try again", ProfileActivity_.class);
                }
            }
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
    }

    public int getPrivacyShare() {
        if (sharedPreferences.getBoolean(PREF_SHARE_PUBLIC + userUuid(), false)) {
            return 3;
        } else if (sharedPreferences.getBoolean(PREF_SHARE_FRIENDS + userUuid(), false)) {
            return 1;
        } else {
            return 0;
        }

    }

    private JsonObject setAggregates(Session session) {
        JsonObject ags = new JsonObject();

        try {
            ags.addProperty("distance_total", session.getDistanceKM() * 1000F);
            ags.addProperty("active_time_total", (int) session.getDuration());
            ags.addProperty("metabolic_energy_total", (int) session.getKilojoules() * 1000);
            ags.addProperty("elapsed_time_total", (int) session.getDuration());

            if (session.getMinHeartRate() > 0) {
                ags.addProperty("heartrate_min", session.getMinHeartRate());
            }
            if (session.getMaxHeartRate() > 0) {
                ags.addProperty("heartrate_max", session.getMaxHeartRate());
            }
            if (session.getAvgHeartRate() > 0) {
                ags.addProperty("heartrate_avg", (int) session.getAvgHeartRate());
            }
            ags.addProperty("speed_max", session.getMaxSpeedKPH() * (1000 / 3600.0F)); //meters per second
            ags.addProperty("speed_avg", session.getAvgSpeedKPH() * (1000 / 3600.0F)); //meters per second
            ags.addProperty("cadence_avg", (int) session.getAvgCadence());
            ags.addProperty("cadence_max", (int) session.getMaxCadence());
            ags.addProperty("power_avg", (int) session.getAvgPower());
            ags.addProperty("power_mac", session.getMaxPower());
        } catch (JsonIOException e) {

        }
        return ags;
    }

    private JsonObject initUploadString(Session session, String dateAsISO) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject.addProperty("name", session.getWorkoutName());
            jsonObject.addProperty("notes", session.getWorkoutDescription());
            jsonObject.addProperty("start_datetime", dateAsISO);
            jsonObject.addProperty("start_locale_timezone", Calendar.getInstance().getTimeZone()
                    .getID());
            jsonObject.addProperty("start_locale_timezone", "US/Central");
            jsonObject.addProperty("privacy", context
                    .getString(R.string.ua_upload_privacy_formatter, getPrivacyShare()));
            jsonObject.addProperty("source", "Kinetic Fit");
            jsonObject.addProperty("activity_type", "/v7.1/activity_type/546/");


        } catch (JsonIOException e) {

        }
        return jsonObject;
    }

    private boolean getHasTimeSeries(Session session) {
        if (session.getDataSlices().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public JsonObject setTimeSeries(Session session) {
        JsonObject ts = new JsonObject();
        JsonArray distances = new JsonArray();
        JsonArray heartrate = new JsonArray();
        JsonArray cadence = new JsonArray();
        JsonArray speeds = new JsonArray();
        JsonArray powers = new JsonArray();
        JsonArray positions = new JsonArray();
        ArrayList<SessionDataSlice> dataSlices = session.getDataSlices();


        for (SessionDataSlice slice : dataSlices) {
            if (slice.accumulatedDistanceKM > 0) {
                JsonArray temp = new JsonArray();
                try {
                    temp.add(slice.timestamp);
                    temp.add(slice.accumulatedDistanceKM);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                }

                distances.add(temp);
            }
            if (slice.currentHeartRate > 0) {
                JsonArray temp = new JsonArray();
                try {
                    temp.add(slice.timestamp);
                    temp.add((double) slice.currentHeartRate);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                }
                heartrate.add(temp);
            }
            if (slice.currentCadence > 0) {
                JsonArray temp = new JsonArray();

                try {
                    temp.add(slice.timestamp);
                    temp.add(slice.currentCadence);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                }
                cadence.add(temp);
            }
            if (slice.currentSpeedKPH > 0) {
                JsonArray temp = new JsonArray();
                try {
                    temp.add(slice.timestamp);
                    temp.add(slice.currentSpeedKPH);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                }
                speeds.add(temp);
            }
            if (slice.currentPower > 0) {
                JsonArray temp = new JsonArray();
                try {
                    temp.add(slice.timestamp);
                    temp.add((double) slice.currentPower);
                } catch (JsonIOException e) {
                    e.printStackTrace();
                }
                powers.add(temp);
            }
            //TODO location data gets added here
        }

        try {
            ts.add("distance", distances);
            Log.d(TAG, "distances: " + distances);
            ts.add("heartrate", heartrate);
            ts.add("power", powers);
            ts.add("cadence", cadence);
            ts.add("speed", speeds);

        } catch (JsonIOException e) {
            e.printStackTrace();
        }

        return ts;
    }

    public void autoShareWorkout(String uuid) {
        if (getAutoShare()) {
            uploadSession(uuid);
        }
    }
}