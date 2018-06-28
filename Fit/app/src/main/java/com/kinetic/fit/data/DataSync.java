package com.kinetic.fit.data;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SignUpEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient_;
import com.kinetic.fit.connectivity.third_party_clients.GoogleClient;
import com.kinetic.fit.connectivity.third_party_clients.GoogleClient_;
import com.kinetic.fit.data.realm_objects.Category;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.realm_objects.SubscriptionAddOn;
import com.kinetic.fit.data.realm_objects.Tag;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanDay;
import com.kinetic.fit.data.realm_objects.TrainingPlanProgress;
import com.kinetic.fit.data.realm_objects.Video;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;
import com.kinetic.fit.ui.login.LoginDispathActivity;
import com.koushikdutta.async.future.FutureCallback;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

@EService
public class DataSync extends Service {

    Realm realm;
    public static final String REFRESH_COMPLETE = "DataSync.REFRESH_COMPLETE";

    RealmResults<Profile> mProfile;
    RealmResults<Session> mSessions;
    RealmResults<Session> badSessions;

    public interface LogInCallback {
        void complete(int code);
    }

    public interface ResetPasswordCallback {
        void complete(int code);
    }

    public interface SignUpCallback {
        void complete(int code);
    }

    private static final String TAG = "DataSync";


    public static void initialize(Context context) {

    }

    private KineticAPI mKineticAPI;

    public class DataSyncBinder extends Binder {

        public void syncProfile() {
            syncProfileWithParse(Profile.current());
        }

        public Session createSession() {
            final Session session = new Session();
            Session backed = realm.copyToRealm(session);
            backed.setParseFlag(Session.CREATE_FLAG);
            mKineticAPI.createOrUpdateParseObject(Session.CLASS_NAME, null, backed.serializeToJson(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (e == null) {
                        session.setObjectId(result.get("objectId").getAsString());
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                session.setParseFlag(Session.OK_FLAG);
                                realm.copyToRealmOrUpdate(session);
                            }
                        });
                    } else {
                        Crashlytics.logException(e);
                    }
                }
            });
            return session;
        }

        public void deleteSession(final Session session) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    session.setParseFlag(Session.DELETE_FLAG);
                }
            });
            mKineticAPI.deleteParseObject(Session.CLASS_NAME, session.getObjectId(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (e == null && session.isManaged()) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                session.deleteFromRealm();
                            }
                        });
                    } else {
//                        TODO handle Parse Server errors here and resave or notify
                        Crashlytics.logException(e);
                    }
                }
            });
        }

        public void refreshSubscriptions() {
            refreshAllSubscriptions();
        }

        public void refreshSubscriptions2(FutureCallback<JsonObject> callback) {
            refreshAllSubscriptions2(callback);
        }

        public void saveOrUpdateSession(final Session session) {
            final Session temp;
            temp = realm.copyToRealmOrUpdate(session);
            if (temp.isValid() && temp.getObjectId() != null && (temp.getParseFlag() == Session.OK_FLAG)) {
                temp.setParseFlag(Session.UPDATE_FLAG);
                mKineticAPI.createOrUpdateParseObject(Session.CLASS_NAME, temp.getObjectId(), temp.serializeToJson(), new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            realm.beginTransaction();
                            temp.setParseFlag(Session.OK_FLAG);
                            realm.copyToRealmOrUpdate(temp);
                            realm.commitTransaction();
                        } else {
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void saveImportedWorkout(final Workout w, final FutureCallback<JsonObject> callback){
            mKineticAPI.createOrUpdateParseObject(Workout.CLASS_NAME, null, w.serializeToJson(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, final JsonObject result) {
                    callback.onCompleted(e, result);
                }
            });
        }

        public void authenticate(String username, String password, final LogInCallback callback) {
            // is logged in?
            mKineticAPI.authenticate(username, password, new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (e == null) {
                        if (result != null) {
                            if (result.get("code") != null) {
                                int code = result.get("code").getAsInt();
                                callback.complete(code);
                            } else {
                                Crashlytics.log("DataSync Authenticate response: " + result.toString());
                                realm.beginTransaction();
                                Profile profile = new Profile(result, realm);
                                realm.copyToRealmOrUpdate(profile);
                                realm.commitTransaction();
                                mKineticAPI.setSessionToken(profile.getSessionToken());
                                callback.complete(200);
                            }
                        }
                    } else {
                        Crashlytics.log(TAG + " authenticate");
                        Crashlytics.logException(e);
                    }

                }
            });
        }

        public void resetPassword(String username, final ResetPasswordCallback callback) {
            mKineticAPI.resetPassword(username, new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (result != null) {
                        if (result.get("code") != null) {
                            int code = result.get("code").getAsInt();
                            callback.complete(code);
                        } else {
                            callback.complete(200);
                        }
                    }
                }
            });
        }

        public void signup(String email, String password, String name, final SignUpCallback callback) {
            mKineticAPI.signup(email, password, name, new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if (result != null) {
                        if (result.get("username") == null) {
                            callback.complete(400);
                        } else {
                            realm.beginTransaction();
                            Profile profile = new Profile(result);
                            profile = realm.copyToRealmOrUpdate(profile);
                            realm.commitTransaction();
                            mKineticAPI.setSessionToken(profile.getSessionToken());
                            Answers.getInstance().logSignUp(new SignUpEvent().putSuccess(true));
                            callback.complete(200);
                        }
                    }
                }
            });
        }

        public void refreshAll(boolean force) {
//            RealmResults<Session> unSynced = realm.where(Session.class).notEqualTo("parseFlag", Session.OK_FLAG).findAll();
//            for (Session s : unSynced) {
//                if (s.getParseFlag() == Session.DELETE_FLAG) {
//                    deleteSession(s);
//                } else {
//                    saveOrUpdateSession(s);
//                }
//            }
//            mKineticAPI.fetchObjects(Category.CLASS_NAME, null, null, 100, null, null, new FutureCallback<JsonObject>() {
//                @Override
//                public void onCompleted(Exception e, JsonObject result) {
//                    if (e == null) {
//                        if (result.get("code") == null) {
//                            realm.beginTransaction();
//                            for (JsonElement j : result.getAsJsonArray("results")) {
//                                realm.copyToRealmOrUpdate(new Category((JsonObject) j, realm));
//                            }
//                            realm.commitTransaction();
//                        } else if (result.get("code").getAsInt() == 209) {
//                            logOut(true);
//                            return;
//                        }
//                    } else {
//                        Crashlytics.logException(e);
//                    }
//                    mKineticAPI.fetchObjects(Workout.CLASS_NAME, "duration", true, 1000, null, null, new FutureCallback<JsonObject>() {
//                        @Override
//                        public void onCompleted(Exception e, final JsonObject result) {
//                            if (e == null) {
//                                realm.executeTransaction(new Realm.Transaction() {
//                                    @Override
//                                    public void execute(Realm realm) {
//                                        for (JsonElement j : result.getAsJsonArray("results")) {
//                                            realm.copyToRealmOrUpdate(new Workout((JsonObject) j, realm));
//                                        }
//                                    }
//                                });
//                            } else {
//                                Crashlytics.logException(e);
//                            }
//                            mKineticAPI.fetchObjects("Session", null, null, 500, null, null, new FutureCallback<JsonObject>() {
//                                @Override
//                                public void onCompleted(Exception e, final JsonObject result) {
//                                    if (e == null) {
//                                        realm.executeTransaction(new Realm.Transaction() {
//                                            @Override
//                                            public void execute(Realm realm) {
//                                                for (JsonElement j : result.getAsJsonArray("results")) {
//                                                    Session temp = realm.copyToRealmOrUpdate(new Session((JsonObject) j));
//                                                    if (((JsonObject) j).get("uuid") == null) {
//                                                        JsonObject update = new JsonObject();
//                                                        update.addProperty("uuid", temp.getUuid());
//                                                        mKineticAPI.createOrUpdateParseObject(Session.getClassName(), ((JsonObject) j)
//                                                                .get("objectId").getAsString(), update, new FutureCallback<JsonObject>() {
//                                                            @Override
//                                                            public void onCompleted(Exception e, JsonObject result) {
//                                                                if (e != null) {
////                                                            Log.e(TAG, e.getLocalizedMessage());
//                                                                    Crashlytics.logException(e);
//                                                                }
//                                                            }
//                                                        });
//                                                    }
//                                                }
//                                            }
//                                        });
//                                    } else {
//                                        Crashlytics.logException(e);
//                                    }
//                                    realm.executeTransaction(new Realm.Transaction() {
//                                        @Override
//                                        public void execute(Realm realm) {
//                                            Tag allWorkouts = new Tag("All Workouts");
//                                            RealmResults<Workout> results = realm.where(Workout.class).findAll();
//                                            Category all = realm.where(Category.class).equalTo("name", "All Workouts").findFirst();
//                                            for (Workout w : results) {
//                                                allWorkouts.addWorkout(w);
//                                            }
//                                            allWorkouts.addCategory(all);
//                                            all.addTag(allWorkouts);
//                                            realm.copyToRealmOrUpdate(allWorkouts);
//                                            realm.copyToRealmOrUpdate(all);
//                                            Profile.current().fetchWorkoutsFromRealm(realm);
//                                            Log.d(TAG, "TAGS DONE");
//                                        }
//                                    });
//                                    mKineticAPI.sendFunctionToParse("getUserSubscriptions2", null, new FutureCallback<JsonObject>() {
//                                        @Override
//                                        public void onCompleted(Exception e, final JsonObject result) {
//                                            if (e == null) {
//                                                realm.executeTransaction(new Realm.Transaction() {
//                                                    @Override
//                                                    public void execute(Realm realm) {
//                                                        RealmResults<Subscription> results = realm.where(Subscription.class)
//                                                                .findAll();
//                                                        results.deleteAllFromRealm();
//                                                        if (result.get("result") != null) {
//                                                            for (JsonElement j : result.getAsJsonObject("result").getAsJsonArray("subscriptions")) {
//                                                                Subscription s = new Subscription((JsonObject) j);
//                                                                realm.copyToRealmOrUpdate(s);
//                                                            }
//                                                        }
//                                                    }
//                                                });
//                                            } else {
//                                                Crashlytics.logException(e);
//                                            }
//                                            mKineticAPI.fetchObjects("TrainingPlan", null, null, 1000, null, null, new FutureCallback<JsonObject>() {
//                                                @Override
//                                                public void onCompleted(Exception e, final JsonObject result) {
//                                                    if (e == null) {
//                                                        realm.executeTransaction(new Realm.Transaction() {
//                                                            @Override
//                                                            public void execute(Realm realm) {
//                                                                for (JsonElement j : result.getAsJsonArray("results")) {
//                                                                    TrainingPlan tp = new TrainingPlan((JsonObject) j);
//                                                                    realm.copyToRealmOrUpdate(tp);
//                                                                }
//                                                                Log.d(TAG, "TP DONE");
//                                                            }
//                                                        });
//                                                    } else {
//                                                        Crashlytics.logException(e);
//                                                    }
//                                                    mKineticAPI.fetchObjects(TrainingPlanDay.CLASS_NAME, null, null, 2000, null, null, new FutureCallback<JsonObject>() {
//                                                        @Override
//                                                        public void onCompleted(Exception e, final JsonObject result) {
//                                                            if (e == null) {
//                                                                realm.executeTransaction(new Realm.Transaction() {
//                                                                    @Override
//                                                                    public void execute(Realm realm) {
//                                                                        for (JsonElement j : result.getAsJsonArray("results")) {
//                                                                            TrainingPlanDay tpd = new TrainingPlanDay((JsonObject) j, realm);
//                                                                            realm.copyToRealmOrUpdate(tpd);
//                                                                        }
//                                                                    }
//                                                                });
//                                                            } else {
//                                                                Crashlytics.logException(e);
////                                                Log.e(TAG, e.getLocalizedMessage());
//                                                            }
//                                                            mKineticAPI.fetchObjects(TrainingPlanProgress.CLASS_NAME, null, null, 1000, null, null, new FutureCallback<JsonObject>() {
//                                                                @Override
//                                                                public void onCompleted(Exception e, final JsonObject result) {
//                                                                    if (e == null) {
//                                                                        realm.executeTransaction(new Realm.Transaction() {
//                                                                            @Override
//                                                                            public void execute(Realm realm) {
//                                                                                for (JsonElement j : result.getAsJsonArray("results")) {
//                                                                                    TrainingPlanProgress tpp = new TrainingPlanProgress((JsonObject) j, realm);
//                                                                                    TrainingPlanProgress temp = realm.copyToRealmOrUpdate(tpp);
//                                                                                    if (((JsonObject) j).get("uuid") == null) {
//                                                                                        JsonObject update = new JsonObject();
//                                                                                        update.addProperty("uuid", temp.getUuid());
//                                                                                        mKineticAPI.createOrUpdateParseObject(TrainingPlanProgress.CLASS_NAME, ((JsonObject) j)
//                                                                                                .get("objectId").getAsString(), update, new FutureCallback<JsonObject>() {
//                                                                                            @Override
//                                                                                            public void onCompleted(Exception e, JsonObject result) {
//                                                                                                if (e != null) {
//                                                                                                    Crashlytics.logException(e);
//                                                                                                }
//                                                                                            }
//                                                                                        });
//                                                                                    }
//                                                                                }
//
//                                                                            }
//                                                                        });
//                                                                    } else {
//                                                                        Crashlytics.logException(e);
//                                                                    }
//                                                                    mKineticAPI.fetchObjects(Video.CLASS_NAME, null, null, 1000, null, null, new FutureCallback<JsonObject>() {
//                                                                        @Override
//                                                                        public void onCompleted(Exception e, final JsonObject result) {
//                                                                            if (e == null) {
//                                                                                realm.executeTransaction(new Realm.Transaction() {
//                                                                                    @Override
//                                                                                    public void execute(Realm realm) {
//                                                                                        if (result.getAsJsonArray("results") != null) {
//                                                                                            for (JsonElement j : result.getAsJsonArray("results")) {
//                                                                                                Video vid = new Video((JsonObject) j);
//                                                                                                realm.copyToRealmOrUpdate(vid);
//                                                                                            }
//                                                                                        }
//                                                                                    }
//                                                                                });
//                                                                            } else {
//                                                                                Crashlytics.logException(e);
//                                                                            }
//                                                                            mKineticAPI.sendFunctionToParse("getSubAddOns", null, new FutureCallback<JsonObject>() {
//                                                                                @Override
//                                                                                public void onCompleted(Exception e, final JsonObject result) {
//                                                                                    if (e == null) {
//                                                                                        if (!result.getAsJsonArray("result").isJsonNull()) {
//                                                                                            realm.executeTransaction(new Realm.Transaction() {
//                                                                                                @Override
//                                                                                                public void execute(Realm realm) {
//                                                                                                    RealmResults<SubscriptionAddOn> results = realm.where(SubscriptionAddOn.class)
//                                                                                                            .findAll();
//                                                                                                    results.deleteAllFromRealm();
//                                                                                                    for (JsonElement item : result.getAsJsonArray("result")) {
//                                                                                                        SubscriptionAddOn addOn = new SubscriptionAddOn((JsonObject) item);
//                                                                                                        realm.copyToRealmOrUpdate(addOn);
//                                                                                                    }
//                                                                                                }
//                                                                                            });
//                                                                                        }
//                                                                                    }
//                                                                                }
//                                                                            });
//                                                                            mKineticAPI.getKineticYouTubePlaylists(new FutureCallback<JsonObject>() {
//                                                                                @Override
//                                                                                public void onCompleted(Exception e, final JsonObject result) {
//                                                                                    if (e == null) {
//                                                                                        realm.executeTransaction(new Realm.Transaction() {
//                                                                                            @Override
//                                                                                            public void execute(Realm realm) {
//                                                                                                realm.delete(YouTubeVideo.class);
//                                                                                                for (JsonElement e : result.getAsJsonArray("items")) {
//                                                                                                    YouTubeVideo vid = new YouTubeVideo((JsonObject) e);
//                                                                                                    realm.copyToRealmOrUpdate(vid);
//                                                                                                }
//                                                                                            }
//                                                                                        });
//                                                                                    } else {
//                                                                                        Crashlytics.logException(e);
//                                                                                    }
//                                                                                    SharedPreferences sharedPreferences = getSharedPreferences(GoogleClient.TAG, Context.MODE_PRIVATE);
//                                                                                    String refreshToken = sharedPreferences.getString("RefreshToken" + Profile.getUUID(), null);
//                                                                                    if (refreshToken != null) {
//                                                                                        GoogleClient gc = GoogleClient_.getInstance_(getBaseContext());
//                                                                                        gc.refreshToken(new FutureCallback<JsonObject>() {
//                                                                                            @Override
//                                                                                            public void onCompleted(Exception e, JsonObject result) {
//                                                                                                if (e == null) {
//                                                                                                    String token = result.get("access_token").getAsString();
//                                                                                                    mKineticAPI.getPersonalYouTubePlaylists(token, new FutureCallback<JsonObject>() {
//                                                                                                        @Override
//                                                                                                        public void onCompleted(Exception e, final JsonObject result) {
//                                                                                                            if (e == null) {
//                                                                                                                if (result != null && result.get("error") == null) {
//                                                                                                                    realm.executeTransaction(new Realm.Transaction() {
//                                                                                                                        @Override
//                                                                                                                        public void execute(Realm realm) {
//                                                                                                                            for (JsonElement e : result.getAsJsonArray("items")) {
//                                                                                                                                YouTubeVideo vid = new YouTubeVideo((JsonObject) e);
//                                                                                                                                realm.copyToRealmOrUpdate(vid);
//                                                                                                                            }
//                                                                                                                        }
//                                                                                                                    });
//                                                                                                                }
//                                                                                                            } else {
//                                                                                                                Crashlytics.logException(e);
//                                                                                                            }
//                                                                                                            finalizeRefresh();
//                                                                                                        }
//                                                                                                    });
//                                                                                                } else {
//                                                                                                    Crashlytics.logException(e);
//                                                                                                }
//                                                                                            }
//                                                                                        });
//
//                                                                                    } else {
                                                                                        finalizeRefresh();
//                                                                                    }
//                                                                                }
//                                                                            });
//                                                                        }
//                                                                    });
//                                                                }
//                                                            });
//                                                        }
//                                                    });
//                                                }
//                                            });
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    });
//                }
//            });
        }

        private void finalizeRefresh() {
            updateDropboxVideos();
            deleteBadSession();
            addChangeListeners();
            while (realm.isInTransaction()) {
            }
            sendBroadcast(new Intent(REFRESH_COMPLETE));
        }

        public void sendTrialParseFunction(String deviceId, String function, FutureCallback<JsonObject> callback) {
            mKineticAPI.sendTrialFunctionToParse(deviceId, function, callback);
        }

        public void createNewTrainingPlanProgress(TrainingPlanProgress tpp, final FutureCallback<JsonObject> callback) {
            mKineticAPI.createOrUpdateParseObject(TrainingPlanProgress.CLASS_NAME, null, tpp.toJsonObject(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    callback.onCompleted(e, result);
                }
            });
        }

        public void updateTrainingPlanProgress(TrainingPlanProgress tpp, final FutureCallback<JsonObject> callback) {
            mKineticAPI.createOrUpdateParseObject(TrainingPlanProgress.CLASS_NAME, tpp.getObjectId(), tpp.toJsonObject(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    callback.onCompleted(e, result);
                }
            });
        }

        public void subscribe(JsonObject params, final FutureCallback<JsonObject> callback) {
            completeSubscriptionPurchase(params, callback);
        }

        public void updateCustomWorkout(Workout workout){
            mKineticAPI.createOrUpdateParseObject(Workout.CLASS_NAME, workout.getObjectId(), workout.serializeToJson(), new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    if(e != null){
                        Crashlytics.logException(e);
                    }
                }
            });
        }
    }

    private final DataSyncBinder mBinder = new DataSyncBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    SharedPreferences mPreferences;
    SharedPreferences.Editor mPreferencesEditor;

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getDefaultInstance();
        mKineticAPI = new KineticAPI(this);
        if (Profile.current() != null) {
            mKineticAPI.setSessionToken(Profile.getCurrentSession());
        } else {
            mKineticAPI.setSessionToken(null);
        }

        mPreferences = getSharedPreferences(TAG, 0);
        mPreferencesEditor = mPreferences.edit();

//        TODO put the listeners on this hog right here
        mSessions = realm.where(Session.class).findAll();
        mProfile = realm.where(Profile.class).findAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    @Receiver(actions = Profile.LOGGED_OUT)
    protected void onLogOut() {
        logOut(false);
    }

    private void logOut(boolean badToken) {
        mKineticAPI.setSessionToken(null);
        mBinder.syncProfile();
        mProfile.removeAllChangeListeners();
        // delete everything
//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        realm.deleteAll();
//        realm.commitTransaction();
//        realm.close();
        Intent i = new Intent(this, LoginDispathActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("badToken", badToken);
        startActivity(i);
    }

    private void syncProfileWithParse(Profile profile) {
        mKineticAPI.createOrUpdateParseObject(Profile.CLASS_NAME, profile.getObjectId(), Profile.serializeToJson(profile), new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e == null) {
                    Log.d(TAG, result.toString());
                } else {
                    Crashlytics.logException(e);
//                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    private void addChangeListeners() {
        mProfile.addChangeListener(new RealmChangeListener<RealmResults<Profile>>() {
            @Override
            public void onChange(RealmResults<Profile> results) {
                if (Profile.current() != null) {
                    mBinder.syncProfile();
                }
            }
        });
    }

    private void completeSubscriptionPurchase(JsonObject params, final FutureCallback<JsonObject> callback) {
        mKineticAPI.sendFunctionToParse("subscribeGoogleIAP", params, new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e == null) {
                    callback.onCompleted(e, result);
                } else {
//                    Log.e(TAG, e.getLocalizedMessage());
                    Crashlytics.logException(e);
                }
            }
        });
    }

    private void refreshAllSubscriptions() {

        mKineticAPI.sendFunctionToParse("getUserSubscriptions", null, new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, final JsonObject result) {
                if (e == null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<Subscription> results = realm.where(Subscription.class)
                                    .findAll();
                            results.deleteAllFromRealm();
                            if (result.get("result") != null) {
                                for (JsonElement j : result.getAsJsonObject("result").getAsJsonArray("subscriptions")) {
                                    Subscription s = new Subscription((JsonObject) j);
                                    realm.copyToRealmOrUpdate(s);
                                }
                            }
                        }
                    });
                } else {
                    Crashlytics.logException(e);
                }
            }
        });
    }

    private void refreshAllSubscriptions2(final FutureCallback<JsonObject> callback) {
        mKineticAPI.sendFunctionToParse("getUserSubscriptions2", null, new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, final JsonObject result) {
                if (e == null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmResults<Subscription> results = realm.where(Subscription.class)
                                    .findAll();
                            results.deleteAllFromRealm();
                            if (result.get("result") != null) {
                                for (JsonElement j : result.getAsJsonObject("result").getAsJsonArray("subscriptions")) {
                                    Subscription s = new Subscription((JsonObject) j);
                                    realm.copyToRealmOrUpdate(s);
                                }
                            }
                        }
                    });
                } else {
                    Crashlytics.logException(e);
                }
                mKineticAPI.fetchObjects("TrainingPlan", null, null, 1000, null, null, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, final JsonObject result) {
                        if (e == null) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    for (JsonElement j : result.getAsJsonArray("results")) {
                                        TrainingPlan tp = new TrainingPlan((JsonObject) j);
                                        realm.copyToRealmOrUpdate(tp);
                                    }
                                    Log.d(TAG, "TP DONE");
                                }
                            });
                        } else {
                            Crashlytics.logException(e);
                        }
                        mKineticAPI.fetchObjects(TrainingPlanDay.CLASS_NAME, null, null, 2000, null, null, new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, final JsonObject result) {
                                if (e == null) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            for (JsonElement j : result.getAsJsonArray("results")) {
                                                TrainingPlanDay tpd = new TrainingPlanDay((JsonObject) j, realm);
                                                realm.copyToRealmOrUpdate(tpd);
                                            }
                                        }
                                    });
                                } else {
                                    Crashlytics.logException(e);
//                                                Log.e(TAG, e.getLocalizedMessage());
                                }
                                mKineticAPI.fetchObjects(Video.CLASS_NAME, null, null, 1000, null, null, new FutureCallback<JsonObject>() {
                                    @Override
                                    public void onCompleted(Exception e, final JsonObject result) {
                                        if (e == null) {
                                            realm.executeTransaction(new Realm.Transaction() {
                                                @Override
                                                public void execute(Realm realm) {
                                                    if (result.getAsJsonArray("results") != null) {
                                                        for (JsonElement j : result.getAsJsonArray("results")) {
                                                            Video vid = new Video((JsonObject) j);
                                                            realm.copyToRealmOrUpdate(vid);
                                                        }
                                                    }
                                                }
                                            });
                                        } else {
                                            Crashlytics.logException(e);
                                        }
                                        callback.onCompleted(e, new JsonObject());
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    void deleteBadSession() {
        /**
         * DELETE ANY SESSIONS FROM CRASHES
         **/
        badSessions = realm.where(Session.class)
                .beginGroup()
                .isNull("workoutName")
                .or()
                .isNull("workoutDescription")
                .endGroup()
                .findAll();
        for (final Session s : badSessions) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(final Realm realm) {
                    mKineticAPI.deleteParseObject(Session.CLASS_NAME, s.getObjectId(), new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e == null) {
                                if (realm.isInTransaction()) {
                                    s.deleteFromRealm();
                                } else {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            s.deleteFromRealm();
                                        }
                                    });
                                }
                            } else {
                                Crashlytics.logException(e);
                            }
                        }
                    });
                }
            });
        }
    }

    void updateDropboxVideos(){
        DropboxClient client = DropboxClient_.getInstance_(this);
        if(client.isConnected()) {
            client.discoverVideos();
        }
    }
}
