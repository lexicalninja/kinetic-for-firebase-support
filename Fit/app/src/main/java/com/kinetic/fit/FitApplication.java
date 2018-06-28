package com.kinetic.fit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.SensorScanner_;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.util.subscription.FitSubscription_;
import com.kinetic.sdk.KineticSDK;
import com.kinetic.sdk.exceptions.APIKeyException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
//import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import net.danlew.android.joda.JodaTimeAndroid;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import io.fabric.sdk.android.Fabric;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

@EApplication
public class FitApplication extends MultiDexApplication {

    private static final String TAG = "FitApplication";

    public static final String APPLICATION_IN_BACKGROUND = "FitApplication.APPLICATION_IN_BACKGROUND";
    public static final String APPLICATION_IN_FOREGROUND = "FitApplication.APPLICATION_IN_FOREGROUND";
    public static final String APP_VERSION = "appVersion";
    private static final int REALM_SCHEMA_VERSION = 6; //increment every time there is a realm change

    SharedPreferences sharedPreferences;
    @Bean
    VideoController videoController;
    private int mActivityCount = 0;

    private void activityStarted() {
        mActivityCount++;
        if (mActivityCount == 1) {
            sendBroadcast(new Intent(APPLICATION_IN_FOREGROUND));
        }
    }

    private void activityStopped() {
        mActivityCount--;
        if (mActivityCount <= 0) {
            mActivityCount = 0;
            sendBroadcast(new Intent(APPLICATION_IN_BACKGROUND));
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics, twitter kit.
        TwitterAuthConfig authConfig =
                new TwitterAuthConfig("1IItYeyWa3fPPYrT0jTTB1xGO",
                        "bRkxAxsGbFhQ5wJYvh5qRi3SQUX6PA8dbWvwAWO0Cq8eEixBQZ");
        Fabric.with(this, crashlyticsKit, new TwitterCore(authConfig), new TweetComposer(), new Twitter(authConfig));

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(REALM_SCHEMA_VERSION) // Must be bumped above when the schema changes
                //use delete for development and create for production
//                .deleteRealmIfMigrationNeeded()
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
                        RealmSchema schema = realm.getSchema();
                        if (oldVersion == 0) {
                            schema.create("Subscription")
                                    .addField("uuid", String.class, FieldAttribute.PRIMARY_KEY)
                                    .addField("planName", String.class)
                                    .addField("planInterval", String.class)
                                    .addField("group", String.class)
                                    .addField("role", String.class)
                                    .addField("status", String.class)
                                    .addField("cancelled", boolean.class)
                                    .addField("periodEnd", double.class);

                            schema.create("SubscriptionAddOn")
                                    .addField("name", String.class)
                                    .addField("sku", String.class, FieldAttribute.PRIMARY_KEY)
                                    .addField("imageUrl", String.class)
                                    .addField("color", String.class)
                                    .addField("price", int.class)
                                    .addField("retailPrice", int.class);

                            oldVersion++;
                        }
                        if (oldVersion == 1) {
                            schema.rename("Category_RO", "Category");
                            schema.rename("Profile_RO", "Profile");
                            schema.rename("Session_RO", "Session");
                            schema.rename("Tag_RO", "Tag");
                            schema.rename("Video_RO", "Video");
                            schema.rename("Workout_RO", "Workout");
                            oldVersion++;
                        }
                        if (oldVersion == 2) {
                            schema.create("Subscription2")
                                    .addField("transactionId", String.class, FieldAttribute.PRIMARY_KEY)
                                    .addField("type", String.class)
                                    .addField("expiration", long.class)
                                    .addField("trialing", boolean.class)
                                    .addField("valid", boolean.class);
                            oldVersion++;
                        }
                        if (oldVersion == 3) {
                            schema.remove("Subscription");
                            if (schema.contains("Subscriptions2")) {
                                schema.rename("Subscription2", "Subscription");
                            } else {
                                schema.create("Subscription")
                                        .addField("transactionId", String.class, FieldAttribute.PRIMARY_KEY)
                                        .addField("type", String.class)
                                        .addField("expiration", long.class)
                                        .addField("trialing", boolean.class)
                                        .addField("valid", boolean.class);
                            }
                            oldVersion++;
                        }
                        if (oldVersion == 4) {
                            if (!schema.contains("YouTubeVideo")) {
                                schema.create("YouTubeVideo")
                                        .addField("objectId", String.class, FieldAttribute.PRIMARY_KEY)
                                        .addField("title", String.class)
                                        .addField("author", String.class)
                                        .addField("workoutSync", boolean.class)
                                        .addField("hidePopups", boolean.class)
                                        .addField("thumbUrl", String.class)
                                        .addField("youtubeId", String.class);
                            }
                            oldVersion++;
                        }
                        if (oldVersion == 5) {
                            schema.get("Profile").addField("freeCustom", String.class);
                            oldVersion++;
                        }
                    }
                }) // Migration to run instead of throwing an exception
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        Realm realm = Realm.getDefaultInstance();
//        Stetho.initialize(
//                Stetho.newInitializerBuilder(this)
//                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
//                        .build());

        JodaTimeAndroid.init(this);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        SharedPreferencesInterface.initialize(this);

        try {
            KineticSDK.initialize("18c4b061-d3f1-49ab-bade-b5653a1556ac");
        } catch (APIKeyException e) {

        }

        SensorDataService_.intent(this).start();
        FitSubscription_.intent(this).start();

        DataSync_.intent(this).start();
        SensorScanner_.intent(this).start();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityStarted();
                Log.d(TAG, "activityStarted: " + activity + " (" + mActivityCount + ")");
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityStopped();
                Log.d(TAG, "onActivityStopped: " + activity + " (" + mActivityCount + ")");
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        if (sharedPreferences.getInt(APP_VERSION, 0) != BuildConfig.VERSION_CODE) {
//            Profile.logOut(this);
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.deleteAll();
//                }
//            });
//            Intent i = new Intent(this, LoginDispatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(i);
            sharedPreferences.edit().putInt(APP_VERSION, BuildConfig.VERSION_CODE).apply();
        }
        realm.close();
    }

}
