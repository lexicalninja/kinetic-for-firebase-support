package com.kinetic.fit.ui.subscriptions;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.fragment.SupportWalletFragment;
import com.google.android.gms.wallet.fragment.WalletFragmentInitParams;
import com.google.android.gms.wallet.fragment.WalletFragmentMode;
import com.google.android.gms.wallet.fragment.WalletFragmentOptions;
import com.google.gson.JsonObject;
import com.kinetic.fit.R;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.objects.ShippingAddress;
import com.kinetic.fit.data.objects.SmartSubscription;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.SubscriptionAddOn;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.CheckoutItemView;
import com.kinetic.fit.ui.widget.CreditCardWidget;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.FitProgressDialog;
import com.kinetic.fit.util.ViewStyling;
import com.koushikdutta.async.future.FutureCallback;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.net.TokenParser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import static android.view.View.GONE;

/**
 * Created by Saxton on 4/28/17.
 */

@EActivity(R.layout.activity_checkout)
public class CheckoutActivity extends FitActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "CheckoutActivity";
    public static final int WALLET_ENVIRONMENT = WalletConstants.ENVIRONMENT_PRODUCTION;
    public static final String MERCHANT_NAME = "Kinetic Fit";
    public static final String CURRENCY_CODE_USD = "USD";
    //    private static final String PUBLISHABLE_KEY = "pk_test_dl9DYsYCEjQbuLyK6jy9QfpT";
    private static final String PUBLISHABLE_KEY = "pk_live_BvCQ555h4K6kudp8HTfS69nA";
    private static final String SMART_PLAN_ID = "app_smart_year";
    // Unique identifiers for asynchronous requests:
    private static final int LOAD_MASKED_WALLET_REQUEST_CODE = 1000;
    private static final int LOAD_FULL_WALLET_REQUEST_CODE = 1001;

    private SupportWalletFragment walletFragment;
    private MaskedWallet mMaskedWallet;

    GoogleApiClient mGoogleApiClient;
    IsReadyToPayRequest mIsReadyToPayRequest;
    FitProgressDialog mProgressDialog;
    Card mCard;

    @ViewById(R.id.button_left)
    FitButton buttonLeft;
    @ViewById(R.id.button_middle)
    FitButton buttonMiddle;
    @ViewById(R.id.button_right)
    FitButton buttonRight;
    @ViewById(R.id.credit_card_widget)
    CreditCardWidget cardWidget;
    @ViewById(R.id.wallet_frame)
    FrameLayout walletFrame;
    @ViewById(R.id.item1)
    CheckoutItemView item1;
    @ViewById(R.id.item2)
    CheckoutItemView item2;
    @ViewById(R.id.totalCost)
    TextView totalCost;

    @Extra("subscription")
    SmartSubscription subscription;
    @Extra("addon")
    SubscriptionAddOn addOn;

    JsonObject params;

    boolean usingAndroidPay = false;
    boolean usingCreditCard = false;
    int total = 0;
    String totalString;

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSync = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgressDialog();
        mGoogleApiClient.connect();
    }

    @AfterViews
    void afterViews() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this /* onConnectionFailedListener */)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WALLET_ENVIRONMENT)
                        .setTheme(WalletConstants.THEME_LIGHT)
                        .build())
                .build();
        mIsReadyToPayRequest = IsReadyToPayRequest.newBuilder().build();
        buttonLeft.setVisibility(GONE);
        buttonRight.setVisibility(GONE);
        buttonRight.setText(getString(R.string.purchase));
        buttonMiddle.setText(getString(R.string.credit_card));
        item1.setItemName(subscription.getName());
        item1.setItemCost(subscription.getPriceUSPennies());
        item2.setItemName(addOn.getName() + (addOn.getColor() == null ? "" : "\n[" + addOn.getColor() + "]"));
        item2.setItemCost(addOn.getPrice());
        total = subscription.getPriceUSPennies() + addOn.getPrice();
        totalString = "US$" + String.format(Locale.getDefault(), "%,.2f", (double) total / 100.00);
        totalCost.setText(totalString);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDataSyncConnection != null) {
            unbindService(mDataSyncConnection);
        }
    }

    @Click(R.id.button_middle)
    void showStripeWidget() {
        usingCreditCard = !usingCreditCard;
        if (usingCreditCard) {
            cardWidget.setVisibility(View.VISIBLE);
            buttonMiddle.setFitButtonStyle(FitButton.DESTRUCTIVE);
            buttonMiddle.setText(getString(R.string.cancel));
            walletFrame.setVisibility(GONE);
            buttonRight.setVisibility(View.VISIBLE);
        } else {
            cardWidget.setVisibility(GONE);
            buttonMiddle.setFitButtonStyle(FitButton.BASIC);
            buttonMiddle.setText(getString(R.string.credit_card));
            buttonRight.setVisibility(GONE);
            walletFrame.setVisibility(View.VISIBLE);
        }
    }

    @Click(R.id.button_left)
    void cancelAndroidPay() {
        usingAndroidPay = false;
        getSupportFragmentManager().beginTransaction()
                .remove(walletFragment)
                .commit();
        walletFrame.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(GONE);
        buttonRight.setVisibility(GONE);
        buttonMiddle.setVisibility(View.VISIBLE);
        showAndroidPay();
    }

    @Click(R.id.button_right)
    void purchase() {
        showProcessingDialog();
        if (usingCreditCard) {
            mCard = cardWidget.getCard();
            if (mCard != null && mCard.validateCard() && mCard.validateCVC()) {
                Stripe stripe = new Stripe(this, PUBLISHABLE_KEY);
                stripe.createToken(
                        mCard,
                        new TokenCallback() {
                            public void onSuccess(Token token) {
                                // Send token to your server
                                setPurchaseParams(token);
                                subscribe();
//                                Log.d(TAG, params.toString());
                            }

                            public void onError(Exception error) {
                                dismissProgressDialog();
                                // Show localized error message
                                ViewStyling.getCustomToast(CheckoutActivity.this, getLayoutInflater(), getString(R.string.error_processing_payment_message)).show();
                                Crashlytics.logException(error);
                            }
                        }
                );
            } else {
                dismissProgressDialog();
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Something went wrong.Please check your info and try again").show();
            }
        }
        if (usingAndroidPay) {
            FullWalletRequest fullWalletRequest = FullWalletRequest.newBuilder()
                    .setCart(Cart.newBuilder()
                            .setCurrencyCode(CURRENCY_CODE_USD)
                            .setTotalPrice(String.valueOf((double) total / 100.00))
                            .addLineItem(LineItem.newBuilder() // Identify item being purchased
                                    .setCurrencyCode(CURRENCY_CODE_USD)
                                    .setQuantity("1")
                                    .setDescription(subscription.getName())
                                    .setTotalPrice(String.valueOf(subscription.getPriceUSPennies() / 100.00))
                                    .setUnitPrice(String.valueOf(subscription.getPriceUSPennies() / 100.00))
                                    .build())
                            .addLineItem(LineItem.newBuilder() // Identify item being purchased
                                    .setCurrencyCode(CURRENCY_CODE_USD)
                                    .setQuantity("1")
                                    .setDescription(addOn.getName() + (addOn.getColor() == null ? "" : "[" + addOn.getColor() + "]"))
                                    .setTotalPrice(String.valueOf(addOn.getPrice() / 100.00))
                                    .setUnitPrice(String.valueOf(addOn.getPrice() / 100.00))
                                    .build())
                            .build())
                    .setGoogleTransactionId(mMaskedWallet.getGoogleTransactionId())
                    .build();
            Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest, LOAD_FULL_WALLET_REQUEST_CODE);
        }
    }

    public void checkIsReadyToPay() {
        Wallet.Payments.isReadyToPay(mGoogleApiClient, mIsReadyToPayRequest).setResultCallback(
                new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {
                        dismissProgressDialog();
                        if (booleanResult.getStatus().isSuccess()) {
                            if (booleanResult.getValue()) {
                                showAndroidPay();
                                Log.d(TAG, "isReadyToPay:" + booleanResult.getStatus());
                            } else {
                                walletFrame.setVisibility(GONE);
                                Log.d(TAG, "isReadyToPay:" + booleanResult.getStatus());
                            }
                        } else {
                            // Error making isReadyToPay call
                            Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOAD_MASKED_WALLET_REQUEST_CODE) { // Unique, identifying constant
            if (resultCode == Activity.RESULT_OK) {
                mMaskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                showConfirmationFragment();
            }
        } else if (requestCode == LOAD_FULL_WALLET_REQUEST_CODE) { // Unique, identifying constant
            if (resultCode == Activity.RESULT_OK) {
                FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                String tokenJSON = fullWallet.getPaymentMethodToken().getToken();
                try {
                    setPurchaseParams(TokenParser.parseToken(tokenJSON));
                } catch (JSONException e) {
                    dismissProgressDialog();
                    Log.e(TAG, e.getLocalizedMessage());
                }
                //A token will only be returned in production mode,
                //i.e. WalletConstants.ENVIRONMENT_PRODUCTION
                if (WALLET_ENVIRONMENT == WalletConstants.ENVIRONMENT_PRODUCTION) {
                    try {
                        Token token = TokenParser.parseToken(tokenJSON);
                        setPurchaseParams(token);
                        subscribe();
                    } catch (JSONException jsonException) {
                        // Log the error and notify Stripe help
                    }
                }
            }
        } else if (requestCode == CreditCardWidget.CC_SCAN_REQUEST_CODE) {
            String ccNum = null;
            Integer expMonth = null;
            Integer expYear = null;
            String cvvNum = null;

            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                ccNum = scanResult.getFormattedCardNumber();
                if (scanResult.isExpiryValid()) {
                    expMonth = scanResult.expiryMonth;
                    expYear = scanResult.expiryYear;
                }
                if (scanResult.cvv != null) {
                    cvvNum = scanResult.cvv;
                    // Never log or display a CVV
                }
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(), getString(R.string.error_validating_card)).show();
            }
            if (ccNum != null && expMonth != null && cvvNum != null) {
                cardWidget.setStripeCardInfo(ccNum, expMonth, expYear, cvvNum);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
            dismissProgressDialog();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.d(TAG, connectionResult.toString());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkIsReadyToPay();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void showAndroidPay() {
        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WALLET_ENVIRONMENT)
                .setTheme(WalletConstants.THEME_DARK)
                .setMode(WalletFragmentMode.BUY_BUTTON)
                .build();
        walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        MaskedWalletRequest maskedWalletRequest = MaskedWalletRequest.newBuilder()
                // Request credit card tokenization with Stripe by specifying tokenization parameters:
                .setPaymentMethodTokenizationParameters(PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.PAYMENT_GATEWAY)
                        .addParameter("gateway", "stripe")
                        .addParameter("stripe:publishableKey", PUBLISHABLE_KEY)
                        .addParameter("stripe:version", com.stripe.android.BuildConfig.VERSION_NAME)
                        .build())
                // You want the shipping address:
                .setShippingAddressRequired(true)
                // Price set as a decimal:
                .setEstimatedTotalPrice(String.valueOf((double) total / 100.00))
                .setCurrencyCode(CURRENCY_CODE_USD)
                .build();

        // Set the parameters:
        WalletFragmentInitParams initParams = WalletFragmentInitParams.newBuilder()
                .setMaskedWalletRequest(maskedWalletRequest)
                .setMaskedWalletRequestCode(LOAD_MASKED_WALLET_REQUEST_CODE)
                .build();

        // Initialize the fragment:
        walletFragment.initialize(initParams);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wallet_frame, walletFragment)
                .commit();
    }

    private void showConfirmationFragment() {
        usingAndroidPay = true;
        buttonRight.setVisibility(View.VISIBLE);
        buttonMiddle.setVisibility(GONE);

        getSupportFragmentManager().beginTransaction()
                .remove(walletFragment)
                .commit();

        WalletFragmentOptions walletFragmentOptions = WalletFragmentOptions.newBuilder()
                .setEnvironment(WALLET_ENVIRONMENT)
                .setTheme(WalletConstants.THEME_LIGHT)
                .setMode(WalletFragmentMode.SELECTION_DETAILS)
                .build();
        walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions);

        WalletFragmentInitParams.Builder startParamsBuilder = WalletFragmentInitParams.newBuilder()
                .setMaskedWallet(mMaskedWallet)
                .setMaskedWalletRequestCode(LOAD_FULL_WALLET_REQUEST_CODE);
        walletFragment.initialize(startParamsBuilder.build());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.confirm_layout, walletFragment)
                .commit();
        walletFrame.setVisibility(GONE);
        buttonLeft.setFitButtonStyle(FitButton.DESTRUCTIVE);
        buttonLeft.setText(R.string.cancel);
        buttonLeft.setVisibility(View.VISIBLE);
    }

    void showProgressDialog() {
        mProgressDialog = FitProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_saved_payment_methods));
    }

    void showProcessingDialog() {
        mProgressDialog = FitProgressDialog.show(this, getString(R.string.processing), getString(R.string.processing_payment_message));

    }

    void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void setPurchaseParams(Token token) {
        params = new JsonObject();
        if (usingAndroidPay) {
            UserAddress ua = mMaskedWallet.getBuyerShippingAddress();
            ShippingAddress sa = new ShippingAddress();
            sa.setAddress1(ua.getAddress1());
            if (ua.getAddress2() != null) {
                sa.setAddress2(ua.getAddress2());
            }
            sa.setCity(ua.getLocality());
            sa.setState(ua.getAdministrativeArea());
            sa.setPostal(ua.getPostalCode());
            sa.setCountry(ua.getCountryCode());

            if (sa.isValid()) {
                JsonObject shipping = new JsonObject();
                shipping.add("address", sa.getShippingAddress());
                shipping.addProperty("name", ua.getName());
                shipping.addProperty("email", mMaskedWallet.getEmail());

                params.add("shipping", shipping);
                params.addProperty("tokenId", token.getId());
                params.addProperty("addOnId", addOn.getSku());
                params.addProperty("planId", SMART_PLAN_ID);

//                Log.d(TAG, params.toString());
            } else {
//                shipping info is bad
            }
        } else if (usingCreditCard) {
            ShippingAddress sa = new ShippingAddress();
            sa.setAddress1(cardWidget.getAddress1());
            sa.setAddress2(cardWidget.getAddress1());
            sa.setPostal(cardWidget.getPostalCode());
            sa.setCity(cardWidget.getCity());
            sa.setState(cardWidget.getState());
            sa.setCountry(cardWidget.getCountryCode());
            if (sa.isValid()) {
                JsonObject shipping = new JsonObject();
                shipping.add("address", sa.getShippingAddress());
                shipping.addProperty("name", cardWidget.getName());
                shipping.addProperty("email", Profile.getMainEmail());

                params.add("shipping", shipping);
                params.addProperty("token", token.getId());
                params.addProperty("addOnId", addOn.getSku());
                params.addProperty("planId", SMART_PLAN_ID);
//                Log.d(TAG, params.toString());
            }
        }
    }

    public void subscribe() {
        if (mDataSync != null) {
            mDataSync.subscribe(params, new FutureCallback<JsonObject>() {
                @Override
                public void onCompleted(Exception e, JsonObject result) {
                    Log.d(TAG, result.toString());
                    if (e == null && result.get("error") == null) {
                        ConfirmationActivty_.intent(CheckoutActivity.this).start();
                        logPurchaseKPI();
                        dismissProgressDialog();
                    } else {
                        ViewStyling.getCustomToast(CheckoutActivity.this, getLayoutInflater(), getString(R.string.error_processing_payment_message)).show();
                    }
                }
            });
        }
    }

    public void logPurchaseKPI(){
        Answers.getInstance().logPurchase(new PurchaseEvent()
                .putCurrency(Currency.getInstance(CURRENCY_CODE_USD))
                .putItemName("app_smart_year")
                .putItemId(addOn.getSku())
                .putItemPrice(BigDecimal.valueOf(((double)total) / 100.0))
                .putSuccess(true));
    }
}
