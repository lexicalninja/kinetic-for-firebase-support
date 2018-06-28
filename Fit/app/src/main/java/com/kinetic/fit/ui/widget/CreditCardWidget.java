package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.util.JsonFromFile;
import com.stripe.android.model.Card;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.card.payment.CardIOActivity;

/**
 * Created by Saxton on 5/3/17.
 */

public class CreditCardWidget extends ScrollView implements AdapterView.OnItemSelectedListener{
    public static final String TAG = "CCWidget";
    public static final int CC_SCAN_REQUEST_CODE = 200;

    EditText shippingName;
    EditText shippingAddress1;
    EditText shippingAddress2;
    EditText shippingCity;
    EditText shippingStateProv;
    EditText shippingPostalCode;
    Spinner shippingCountry;
    ImageView cameraIcon;
    CardInputWidget stripeWidget;

    HashMap<String, String> mCountries;
    ArrayList<String> countryCodes;
    String countryCode;

    public CreditCardWidget(Context context) {
        super(context);
        init();
    }

    public CreditCardWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CreditCardWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CreditCardWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init(){
        mCountries = new LinkedHashMap<>();
        mCountries = getCountryList();
        countryCodes = new ArrayList<>(mCountries.values());
        View.inflate(getContext(), R.layout.widget_credit_card, this);
        shippingName = (EditText) findViewById(R.id.shipping_name);
        shippingAddress1 = (EditText) findViewById(R.id.shipping_address1);
        shippingAddress2 = (EditText) findViewById(R.id.shipping_address2);
        shippingCity = (EditText) findViewById(R.id.shipping_city);
        shippingStateProv = (EditText) findViewById(R.id.shipping_state_prov);
        shippingPostalCode = (EditText) findViewById(R.id.shipping_post_code);
        shippingCountry = (Spinner) findViewById(R.id.shipping_country);
        stripeWidget = (CardInputWidget) findViewById(R.id.stripe_widget);
        cameraIcon = (ImageView) findViewById(R.id.camera_icon);
        cameraIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onScanPress();

            }
        });
        setUpSpinner();
    }
    public void onScanPress() {
        Intent scanIntent = new Intent(getContext(), CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

        // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
        ((Activity)getContext()).startActivityForResult(scanIntent, CC_SCAN_REQUEST_CODE);
    }

    public void setStripeCardInfo(String cardNum, int expMonth, int expYear, String cvc){
        stripeWidget.setCardNumber(cardNum);
        stripeWidget.setExpiryDate(expMonth, expYear);
        stripeWidget.setCvcCode(cvc);
    }

    public Card getCard(){
        return stripeWidget.getCard();
    }

    public String getAddress1(){
        return shippingAddress1.getText().toString();
    }

    public String getAddress2(){
        return shippingAddress2.getText().toString();
    }

    public String getCity(){
        return shippingCity.getText().toString();
    }

    public String getState(){
        return shippingStateProv.getText().toString();
    }

    public String getCountryCode(){
        return countryCode;
    }

    public String getPostalCode(){
        return shippingPostalCode.getText().toString();
    }

    public String getName(){
        return shippingName.getText().toString();
    }

    private HashMap<String, String> getCountryList(){
        HashMap<String, String> countries = new LinkedHashMap<>();
        try {
            JsonFromFile parser = new JsonFromFile(getContext());
            String s = parser.loadJSONFromAsset();
//                    Log.d(TAG, s);
            JSONArray array = new JSONArray(s);
            for( int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                countries.put(object.getString("name"), object.getString("alpha-2"));
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return countries;
    }

    void setUpSpinner(){
        List<String> keys = new ArrayList<>(mCountries.keySet());
        SpinnerArrayAdapter adapter = new SpinnerArrayAdapter(getContext(), android.R.layout.simple_spinner_item, mCountries);
        // Drop down layout style - list view with radio button
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        shippingCountry.setAdapter(adapter);
        shippingCountry.setOnItemSelectedListener(this);
        shippingCountry.setSelection(keys.indexOf("United States of America"));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        countryCode = countryCodes.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void registerSpinnerListener(AdapterView.OnItemSelectedListener listener){
        shippingCountry.setOnItemSelectedListener(listener);
    }

    public class SpinnerArrayAdapter extends ArrayAdapter<String> {

        private Context mContext;
        List<String> keys;
        List<String> values;
        private String mCustomText = "";

        public SpinnerArrayAdapter(Context context, int textViewResourceId,
                                   HashMap<String, String> map) {
            super(context, textViewResourceId, new ArrayList<String>(map.values()));
            this.mContext = context;
            this.keys = new ArrayList<>(map.keySet());
            this.values = new ArrayList<>(map.values());
        }

        public int getCount(){
            return keys.size();
        }

        public String getItem(int position){
            return values.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = new TextView(mContext);
            label.setText(values.get(position));
            countryCode = values.get(position);
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = new TextView(mContext);
            label.setText(keys.get(position));
            return label;
        }
    }


}
