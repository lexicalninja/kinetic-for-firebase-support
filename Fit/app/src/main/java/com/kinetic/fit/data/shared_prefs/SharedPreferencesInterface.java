package com.kinetic.fit.data.shared_prefs;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Created by andrew on 2/25/15.
 */
public class SharedPreferencesInterface {

    private static final String PREFERENCE_KEY_CURRENT_PROFILE_UUID = "PREFERENCE_KEY_CURRENT_PROFILE_UUID";
    private static final String PREFERENCE_KEY_HAS_PRO_FLYWHEEL = "PREFERENCE_KEY_HAS_PRO_FLYWHEEL";
    private static final String PREFERENCE_KEY_LAST_CONFIGURED_TIME = "PREFERENCE_KEY_LAST_CONFIGURED_TIME";
    private static final String PREFERENCE_KEY_SPINDOWN_DURATION = "PREFERENCE_KEY_SPINDOWN_DURATION";
    private static final String PREFERENCE_KEY_CP_SENSOR_NAME = "PREFERENCE_KEY_CP_SENSOR_NAME";
    private static final String PREFERENCE_KEY_SUPPORT_FORM = "SupportFormDescriptionKey--";
    private static final String PREFERENCE_KEY_METRIC = "PREFERENCE_KEY_METRIC";
    private static final String PREFERENCE_RATE_APP = "PREFERENCE_RATE_APP";
    private static final String PREFERENCE_RATE_APP_WAIT_TIME= "PREFERENCE_RATE_APP_WAIT_TIME";

    private static Context mContext;
    private static final String TAG = "KineticSharedPrefs";

    public static void initialize(Context context) {
        mContext = context;
    }

    public static boolean isMetric() {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(TAG, 0);
            return prefs.getBoolean(PREFERENCE_KEY_METRIC, systemIsMetric());
        }
        return true;
    }

    private static boolean systemIsMetric() {
        String countryCode = Locale.getDefault().getCountry();
        if ("US".equals(countryCode)) return false; // USA
        if ("LR".equals(countryCode)) return false; // liberia
        if ("MM".equals(countryCode)) return false; // burma
        return true;
    }

    public static void setMetric(boolean metric) {
        if (mContext != null) {
            SharedPreferences prefs = mContext.getSharedPreferences(TAG, 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREFERENCE_KEY_METRIC, metric);
            editor.apply();
        }
    }

    public static boolean requestReviews(){
        SharedPreferences prefs = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFERENCE_RATE_APP, true);
    }

    public static void setWaitTime(){
        SharedPreferences prefs = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int wait = prefs.getInt(PREFERENCE_RATE_APP_WAIT_TIME, 6);
        if(wait == 0){
            editor.putBoolean(PREFERENCE_RATE_APP, true);
            wait = 5;
        } else {
            wait--;
        }
        editor.putInt(PREFERENCE_RATE_APP_WAIT_TIME, wait);
        editor.apply();
    }

    public static void setRequestRateApp(boolean req){
        SharedPreferences prefs = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCE_RATE_APP, req);
        editor.apply();
    }



}
