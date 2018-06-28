package com.kinetic.fit.ui.support;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.jaredrummler.android.device.DeviceName;
import com.kinetic.fit.BuildConfig;
import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.FitActivity;
import com.zendesk.sdk.feedback.ZendeskFeedbackConfiguration;
import com.zendesk.sdk.feedback.ui.ContactZendeskActivity;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.model.access.JwtIdentity;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.sdk.requests.RequestActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

import java.util.Arrays;
import java.util.List;

@EActivity(R.layout.activity_fit_support)
public class SupportActivity extends FitActivity {

    private static final String TAG = "SupportActivity";


    private ZendeskFeedbackConfiguration configuration = new ZendeskFeedbackConfiguration() {
        @Override
        public String getRequestSubject() {
            return "Android Support Request";
        }

        @Override
        public List<String> getTags() {
            return Arrays.asList("fit.android", "tag2");
        }

        @Override
        public String getAdditionalInfo() {
            return DeviceName.getDeviceName() + "\nAndroid Version " + Build.VERSION.RELEASE + "\nFit Version " + BuildConfig.VERSION_NAME + " (Build " + BuildConfig.VERSION_CODE + ")";
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ZendeskConfig.INSTANCE.isInitialized()) {
            ZendeskConfig.INSTANCE.init(this, "https://support.kurtkinetic.com", "a4792a6837bb6a543866b85b6cd520498c939c9313c32e60", "mobile_sdk_client_83c47ba8af3e81c4e1cc");
        }
        configureZendesk();
    }

    private void configureZendesk() {
        Identity jwtId = new JwtIdentity(Profile.getCurrentSession());
        ZendeskConfig.INSTANCE.setIdentity(jwtId);
    }

    @Click(R.id.button_support_contact)
    void handleButtonContact() {
        ContactZendeskActivity.startActivity(this, configuration);
    }

    @Click(R.id.button_support_kbase)
    void handleButtonKnowledgeBase() {
        new com.zendesk.sdk.support.SupportActivity.Builder()
                .showContactUsButton(false)
                .withContactConfiguration(configuration)
                .withCategoriesCollapsed(true)
                .show(this);
    }

    @Click(R.id.button_support_tickets)
    void handleButtonTickets() {
        Intent requestIntent = new Intent(this, RequestActivity.class);
        startActivity(requestIntent);
    }

}
