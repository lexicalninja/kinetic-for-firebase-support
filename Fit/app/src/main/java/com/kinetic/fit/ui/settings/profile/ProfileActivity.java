package com.kinetic.fit.ui.settings.profile;

import android.app.DatePickerDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.third_party_clients.OAuth2Activity;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.settings.SettingsActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.PlusMinusAttributeWidget;
import com.kinetic.fit.util.Conversions;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;


/**
 * Created by Saxton on 1/11/17.
 */

@EActivity(R.layout.activity_profile)
public class ProfileActivity extends FitActivity {
    private static final String TAG = "ProfileActivity";
    private static final int OFF_SCREEN_PAGE_LIMIT = 3;
    public static final String SOCIAL_CALLBACK_RECEIVED = "ProfileActivity.SOCIAL_CALLBACK_RECEIVED";

    @ViewById(R.id.profile_name_input)
    EditText nameEditText;
    @ViewById(R.id.profile_email_display_text)
    TextView emailText;
    @ViewById(R.id.profile_unit_selection_button)
    FitButton unitButton;
    @ViewById(R.id.profile_birthdate_button)
    FitButton birthdateButton;
    @ViewById(R.id.profile_weight_picker_widget)
    PlusMinusAttributeWidget weightWidget;
    @ViewById(R.id.profile_weight_unit_indicator)
    TextView weightUnitsText;
    @ViewById(R.id.profile_height_picker_widget)
    PlusMinusAttributeWidget heightWidget;
    @ViewById(R.id.profile_height_unit_indicator)
    TextView heightUnitsText;
    @ViewById(R.id.profile_ftp_picker_widget)
    PlusMinusAttributeWidget ftpWidget;
    @ViewById(R.id.profile_resting_heart_picker_widget)
    PlusMinusAttributeWidget restingHeartWidget;
    @ViewById(R.id.profile_max_heart_picker_widget)
    PlusMinusAttributeWidget maxHeartWidget;
    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;
    @ViewById(R.id.button_right)
    FitButton rightButton;

    boolean isLoggingOut = false;


    SharedPreferences sharedPreferences;
    DateFormat dtf;
    Profile mProfile;
    boolean isMetric;
    Realm realm;
    private DataSync.DataSyncBinder mDataSyncBinder;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSyncBinder = (DataSync.DataSyncBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSyncBinder = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(SettingsActivity.getSettingsNamespace(), MODE_PRIVATE);
        mProfile = Profile.current();
        isMetric = SharedPreferencesInterface.isMetric();
        dtf = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        realm = Realm.getDefaultInstance();
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode > 1 && requestCode <= 5) {
            Intent i = new Intent(SOCIAL_CALLBACK_RECEIVED).putExtra("requestCode", requestCode)
                    .putExtra("resultCode", resultCode)
                    .putExtra("token", data.getStringExtra(OAuth2Activity.AUTH_TOKEN));
            sendBroadcast(i);
        }

    }

    @AfterViews
    void afterViews() {
        nameEditText.setText(mProfile.getName());
        emailText.setText(mProfile.getEmail());
        setUnitButtons();
        birthdateButton.setText(dtf.format(mProfile.getBirthdate()));
        setWeightWidget();
        setHeightWidget();
        ftpWidget.setAttributeValue(mProfile.getPowerFTP());
        restingHeartWidget.setAttributeValue(mProfile.getHeartResting());
        maxHeartWidget.setAttributeValue(mProfile.getHeartMax());
        birthdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        unitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesInterface.setMetric(!isMetric);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        saveHeightWeight();
                    }
                });
                isMetric = !isMetric;
                setUnitButtons();
            }
        });
        leftButton.setVisibility(View.INVISIBLE);
        middleButton.setVisibility(View.INVISIBLE);
        rightButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
        rightButton.setText(R.string.profile_save_and_exit_button);
    }

    @Override
    protected void onDestroy() {
        if(!isLoggingOut) {
            if (mProfile != null & !realm.isInTransaction()) {
                save();
            }
            mDataSyncBinder.syncProfile();
        }
        unbindService(mDataSyncConnection);
        realm.close();
        super.onDestroy();
    }

    @Click(R.id.button_right)
    void saveAndExit() {
        finish();
    }

    public void setUnitButtons() {
        if (isMetric) {
            unitButton.setText(getString(R.string.profile_text_metric));
            weightUnitsText.setText(getString(R.string.profile_text_kilos));
            heightUnitsText.setText(getString(R.string.profile_text_centimeters));
        } else {
            unitButton.setText(getString(R.string.profile_text_imperial));
            weightUnitsText.setText(getString(R.string.profile_text_pounds));
            heightUnitsText.setText(getString(R.string.profile_text_inches));
        }
        setWeightWidget();
        setHeightWidget();
    }

    void setWeightWidget() {
        if (isMetric) {
            weightWidget.setAttributeValue((int) mProfile.getWeightKG());
        } else {
            weightWidget.setAttributeValue((int) Conversions.kg_to_lbs(mProfile.getWeightKG()));
        }
    }

    void setHeightWidget() {
        if (isMetric) {
            heightWidget.setAttributeValue((int) mProfile.getHeightCM());
        } else {
            heightWidget.setAttributeValue((int) Conversions.cm_to_inches(mProfile.getHeightCM()));
        }
    }

    private void showDatePicker() {
        if (getCurrentFocus() != null) {
            hideKeyboard();
        }
        DateTime dt = new DateTime(mProfile.getBirthdate(), DateTimeZone.UTC);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Log.d(TAG, "i: " + i + " i1: " + i1 + " i2: " + i2);
                final Date dt = new Date(i - 1900, i1, i2);
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mProfile.setBirthdate(dt);
                    }
                });
                birthdateButton.setText(dtf.format(dt));
            }
        }, dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth());
        dialog.show();
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void saveHeightWeight() {
        if (isMetric) {
            mProfile.setWeightKG(weightWidget.getAttributeValue());
            mProfile.setHeightCM(heightWidget.getAttributeValue());
        } else {
            mProfile.setWeightKG(Conversions.lbs_to_kg(weightWidget.getAttributeValue()));
            mProfile.setHeightCM(Conversions.inches_to_cm(heightWidget.getAttributeValue()));
        }
    }

    @Override
    protected List<FitMenuItem> getMenuItems() {
        List<FitMenuItem> items = super.getMenuItems();
        items.add(new FitMenuItem("Logout", R.drawable.material_icon_logout) {
            protected void activate() {
                super.activate();
                isLoggingOut = true;
                Profile.current().logOut(ProfileActivity.this);
                ActivityCompat.finishAffinity(ProfileActivity.this);

//                Intent intent = new Intent(getApplicationContext(), LoginDispatchActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
            }
        });
        return items;
    }

    private void save() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (nameEditText.getText() != null) {
                    mProfile.setName(nameEditText.getText().toString());
                }
                saveHeightWeight();
                mProfile.setPowerFTP(ftpWidget.getAttributeValue());
                mProfile.autoCalculatePowerZones();
                mProfile.setHeartResting(restingHeartWidget.getAttributeValue());
                mProfile.setHeartMax(maxHeartWidget.getAttributeValue());
                mProfile.autoCalculateHeartZones();
                realm.copyToRealmOrUpdate(mProfile);
            }
        });
    }

    void leaveProfile() {
        exit();
    }


    void exit() {
        RootActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
        finish();
    }

    @Override
    public void onBackPressed() {
        exit();
    }

}
