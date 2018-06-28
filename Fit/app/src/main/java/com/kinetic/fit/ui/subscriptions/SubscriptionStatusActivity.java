package com.kinetic.fit.ui.subscriptions;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.koushikdutta.async.future.FutureCallback;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saxton on 4/26/17.
 */

@EActivity(R.layout.activity_subscription_status)
public class SubscriptionStatusActivity extends FitActivity implements PurchasesUpdatedListener {
    private static final String TAG = "SubStatusActivity";

    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;
    @ViewById(R.id.button_right)
    FitButton rightButton;
    @ViewById(R.id.monthly_sub)
    LinearLayout monthlySubButton;
    @ViewById(R.id.quarterly_sub)
    LinearLayout quarterlySubButton;
    @ViewById(R.id.fetching_prices_text)
    TextView fetchingPrices;
    @ViewById(R.id.monthly_price)
    TextView monthlyPrice;
    @ViewById(R.id.quarterly_price)
    TextView quarterlyPrice;

    private BillingClient mBillingClient;
    IInAppBillingService mBillingService;

    ServiceConnection mBillingServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBillingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mBillingService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSync = null;
        }
    };

    @AfterViews
    void afterViews() {
        leftButton.setVisibility(View.INVISIBLE);
        middleButton.setVisibility(View.INVISIBLE);
        rightButton.setFitButtonStyle(FitButton.DEFAULT);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBillingClient = new BillingClient.Builder(SubscriptionStatusActivity.this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponse) {
                if (billingResponse == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    getSubscriptionPrices();
                    monthlySubButton.setOnClickListener(billMeMonthly);
                    quarterlySubButton.setOnClickListener(billMeQuarterly);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to the
                // In-app Billing service by calling the startConnection() method.
//                fetchingPrices.setText();
            }
        });
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mBillingServiceConn, Context.BIND_AUTO_CREATE);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDataSyncConnection != null) {
            unbindService(mDataSyncConnection);
        }
        if (mBillingClient != null) {
            unbindService(mBillingServiceConn);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                JsonObject params = new JsonObject();
                JsonObject receiptData = new JsonObject();
                receiptData.addProperty("data", purchase.getOriginalJson());
                receiptData.addProperty("signature", purchase.getSignature());
                params.add("receiptData", receiptData);
                params.addProperty("receiptUsername", Profile.getCurrentUsername());
                mDataSync.subscribe(params, new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            if (result.get("result") != null && result.get("result").getAsInt() == 200) {
                                Intent i = new Intent(ConfirmationActivty.ROLE_REBUILD_COMPLETE);
                                sendBroadcast(i);
                            }
                        } else {
                            Crashlytics.logException(e);
                        }
                    }
                });
                Answers.getInstance().logPurchase(new PurchaseEvent().putItemId(purchase.getSku()).putSuccess(true));
            }
            ConfirmationActivty_
                    .intent(SubscriptionStatusActivity.this)
                    .start();
            finish();
        }
    }

    View.OnClickListener billMeMonthly = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBillingClient.isReady() && mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingClient.BillingResponse.OK) {
                BillingFlowParams.Builder builder = new BillingFlowParams.Builder()
                        .setSku("com.kinetic.fit.smart.monthly")
                        .setType(BillingClient.SkuType.SUBS);
                int responseCode = mBillingClient.launchBillingFlow(SubscriptionStatusActivity.this, builder.build());
            }
        }
    };

    View.OnClickListener billMeQuarterly = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBillingClient.isReady() && mBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS) == BillingClient.BillingResponse.OK) {
                BillingFlowParams.Builder builder = new BillingFlowParams.Builder()
                        .setSku("com.kinetic.fit.smart.quarterly")
                        .setType(BillingClient.SkuType.SUBS);
                int responseCode = mBillingClient.launchBillingFlow(SubscriptionStatusActivity.this, builder.build());
            }
        }
    };

    public void getSubscriptionPrices() {
        List<String> skuList = new ArrayList<>();
        skuList.add("com.kinetic.fit.smart.monthly");
        skuList.add("com.kinetic.fit.smart.quarterly");
        mBillingClient.querySkuDetailsAsync(BillingClient.SkuType.SUBS, skuList,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(SkuDetails.SkuDetailsResult result) {
                        // Process the result.
                        if (result.getResponseCode() == BillingClient.BillingResponse.OK
                                && result.getSkuDetailsList() != null) {
                            fetchingPrices.setVisibility(View.GONE);
                            for (SkuDetails details : result.getSkuDetailsList()) {
                                setPrices(details);
                            }
                        } else {
                            fetchingPrices.setText(getString(R.string.subscriptions_error_fetching_prices));
                        }
                    }
                });
    }

    public void setPrices(SkuDetails details) {
        switch (details.getSku()) {
            case "com.kinetic.fit.smart.monthly": {
                monthlyPrice.setText(details.getPrice());
                break;
            }
            case "com.kinetic.fit.smart.quarterly": {
                quarterlyPrice.setText(details.getPrice());
                break;
            }
        }
    }
}
