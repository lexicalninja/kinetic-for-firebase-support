package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kinetic.fit.ui.settings.profile.SocialActivity;
import com.kinetic.fit.ui.settings.profile.SocialActivity_;

/**
 * Created by Saxton on 7/12/17.
 */

public class OAuthRedirectHandlerActivity extends Activity {
    public static final String ROOT_URI_SCHEME = "com.kinetic.fit:/";
    public static final String CHECKER = "<api-key>";
    String authToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String uri = getIntent().getData().toString();
        if (uri.startsWith(ROOT_URI_SCHEME)) {
            uri = uri.substring(ROOT_URI_SCHEME.length(), uri.length());
            uri = uri.substring(0, uri.indexOf("?"));
            switch (uri) {
                case "/strava": {
                    checkForHackers("code", CHECKER);
                    Intent i = new Intent(this, SocialActivity_.class);
                    i.putExtra(OAuth2Activity.AUTH_TOKEN, authToken);
                    i.putExtra("requestCode", SocialActivity.ACTIVITY_REQUEST_STRAVA_TOKEN);
                    startActivity(i);
                    finish();
                    break;
                }
                case "google": {
                    checkForHackers("code", CHECKER);
                    Intent i = new Intent(this, SocialActivity_.class);
                    i.putExtra(OAuth2Activity.AUTH_TOKEN, authToken);
                    i.putExtra("requestCode", SocialActivity.ACTIVITY_REQUEST_GOOGLE_TOKEN);
                    startActivity(i);
                    finish();
                }
            }
        }
    }

    private void checkForHackers(String responseType, String clientStateCode) {
        String stateCode = getIntent().getData().getQueryParameter("state");
        authToken = getIntent().getData().getQueryParameter(responseType);
        if (authToken == null) {
//            exit
            finish();
        } else if (stateCode != null && clientStateCode != null && !stateCode.equals(clientStateCode)) {
            // CSRF attack. Fuck you hackers.
            //            exit
            finish();
        }
    }
}
