package com.kinetic.fit.connectivity.third_party_clients;

import static com.kinetic.fit.connectivity.third_party_clients.OAuthRedirectHandlerActivity.CHECKER;

/**
 * Created by Saxton on 7/31/17.
 */

public class GoogleAuthActivity extends OAuth2Activity {
    private static final String TAG = "GoogleAuthActivity";

    public static final String REDIRECT_URI = OAuthRedirectHandlerActivity.ROOT_URI_SCHEME + "google";
    private static final String RESPONSE_TYPE = "code";
    public static final String GOOGLE_CLIENT_ID = "846880199413-m281m1v4fs4rcsr6ur0umnor4pqabf1n.apps.googleusercontent.com";
    private static final String SCOPE = "https://www.googleapis.com/auth/youtube.readonly";
    boolean systemBrowserRequired = true;

    @Override
    String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + GOOGLE_CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&state=" + CHECKER
                + "&redirect_uri=" + REDIRECT_URI
                + "&scope=" + SCOPE;
    }

    @Override
    String getRedirectUri() {
        return REDIRECT_URI;
    }

    @Override
    String getResponseType() {
        return RESPONSE_TYPE;
    }

    @Override
    String getStateCode() {
        return CHECKER;
    }

    @Override
    boolean getSystemBrowserRequired() {
        return systemBrowserRequired;
    }
}
