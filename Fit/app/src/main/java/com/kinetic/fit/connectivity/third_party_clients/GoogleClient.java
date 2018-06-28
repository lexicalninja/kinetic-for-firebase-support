package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.json.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.util.ViewStyling;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.json.JSONArray;

import retrofit2.http.POST;

import static com.kinetic.fit.connectivity.third_party_clients.GoogleAuthActivity.GOOGLE_CLIENT_ID;
import static com.kinetic.fit.connectivity.third_party_clients.GoogleAuthActivity.REDIRECT_URI;

/**
 * Created by Saxton on 7/31/17.
 */

@EBean(scope = EBean.Scope.Default)
public class GoogleClient {

    public static final String TAG = "GoogleClient";
    public static final String STATUS_CHANGED = "Google.CONNECTION_CHANGED";
    private static final String GOOGLE_CLIENT_SECRET = "<api-key>";
    private static final String PREF_REFRESH_TOKEN = "RefreshToken";
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
        return sharedPreferences.getString(PREF_REFRESH_TOKEN + userUuid(), null) != null;
    }

    public void disconnect() {
        sharedPreferences.edit().remove(PREF_REFRESH_TOKEN + userUuid()).commit();
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
                .load(HttpMethods.POST, "https://www.googleapis.com/oauth2/v4/token")
                .setBodyParameter("client_id", GOOGLE_CLIENT_ID)
                .setBodyParameter("code", authToken)
                .setBodyParameter("grant_type", "authorization_code")
                .setBodyParameter("redirect_uri", REDIRECT_URI)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            String accessToken = result.get("refresh_token").getAsString();
                            if (accessToken != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(PREF_REFRESH_TOKEN + userUuid(), accessToken);
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
//        if (success) {
//            ViewStyling.getCustomToast(context.getApplicationContext(), ((Activity) context).getLayoutInflater(), "Google Connected").show();
//        } else {
//            ViewStyling.getCustomToast(((Activity) context), ((Activity) context).getLayoutInflater(), "Google Connection Error").show();
//        }
    }

    public String getRefreshURL(){
        String token = sharedPreferences.getString(PREF_REFRESH_TOKEN + userUuid(), null);
        if(token != null) {
            return "https://www.googleapis.com/oauth2/v4/token?client_id=" + GOOGLE_CLIENT_ID
                    +"&client_secret=" + GOOGLE_CLIENT_SECRET
                    + "&refresh_token=" + token
                    + "&grant_type=refresh_token";
        } else {
            return null;
        }
    }


    public Future<JsonObject> refreshToken(final FutureCallback<JsonObject> callback){
        String token = sharedPreferences.getString(PREF_REFRESH_TOKEN + userUuid(), null);
        return  Ion.with(context).load(HttpMethods.POST, "https://www.googleapis.com/oauth2/v4/token")
                .setBodyParameter("client_id", GOOGLE_CLIENT_ID)
                .setBodyParameter("refresh_token", token)
                .setBodyParameter("grant_type", "refresh_token")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });
    }
}
