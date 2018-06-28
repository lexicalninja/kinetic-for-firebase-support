package com.kinetic.fit.util;

import android.os.Build;

import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;

/**
 * Created by Saxton on 10/4/17.
 */

public class FitRateApp {
    public static boolean askToRateApp(Session session) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                !Profile.preferredRider() ||
                session.getDuration() < 20 * 60 ||
                !SharedPreferencesInterface.requestReviews()) {
            return false;
        }
        return true;
    }
}
