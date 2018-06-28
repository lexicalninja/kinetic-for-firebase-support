package com.kinetic.fit.connectivity.third_party_clients;

public class UAAuthActivity extends OAuth2Activity {

    private static final String TAG = "UAAuthActivity";

    private static final String REDIRECT_URI = "<api-key>";
//private static final String REDIRECT_URI = "com.kinetic.fit://underarmour";

    private static final String RESPONSE_TYPE = "code";

    @Override
    String getAuthorizationUrl() {
        return "https://www.mapmyfitness.com/v7.1/oauth2/authorize/"
//        return "https://www-mapmyfitness-com-ure3t8ihuk3s.runscope.net/v7.1/oauth2/uacf/authorize/"
                + "?client_id=" + UAClient.UA_CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&redirect_uri=" + REDIRECT_URI;
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
        return null;
    }

}
