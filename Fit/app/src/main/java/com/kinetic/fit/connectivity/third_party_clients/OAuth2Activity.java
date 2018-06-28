package com.kinetic.fit.connectivity.third_party_clients;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.kinetic.fit.util.ViewStyling;

import okhttp3.HttpUrl;

public abstract class OAuth2Activity extends Activity {

    public static final String TAG = "OAuth2Activity";
    public static final String AUTH_TOKEN = "AUTH_TOKEN";

    abstract String getAuthorizationUrl();

    abstract String getRedirectUri();

    abstract String getResponseType();

    abstract String getStateCode();

    WebView webView;
    ProgressDialog progressDialog;
    boolean systemBrowserRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!getSystemBrowserRequired()) {
            LinearLayout contentView = new LinearLayout(this);
            webView = new WebView(this);
            contentView.addView(webView);
            setContentView(contentView);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } else {
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getBaseContext());
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }
            webView.getSettings().setJavaScriptEnabled(true);
            webView.clearCache(true);
            webView.getSettings().setAppCacheEnabled(false);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.requestFocus(View.FOCUS_DOWN);

            progressDialog = ProgressDialog.show(this, "", "Loading..", true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Log.d(TAG, "url = " + url);
                    if (url.startsWith(getRedirectUri())) {
                        Uri uri = Uri.parse(url);

                        String stateCode = uri.getQueryParameter("state");
                        String authToken = uri.getQueryParameter(getResponseType());

                        if (authToken == null) {
                            setResult(RESULT_CANCELED);
                        } else if (stateCode != null && getStateCode() != null && !stateCode.equals(getStateCode())) {
                            // CSRF attack. Fuck you hackers.
                            setResult(RESULT_CANCELED);
                        } else {
                            Intent data = new Intent();
                            data.putExtra(AUTH_TOKEN, authToken);
                            setResult(RESULT_OK, data);
                        }
                        OAuth2Activity.this.finish();
                    } else {
                        webView.loadUrl(url);
                    }
                    return true;
                }
            });
            webView.loadUrl(getAuthorizationUrl());
        } else { /** THIS IS FOR STRAVA BECAUSE FUCK YOU GOOGLE**/
            HttpUrl authorizeUrl = HttpUrl.parse(getAuthorizationUrl())
                    .newBuilder() //
                    .build();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(String.valueOf(authorizeUrl.url())));
            startActivity(i);
//            finish();
        }
    }

    boolean getSystemBrowserRequired() {
        return systemBrowserRequired;
    }
}
