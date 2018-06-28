package com.kinetic.fit.connectivity.third_party_clients;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;
import com.kinetic.fit.data.realm_objects.Profile;

import static com.kinetic.fit.connectivity.third_party_clients.DropboxClient.DROPBOX_PREF;
import static com.kinetic.fit.connectivity.third_party_clients.DropboxClient.STATUS_CHANGED;

/**
 * Created by Saxton on 5/16/17.
 */

public class DropboxAuthActivity extends AppCompatActivity{

    private static final String DROPBOX_KEY= "nyrnurbq98qpjgs";

    SharedPreferences sharedPreferences;
    String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("DropboxClient", MODE_PRIVATE);
        uuid = Profile.getUUID();
//        Auth.startOAuth2Authentication(getApplicationContext(), DROPBOX_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccessToken();
    }

    private void getAccessToken(){
        String token = Auth.getOAuth2Token();
        if(token != null){
            sharedPreferences.edit().putString(DROPBOX_PREF + uuid, token).apply();
            Intent i = new Intent();
            i.putExtra(OAuth2Activity.AUTH_TOKEN, token);
            setResult(RESULT_OK, i);
            finish();
        } else {
            Auth.startOAuth2Authentication(getApplicationContext(), DROPBOX_KEY);
        }
    }
}
