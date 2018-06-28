package com.kinetic.fit.ui.settings.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.third_party_clients.DropboxAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient;
import com.kinetic.fit.connectivity.third_party_clients.GoogleAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.GoogleClient;
import com.kinetic.fit.connectivity.third_party_clients.OAuth2Activity;
import com.kinetic.fit.connectivity.third_party_clients.StravaAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.StravaClient;
import com.kinetic.fit.connectivity.third_party_clients.TrainingPeaksAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.TrainingPeaksClient;
import com.kinetic.fit.connectivity.third_party_clients.TwoPeakAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.TwoPeakClient;
import com.kinetic.fit.connectivity.third_party_clients.UAAuthActivity;
import com.kinetic.fit.connectivity.third_party_clients.UAClient;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.util.ViewStyling;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 2/10/17.
 * /
 */

@EActivity(R.layout.activity_social)
public class SocialActivity extends FitActivity implements SocialStatusView.SocialStatusViewListener {
    private static final String TAG = "ProfileFragmentSocial";

    public static final int ACTIVITY_REQUEST_STRAVA_TOKEN = 2;
    public static final int ACTIVITY_REQUEST_UA_TOKEN = 3;
    public static final int ACTIVITY_REQUEST_TP_TOKEN = 4;
    public static final int ACTIVITY_REQUEST_2PEAK_TOKEN = 5;
    public static final int ACTIVITY_REQUEST_DROPBOX_TOKEN = 6;
    public static final int ACTIVITY_REQUEST_GOOGLE_TOKEN = 7;

    @ViewById
    protected SocialStatusView socialStrava;
    @ViewById
    protected SocialStatusView socialMapMyFitness;
    @ViewById
    protected SocialStatusView socialTrainingPeaks;
    @ViewById
    protected SocialStatusView social2Peak;
    @ViewById
    protected SocialStatusView socialDropbox;
    @ViewById
    protected SocialStatusView socialTwitter;
    @ViewById
    protected SocialStatusView socialGoogle;
    @Bean
    protected StravaClient stravaClient;
    @Bean
    protected UAClient uaClient;
    @Bean
    protected TrainingPeaksClient trainingPeaksClient;
    @Bean
    protected TwoPeakClient twoPeakClient;
    @Bean
    protected DropboxClient dropboxClient;
    @Bean
    protected GoogleClient googleClient;
    TwitterAuthClient mTwitterAuthClient;
    boolean twitterConnected;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            onOAuthRedirect(getIntent().getExtras());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSocialViews();
    }

    @AfterViews
    void afterViews() {
        socialGoogle.setListener(this);
        socialGoogle.title.setText("Google");
        socialGoogle.removeFriendShare();
        socialGoogle.removePublicShare();
        socialGoogle.privateShare.setText("YouTube");
        socialGoogle.removeAutoShare();
        socialGoogle.socialIcon.setImageResource(R.drawable.material_icon_google_circle);

        socialStrava.setListener(this);
        socialStrava.title.setText("Strava");
        socialStrava.removeFriendShare();

        socialMapMyFitness.setListener(this);
        socialMapMyFitness.title.setText("MapMyFitness");
        socialMapMyFitness.socialIcon.setImageResource(R.mipmap.social_icon_mapmyfitness);

        socialTrainingPeaks.setListener(this);
        socialTrainingPeaks.title.setText("Training Peaks");
        socialTrainingPeaks.socialIcon.setImageResource(R.mipmap.social_icon_trainingpeaks);
        socialTrainingPeaks.removeFriendShare();


        social2Peak.setListener(this);
        social2Peak.title.setText("2Peak");
        social2Peak.removeFriendShare();
        social2Peak.socialIcon.setImageResource(R.mipmap.social_icon_2peak);

        socialDropbox.setListener(this);
        socialDropbox.title.setText("Dropbox");
        socialDropbox.removeFriendShare();
        socialDropbox.removePublicShare();
        socialDropbox.socialIcon.setImageResource(R.mipmap.social_icons_dropbox);

        socialTwitter.setListener(this);
        socialTwitter.removeFriendShare();
        socialTwitter.removePrivateShare();
        socialTwitter.socialIcon.setImageResource(R.drawable.social_icon_twitter);
        socialTwitter.title.setText("Twitter");

        updateSocialViews();

        View view = getCurrentFocus();
        if (view != null && view.getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void updateSocialViews() {
        socialGoogle.setConnected(googleClient.isConnected());
        if (googleClient.isConnected()) {
            socialGoogle.setSocialVisibility(0);
        }

        socialStrava.setConnected(stravaClient.isConnected());
        if (stravaClient.isConnected()) {
            socialStrava.setAutoShare(stravaClient.getAutoShare());
            if (stravaClient.getSharePublic()) {
                socialStrava.setSocialVisibility(2);
            } else {
                socialStrava.setSocialVisibility(0);
            }
        }

        socialMapMyFitness.setConnected(uaClient.isConnected());
        if (uaClient.isConnected()) {
            socialMapMyFitness.setAutoShare(uaClient.getAutoShare());
            if (uaClient.getSharePublic()) {
                socialMapMyFitness.setSocialVisibility(2);
            } else if (uaClient.getSharedFriends()) {
                socialMapMyFitness.setSocialVisibility(1);
            } else {
                socialMapMyFitness.setSocialVisibility(0);
            }
        }

        socialTrainingPeaks.setConnected(trainingPeaksClient.isConnected());
        if (trainingPeaksClient.isConnected()) {
            socialTrainingPeaks.setAutoShare(trainingPeaksClient.getAutoShare());
            if (trainingPeaksClient.getSharePublic()) {
                socialTrainingPeaks.setSocialVisibility(2);
            } else {
                socialTrainingPeaks.setSocialVisibility(0);
            }
        }

        social2Peak.setConnected(twoPeakClient.isConnected());
        if (twoPeakClient.isConnected()) {
            social2Peak.setAutoShare(twoPeakClient.getAutoShare());
            if (twoPeakClient.getSharePublic()) {
                social2Peak.setSocialVisibility(2);
            } else {
                social2Peak.setSocialVisibility(0);
            }
        }

        socialDropbox.setConnected(dropboxClient.isConnected());
        if (dropboxClient.isConnected()) {
            socialDropbox.setAutoShare(dropboxClient.getAutoShare());
            socialDropbox.setSocialVisibility(0);
        }

        socialTwitter.setConnected(twitterConnected);
        if (twitterConnected) {
            socialTwitter.setSocialVisibility(2);
            //        TODO autoshare here. probably need a client
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_STRAVA_TOKEN && resultCode == Activity.RESULT_OK && data != null) {
            String authToken = data.getStringExtra(OAuth2Activity.AUTH_TOKEN);
            stravaClient.exchangeAuthToken(authToken);
        } else if (requestCode == ACTIVITY_REQUEST_UA_TOKEN && resultCode == Activity.RESULT_OK && data != null) {
            String authToken = data.getStringExtra(OAuth2Activity.AUTH_TOKEN);
            uaClient.exchangeAuthToken(authToken);
        } else if (requestCode == ACTIVITY_REQUEST_TP_TOKEN && resultCode == Activity.RESULT_OK && data != null) {
            String authToken = data.getStringExtra(OAuth2Activity.AUTH_TOKEN);
            trainingPeaksClient.exchangeAuthToken(authToken);
        } else if (requestCode == ACTIVITY_REQUEST_2PEAK_TOKEN && resultCode == Activity.RESULT_OK && data != null) {
            String authToken = data.getStringExtra(OAuth2Activity.AUTH_TOKEN);
            twoPeakClient.exchangeAuthToken(authToken);
        } else if (requestCode == ACTIVITY_REQUEST_DROPBOX_TOKEN && resultCode == Activity.RESULT_OK && data != null) {
//           might not need to do anything here
            dropboxClient.discoverVideos();
        } else if (mTwitterAuthClient != null) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
        updateSocialViews();
    }

    @Override
    public void onBackPressed() {
        RootActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
        finish();
    }

    public void onOAuthRedirect(Bundle data) {
        int requestCode = data.getInt("requestCode");
        if (requestCode == ACTIVITY_REQUEST_STRAVA_TOKEN) {
            String authToken = data.getString(OAuth2Activity.AUTH_TOKEN);
            stravaClient.exchangeAuthToken(authToken);
        }
        if (requestCode == ACTIVITY_REQUEST_GOOGLE_TOKEN) {
            String authToken = data.getString(OAuth2Activity.AUTH_TOKEN);
            googleClient.exchangeAuthToken(authToken);
        }
    }

    @Override
    public void toggleConnection(SocialStatusView statusView) {
        if (statusView == socialGoogle) {
            if (googleClient.isConnected()) {
                googleClient.disconnect();
            } else {
                Intent i = new Intent(this, GoogleAuthActivity.class);
                startActivity(i);
            }
        } else if (statusView == socialStrava) {
            if (stravaClient.isConnected()) {
                stravaClient.disconnect();
            } else {
                Intent i = new Intent(this, StravaAuthActivity.class);
                startActivity(i);
            }
        } else if (statusView == socialMapMyFitness) {
            if (uaClient.isConnected()) {
                uaClient.disconnect();
            } else {
                Intent i = new Intent(this, UAAuthActivity.class);
                startActivityForResult(i, ACTIVITY_REQUEST_UA_TOKEN);
            }
        } else if (statusView == socialTrainingPeaks) {
            if (trainingPeaksClient.isConnected()) {
                trainingPeaksClient.disconnect();
            } else {
                Intent i = new Intent(this, TrainingPeaksAuthActivity.class);
                startActivityForResult(i, ACTIVITY_REQUEST_TP_TOKEN);
            }
        } else if (statusView == social2Peak) {
            if (twoPeakClient.isConnected()) {
                twoPeakClient.disconnect();
            } else {
                Intent i = new Intent(this, TwoPeakAuthActivity.class);
                startActivityForResult(i, ACTIVITY_REQUEST_2PEAK_TOKEN);
            }
        } else if (statusView == socialDropbox) {
            if (dropboxClient.isConnected()) {
                dropboxClient.disconnect();
            } else {
                Intent i = new Intent(this, DropboxAuthActivity.class);
                startActivityForResult(i, ACTIVITY_REQUEST_DROPBOX_TOKEN);
            }
        } else if (statusView == socialTwitter) {
            if (twitterConnected) {
                Twitter.getSessionManager().clearActiveSession();
            } else {
                mTwitterAuthClient = new TwitterAuthClient();
                mTwitterAuthClient.authorize(this, new Callback<TwitterSession>() {
                    @Override
                    public void success(Result<TwitterSession> result) {
                        ViewStyling.getCustomToast(SocialActivity.this, getLayoutInflater(), getString(R.string.success)).show();
                        checkTwitterSession();
                        updateSocialViews();
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        ViewStyling.getCustomToast(SocialActivity.this, getLayoutInflater(), getString(R.string.twitter_error_connecting)).show();
                    }
                });
            }
            checkTwitterSession();
        }
        updateSocialViews();
    }

    @Override
    public void toggleAutoShare(SocialStatusView statusView) {
        if (statusView == socialStrava) {
            stravaClient.setAutoShare(!stravaClient.getAutoShare());

        } else if (statusView == socialMapMyFitness) {
            uaClient.setAutoShare(!uaClient.getAutoShare());

        } else if (statusView == socialTrainingPeaks) {
            trainingPeaksClient.setAutoShare(!trainingPeaksClient.getAutoShare());

        } else if (statusView == social2Peak) {
            twoPeakClient.setAutoShare(!twoPeakClient.getAutoShare());
        } else if (statusView == socialDropbox) {
            dropboxClient.setAutoShare(!dropboxClient.getAutoShare());
        }
        updateSocialViews();
    }

    @Override
    public void changeVisibility(SocialStatusView statusView, int visIndex) {
        if (statusView == socialStrava) {
            if (visIndex == 0) {
                stravaClient.setSharePublic(false);
            } else {
                stravaClient.setSharePublic(true);
            }
        } else if (statusView == socialMapMyFitness) {
            if (visIndex == 2) {
                uaClient.setSharePublic(true);
                uaClient.setSharedFriends(false);
            } else if (visIndex == 1) {
                uaClient.setSharePublic(false);
                uaClient.setSharedFriends(true);
            } else {
                uaClient.setSharedFriends(false);
                uaClient.setSharePublic(false);
            }
        } else if (statusView == socialTrainingPeaks) {
            if (visIndex == 0) {
                trainingPeaksClient.setSharePublic(false);
            } else {
                trainingPeaksClient.setSharePublic(true);
            }
        } else if (statusView == social2Peak) {
            if (visIndex == 0) {
                twoPeakClient.setSharePublic(false);
            } else {
                twoPeakClient.setSharePublic(true);
            }
        } else if (statusView == socialGoogle) {
            if (visIndex == 0) {
                twoPeakClient.setSharePublic(false);
            } else {
                twoPeakClient.setSharePublic(true);
            }
        }
        updateSocialViews();
    }

    public void checkTwitterSession() {
        twitterConnected = Twitter.getSessionManager().getActiveSession() != null;
    }

    @Receiver(actions = StravaClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void onStravaStatusChanged() {
        updateSocialViews();
    }

    @Receiver(actions = UAClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void onUAStatusChanged() {
        updateSocialViews();
    }

    @Receiver(actions = TrainingPeaksClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void onTrainingPeaksStatusChanged() {
        updateSocialViews();
    }

    @Receiver(actions = TwoPeakClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnResumeOnPause)
    protected void on2PeakStatusChanged() {
        updateSocialViews();
    }

    @Receiver(actions = DropboxClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onDropBoxStatusChanged() {
        updateSocialViews();
    }

    @Receiver(actions = GoogleClient.STATUS_CHANGED, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onGoogleStatusChanged() {
        if(googleClient.isConnected()){
            ViewStyling.getCustomToast(this, getLayoutInflater(), "Google Connected").show();
        }
        updateSocialViews();
    }

}
