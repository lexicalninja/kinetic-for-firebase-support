package com.kinetic.fit.connectivity.third_party_clients;

public class TrainingPeaksAuthActivity extends OAuth2Activity {

    private static final String TAG = "TrainingPeaksAuthActivity";


    private static final String RESPONSE_TYPE = "code";

    @Override
    String getAuthorizationUrl() {
        return "https://oauth.trainingpeaks.com/oauth/authorize?client_id=" + TrainingPeaksClient.TP_CLIENT_ID
                + "&response_type=" + RESPONSE_TYPE
                + "&redirect_uri=" + getRedirectUri()
                + "&scope=file:write";
    }

    @Override
    String getRedirectUri() {
        return TrainingPeaksClient.REDIRECT_URI;
    }

    @Override
    String getResponseType() {
        return RESPONSE_TYPE;
    }

    @Override
    String getStateCode() {
        return "";
    }
}
