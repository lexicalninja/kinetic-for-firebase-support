package com.kinetic.fit.connectivity.third_party_clients;

public class TwoPeakAuthActivity extends OAuth2Activity {

    private static final String TAG = "2PeakAuthActivity";

    public static final String REDIRECT_URI = "<api-key>";
    private static final String RESPONSE_TYPE = "code";
    private static final String STATE_CODE = "<api-key>";

    @Override
    String getAuthorizationUrl() {
        return "https://www.2peak.com/oauth/authorize.php?client_id=" + TwoPeakClient.TWOPEAK_CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&redirect_uri=" + REDIRECT_URI
                + "&state=" + STATE_CODE;
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
        return STATE_CODE;
    }
}
