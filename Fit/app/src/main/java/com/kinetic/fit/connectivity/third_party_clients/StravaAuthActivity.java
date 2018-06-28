package com.kinetic.fit.connectivity.third_party_clients;

import static com.kinetic.fit.connectivity.third_party_clients.OAuthRedirectHandlerActivity.CHECKER;

public class StravaAuthActivity extends OAuth2Activity {

    private static final String TAG = "StravaAuthActivity";

    private static final String REDIRECT_URI = OAuthRedirectHandlerActivity.ROOT_URI_SCHEME + "/strava";
    private static final String RESPONSE_TYPE = "code";
    boolean systemBrowserRequired = true;

    @Override
    String getAuthorizationUrl() {
        return "https://www.strava.com/oauth/authorize?client_id=" + StravaClient.STRAVA_CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&state=" + CHECKER
                + "&redirect_uri=" + REDIRECT_URI
                + "&scope=write";
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
