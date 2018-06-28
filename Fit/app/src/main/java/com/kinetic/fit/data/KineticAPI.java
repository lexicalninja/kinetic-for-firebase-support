package com.kinetic.fit.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.google.api.client.http.HttpMethods;
import com.google.gson.JsonObject;
import com.kinetic.fit.connectivity.third_party_clients.GoogleAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.GoogleClient;
import com.kinetic.fit.data.realm_objects.Profile;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.HeadersCallback;
import com.koushikdutta.ion.HeadersResponse;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class KineticAPI {
    private static final String TAG = "KinAPI";
    private static final String SERVER_URL = "<api-key>";
//                private static final String SERVER_URL = "https://staging.kinetic.fit";
    private static final String APP_ID = "KineticFit";
    private static final String REST_API_KEY = "<api-key>";

    private String mSessionToken;
    private Context mContext;

    KineticAPI(Context context) {
        mContext = context;
    }


    Future<JsonObject> authenticate(String username, String password, final FutureCallback<JsonObject> callback) {
        JsonObject params = new JsonObject();
        params.addProperty("username", username);
        params.addProperty("password", password);

        return Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/app/login")
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setJsonObjectBody(params)
                .asJsonObject().setCallback(callback);
    }

    Future<JsonObject> resetPassword(String username, final FutureCallback<JsonObject> callback) {
        JsonObject params = new JsonObject();
        params.addProperty("username", username);

        return Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/app/password/reset")
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setJsonObjectBody(params)
                .asJsonObject().setCallback(callback);
    }

    Future<JsonObject> signup(String email, String password, String name, final FutureCallback<JsonObject> callback) {
        JsonObject params = new JsonObject();
        params.addProperty("username", email);
        params.addProperty("password", password);
        params.addProperty("name", name);

        return Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/app/signup")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setJsonObjectBody(params)
                .asJsonObject().setCallback(callback);
    }


    public boolean loggedIn() {
        return mSessionToken != null;
    }

    public void logout() {
        setSessionToken(null);
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults results = realm.where(Profile.class).findAll();
                results.deleteAllFromRealm();
            }
        });
        realm.close();
    }

    public void setSessionToken(@Nullable String sessionToken) {
        mSessionToken = sessionToken;
    }

    public interface KineticObject {
        String getClassName();

        <T extends RealmObject> T getRealmFromJson(JsonObject jsonObject);
    }

    public Future<JsonObject> fetchObjects(String className,
                                           @Nullable String sortKey,
                                           @Nullable Boolean ascending,
                                           @Nullable Integer limit,
                                           @Nullable Integer skip,
                                           @Nullable String[] keys,
                                           final FutureCallback<JsonObject> callback) {

        Builders.Any.B builder = Ion.with(mContext).load(HttpMethods.GET, SERVER_URL + "/parse/classes/" + className)
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("X-Parse-Session-Token", mSessionToken);
        if (limit != null) {
            builder.setBodyParameter("limit", String.valueOf(limit));
        }
        if (skip != null) {
            builder.setBodyParameter("skip", String.valueOf(skip));
        }
        if (sortKey != null) {
            if (ascending != null && ascending == false) {
                builder.setBodyParameter("order", "-" + sortKey);
            } else {
                builder.setBodyParameter("order", sortKey);
            }
        }
        if (keys != null) {
            builder.setBodyParameter("keys", TextUtils.join(",", keys));
        }
        return builder.asJsonObject().setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
//                Log.d("Results: ", result.toString());
                callback.onCompleted(e, result);
            }
        });

    }

    public Future<JsonObject> createOrUpdateParseObject(String className, @Nullable String parseObjId, JsonObject object, final FutureCallback<JsonObject> callback) {

        if (parseObjId != null) {
            return Ion.with(mContext).load(HttpMethods.PUT, SERVER_URL + "/parse/classes/" + className + "/" + parseObjId)
                    .addHeader("X-Parse-Application-Id", APP_ID)
                    .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Parse-Session-Token", mSessionToken)
                    .setJsonObjectBody(object)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            callback.onCompleted(e, result);
                        }
                    });
        } else {
            return Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/parse/classes/" + className)
                    .addHeader("X-Parse-Application-Id", APP_ID)
                    .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Parse-Session-Token", mSessionToken)
                    .setJsonObjectBody(object)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            callback.onCompleted(e, result);
                        }
                    });
        }
    }

    Future<JsonObject> deleteParseObject(String className, String parseId, final FutureCallback<JsonObject> callback) {
        return Ion.with(mContext).load(HttpMethods.DELETE, SERVER_URL + "/parse/classes/" + className + "/" + parseId)
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Parse-Session-Token", mSessionToken)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });
    }

    Future<JsonObject> sendTrialFunctionToParse(String deviceId, String function, final FutureCallback<JsonObject> callback) {
        JsonObject j = new JsonObject();
        j.addProperty("systemId", deviceId);
        return Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/parse/functions/" + function)
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Parse-Session-Token", mSessionToken)
                .setJsonObjectBody(j)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });
    }

    Future<JsonObject> sendFunctionToParse(String function, @Nullable JsonObject parameters, final FutureCallback<JsonObject> callback) {

        Builders.Any.B builder = Ion.with(mContext).load(HttpMethods.POST, SERVER_URL + "/parse/functions/" + function)
                .addHeader("X-Parse-Application-Id", APP_ID)
                .addHeader("X-Parse-REST-API-Key", REST_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Parse-Session-Token", mSessionToken);
        if (parameters != null) {
            builder.setJsonObjectBody(parameters);
        }
        return builder.asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });
    }

    Future<String> getYouTubeToken(String refreshToken){
        return null;
    }

    Future<JsonObject> getKineticYouTubePlaylists(final FutureCallback<JsonObject> callback) {
        return Ion.with(mContext).load(HttpMethods.GET, "https://www.googleapis.com/youtube/v3/playlists?channelId=UCxHRaVtCSn2TtI6YwIEasHQ&part=snippet&key=AIzaSyDB9b6JuUE6cxKeHhxyXijn1F3rm4ke134")
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });

    }


    Future<JsonObject> getPersonalYouTubePlaylists(String token, final FutureCallback<JsonObject> callback) {
        return Ion.with(mContext).load(HttpMethods.GET, "https://www.googleapis.com/youtube/v3/playlists?mine=true&part=snippet")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", "Bearer " + token)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        callback.onCompleted(e, result);
                    }
                });
    }
}
