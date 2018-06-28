package com.kinetic.fit.ui.analysis;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.garmin.fit.FitRuntimeException;
import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient_;
import com.kinetic.fit.connectivity.third_party_clients.GoogleFitClient;
import com.kinetic.fit.connectivity.third_party_clients.GoogleFitClient_;
import com.kinetic.fit.connectivity.third_party_clients.StravaClient;
import com.kinetic.fit.connectivity.third_party_clients.StravaClient_;
import com.kinetic.fit.connectivity.third_party_clients.TrainingPeaksClient;
import com.kinetic.fit.connectivity.third_party_clients.TrainingPeaksClient_;
import com.kinetic.fit.connectivity.third_party_clients.TwoPeakClient;
import com.kinetic.fit.connectivity.third_party_clients.TwoPeakClient_;
import com.kinetic.fit.connectivity.third_party_clients.UAClient;
import com.kinetic.fit.connectivity.third_party_clients.UAClient_;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
import com.kinetic.fit.data.session_objects.SessionLap;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.exporting.KINKineticClient;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.root.RootActivity_;
import com.kinetic.fit.ui.settings.SensorsActivity_;
import com.kinetic.fit.ui.settings.SettingsActivity_;
import com.kinetic.fit.ui.support.SupportActivity_;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.FitGraphView;
import com.kinetic.fit.ui.widget.FitProgressDialog;
import com.kinetic.fit.ui.widget.FitSessionStatsWidget;
import com.kinetic.fit.ui.workout.OverviewActivity_;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.FitAnalytics;
import com.kinetic.fit.util.FitRateApp;
import com.kinetic.fit.util.ViewStyling;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.view.View.GONE;

/**
 * Created by Saxton on 3/24/17.
 */

public class AnalysisActivity extends FitActivity {
    public static final String EXTRA_SESSION_UUID = "sessionId";
    public static final String END_OF_WORKOUT = "End of Workout";
    public static final String TAG = "AnalysisActivity";
    ViewPager lapViewPager;
    TabLayout tabLayout;
    AppBarLayout appBar;
    FitGraphView graph;
    TextView workoutName;
    TextView workoutDate;
    FitSessionStatsWidget scrollingStats;
    FitSessionStatsWidget toolbarStats;
    TextView powerAvg;
    TextView heartAvg;
    TextView cadenceAvg;
    TextView speedAvg;
    TextView powerMax;
    TextView heartMax;
    TextView cadenceMax;
    TextView speedMax;
    FitButton leftButton;
    FitButton middleButton;
    FitButton rightButton;
    TextView newFtpText;
    Session session;
    int profileFTP;
    Realm realm;
    Profile mProfile;
    private PagerAdapter mPagerAdapter;
    String sessionId;
    FitProgressDialog mProgressDialog;
    ArrayList<SessionDataSlice> slices;
    ArrayList<SessionLap> laps;
    RealmResults<Subscription> subscriptions;
    boolean hasSubscription = false;
    FitAlertDialog mAlertDialog;

    private DataSync.DataSyncBinder mDataSyncBinder;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSyncBinder = (DataSync.DataSyncBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mDataSyncBinder = null;
        }
    };

    protected StravaClient stravaClient;
    protected UAClient uaClient;
    protected TrainingPeaksClient trainingPeaksClient;
    protected TwoPeakClient twoPeakClient;
    protected DropboxClient dropboxClient;
    protected GoogleFitClient googleFitClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        mProgressDialog = FitProgressDialog.show(this, getString(R.string.loading), getString(R.string.loading_wait_analyze));
        sessionId = getIntent().getStringExtra(EXTRA_SESSION_UUID);
        getSupportActionBar().setTitle("Session Analysis");
        setContentView(R.layout.activity_analysis);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
        mProfile = Profile.current();
        profileFTP = mProfile.getPowerFTP();
        subscriptions = realm.where(Subscription.class).findAll();
        for (Subscription s : subscriptions) {
            if (!s.isCancelled()) {
                hasSubscription = true;
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_analysis, menu);
        return true;
    }

    private void setStats() {
        toolbarStats.setDuration(session.getDuration());
        scrollingStats.setDuration(session.getDuration());
        toolbarStats.setCalories(session.getCaloriesBurned());
        scrollingStats.setCalories(session.getCaloriesBurned());
        toolbarStats.setDistance(session.getDistanceKM(), SharedPreferencesInterface.isMetric());
        scrollingStats.setDistance(session.getDistanceKM(), SharedPreferencesInterface.isMetric());
        powerAvg.setText(session.getAvgPower() > 0 ? getString(R.string.watt_formatter_integer, (int) session.getAvgPower()) : getString(R.string.empty_string));
        heartAvg.setText(session.getAvgHeartRate() > 0 ? getString(R.string.heart_rate_formatter, (int) session.getAvgHeartRate()) : getString(R.string.empty_string));
        cadenceAvg.setText(session.getAvgCadence() > 0 ? getString(R.string.analysis_cadence_formatter, (int) session.getAvgCadence()) : getString(R.string.empty_string));
        if (session.getAvgSpeedKPH() > 0) {
            if (SharedPreferencesInterface.isMetric()) {
                speedAvg.setText(getString(R.string.analysis_speed_formatter_kph, session.getAvgSpeedKPH()));
            } else {
                speedAvg.setText(getString(R.string.analysis_speed_formatter_mph, Conversions.kph_to_mph(session.getAvgSpeedKPH())));
            }
        } else {
            speedAvg.setText(R.string.empty_string);
        }
        powerMax.setText(session.getMaxPower() > 0 ? getString(R.string.watt_formatter_integer, (int) session.getMaxPower()) : getString(R.string.empty_string));
        heartMax.setText(session.getMaxHeartRate() > 0 ? getString(R.string.heart_rate_formatter, (int) session.getMaxHeartRate()) : getString(R.string.empty_string));
        cadenceMax.setText(session.getMaxCadence() > 0 ? getString(R.string.analysis_cadence_formatter, (int) session.getMaxCadence()) : getString(R.string.empty_string));
        if (session.getMaxSpeedKPH() > 0) {
            if (SharedPreferencesInterface.isMetric()) {
                speedMax.setText(getString(R.string.analysis_speed_formatter_kph, session.getMaxSpeedKPH()));
            } else {
                speedMax.setText(getString(R.string.analysis_speed_formatter_mph, Conversions.kph_to_mph(session.getMaxSpeedKPH())));
            }
        } else {
            speedMax.setText(getString(R.string.empty_string));
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpDrawer();
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                if (session != null) {
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Session s = realm.where(Session.class).equalTo("uuid", sessionId).findFirst();
                            s.rebuild();
                            slices = s.getDataSlices();
                            laps = s.laps;
                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {
                            session.setDataSlices(slices);
                            session.setLaps(laps);
                            graph.setSession(session);
                            lapViewPager.setAdapter(mPagerAdapter);
                            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                            tabLayout.setupWithViewPager(lapViewPager);
                            setStats();
                            setFooter();
                            // close dialog
                            dismissDialog();
                            mPagerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };
        handler.postDelayed(r, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViews();
        if (session == null) {
            fetchSessionAndUpdateDependentViews();
        }
        stravaClient = StravaClient_.getInstance_(this);
        uaClient = UAClient_.getInstance_(this);
        trainingPeaksClient = TrainingPeaksClient_.getInstance_(this);
        twoPeakClient = TwoPeakClient_.getInstance_(this);
        dropboxClient = DropboxClient_.getInstance_(this);
        googleFitClient = GoogleFitClient_.getInstance_(AnalysisActivity.this);
        googleFitClient.setContext(this);
        if (getIntent().getBooleanExtra(END_OF_WORKOUT, true)) {
            if (FitRateApp.askToRateApp(session)) {
                dismissDialog();
                mAlertDialog = FitAlertDialog.show(this, "Rate Us", "Enjoy your ride? Leave us a review and let us know how it went!", "No", "OK",
                        null,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (mAlertDialog != null) {
                                        mAlertDialog.dismiss();
                                    }
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kinetic.fit")));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.kinetic.fit")));
                                }
                            }
                        }, true);
            }
            setRateWait();
            doAutoSaveWork();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissDialog();
    }

    @Override
    protected void onDestroy() {
        unbindService(mDataSyncConnection);
        realm.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export_file) {
            exportSession();
            return true;
        }
        if (id == R.id.action_delete) {
            deleteSession();
            return true;
        }
        if (id == R.id.action_export_strava) {
            if (stravaClient.isConnected()) {
                stravaClient.uploadSession(sessionId, session.getWorkoutName(), session.getWorkoutDescription());
                return true;
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Strava not Authorized yet. Please go to your profile " +
                        "and authorize it").show();
                return false;
            }
        }
        if (id == R.id.action_export_mappmyfitness) {
            if (uaClient.isConnected()) {
                uaClient.uploadSession(sessionId);
                return true;
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(), "MapMyFitness not Authorized yet. Please go to your profile " +
                        "and authorize it").show();
                return false;
            }
        }
        if (id == R.id.action_export_trainingpeaks) {
            if (trainingPeaksClient.isConnected()) {
                trainingPeaksClient.uploadSession(sessionId, session.getWorkoutName(), session.getExportFileName());
                return true;
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(),
                        "Training Peaks is not Authorized yet. Please go to your profile " +
                                "and authorize it").show();
                return false;
            }
        }
        if (id == R.id.action_export_2peak) {
            if (twoPeakClient.isConnected()) {
                twoPeakClient.uploadSession(sessionId);
                return true;
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(), "2Peak is not Authorized yet. Please go to your profile " +
                        "and authorize it").show();
                return false;
            }
        }
        if (id == R.id.action_export_dropbox) {
            if (dropboxClient.isConnected()) {
                dropboxClient.uploadSession(sessionId, session.getExportFileName());
                return true;
            } else {
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Dropbox is not authorixed yet." +
                        " Please got to your profile and auhorixe it").show();
            }
        }
        if (id == R.id.action_share_twitter) {

            TweetComposer.Builder builder = new TweetComposer.Builder(AnalysisActivity.this)
                    .text(getString(R.string.twitter_formatter,
                            ViewStyling.timeToStringHMS(session.getDuration(), false),
                            (int) session.getAvgPower(),
                            session.getMaxPower()))
                    .image(getImageUri(R.drawable.twitter_header));
            builder.show();
            FitAnalytics.sendShareKPI("Twitter");
        }
        if (id == R.id.action_share_fit) {
            googleFitClient.sendToGoogleFit(sessionId);
        }

        if (id == R.id.export_to_csv) {
            exportSessionCsv();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setViews() {
        mDrawerLeft = findViewById(R.id.drawer_left);
        mDrawerList = ((ListView) findViewById(R.id.drawer_list));
        mDrawerLayout = ((DrawerLayout) findViewById(R.id.drawer_layout));
        mDrawerProfileName = ((TextView) findViewById(R.id.drawer_profile_name));
        mVersionText = ((TextView) findViewById(R.id.drawer_version_text));
        mTitleText = ((TextView) findViewById(R.id.fit_activity_title));
        lapViewPager = ((ViewPager) findViewById(R.id.lapViewpager));
        appBar = ((AppBarLayout) findViewById(R.id.appbar));
        graph = ((FitGraphView) findViewById(R.id.analysis_graph));
        workoutName = ((TextView) findViewById(R.id.analysis_session_workout_name));
        workoutDate = ((TextView) findViewById(R.id.analysis_session_date));
        scrollingStats = ((FitSessionStatsWidget) findViewById(R.id.analysis_scrolling_stats_bar));
        toolbarStats = ((FitSessionStatsWidget) findViewById(R.id.analysis_toolbar_layout));
        powerAvg = ((TextView) findViewById(R.id.analysis_overview_power_avg));
        heartAvg = ((TextView) findViewById(R.id.analysis_overview_heart_avg));
        cadenceAvg = ((TextView) findViewById(R.id.analysis_overview_cadence_avg));
        speedAvg = ((TextView) findViewById(R.id.analysis_overview_speed_avg));
        powerMax = ((TextView) findViewById(R.id.analysis_overview_power_max));
        heartMax = ((TextView) findViewById(R.id.analysis_overview_heart_max));
        cadenceMax = ((TextView) findViewById(R.id.analysis_overview_cadence_max));
        speedMax = ((TextView) findViewById(R.id.analysis_overview_speed_max));
        leftButton = ((FitButton) findViewById(R.id.button_left));
        middleButton = ((FitButton) findViewById(R.id.button_middle));
        rightButton = ((FitButton) findViewById(R.id.button_right));
        leftButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
        leftButton.setText("Close");
        middleButton.setFitButtonStyle(FitButton.BASIC);
        middleButton.setText("Set New FTP");
        middleButton.setVisibility(View.GONE);
        tabLayout = (TabLayout) findViewById(R.id.lapTabs);
        mPagerAdapter = new AnalysisPagerAdapter(getSupportFragmentManager());
        newFtpText = ((TextView) findViewById(R.id.analysis_overview_new_ftp_text));
        if (middleButton != null) {
            middleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNewFTP();
                }
            });
        }
        if (leftButton != null) {
            leftButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeAnalysis();
                }
            });
        }
        if (rightButton != null) {
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reRide();
                }
            });
        }
        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Rect scrollBounds = new Rect();
                appBar.getHitRect(scrollBounds);
                float top = scrollingStats.getY();
                if (scrollBounds.top + top < -top) {
                    toolbarStats.setVisibility(View.VISIBLE);
                } else {
                    toolbarStats.setVisibility(GONE);
                }
            }
        });


    }

    void fetchSessionAndUpdateDependentViews() {
        session = realm.where(Session.class).equalTo("uuid", sessionId).findFirst();
        if (session == null) {
            finish();
            ViewStyling.getCustomToast(this, getLayoutInflater(), getString(R.string.analysis_error_loading_session)).show();
        } else {
            workoutName.setText(session.getWorkoutName());
            workoutDate.setText(session.getFormattedWorkoutDate());
            setStats();
            setFooter();
        }
    }


    void setNewFTP() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mProfile.setPowerFTP(session.getCalculatedFTP());
                mProfile.autoCalculatePowerZones();
                realm.copyToRealmOrUpdate(mProfile);
            }
        });
        middleButton.setVisibility(View.GONE);
        newFtpText.setText(getString(R.string.analysis_overview_new_ftp_set_formatter, mProfile.getPowerFTP()));
    }

    void closeAnalysis() {
        RootActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
        finish();
    }

    void reRide() {
        String workoutid = session.getWorkoutId();
        OverviewActivity_.intent(this).extra("workoutId", workoutid).start();
    }

    private void deleteSession() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        mAlertDialog = FitAlertDialog.show(this,
                getString(R.string.confirm_delete_session_title),
                getString(R.string.confirm_delete_session_message),
                getString(R.string.yes),
                getString(R.string.no),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDataSyncBinder.deleteSession(session);
                        RootActivity_.intent(AnalysisActivity.this)
                                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .start();
                        finish();
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAlertDialog.dismiss();
                    }
                },
                false);
    }

    private void exportSession() {
        Log.d("Analysis", "Sharing session");
        if (session != null) {
            try {
                Uri uri = KINKineticClient.encodeSession(this, sessionId, true);
                String fileName = session.getExportFileName();
                Intent fitExport = new Intent(Intent.ACTION_SEND);
                fitExport.setType("application/octet-stream");
                fitExport.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                fitExport.putExtra(Intent.EXTRA_SUBJECT, fileName);
                fitExport.putExtra(Intent.EXTRA_STREAM, uri);
                startActivityForResult(Intent.createChooser(fitExport, "Export FIT File"), 0);

            } catch (IOException e) {

            } catch (FitRuntimeException e) {

            }
        }
    }

    private void exportSessionCsv() {
        Log.d("Analysis", "Sharing session");
        if (session != null) {
            try {
                Uri uri = KINKineticClient.encodeSessionCSV(this, sessionId);
                //File file = new File(uri.getPath());
                String fileName = session.getExportFileName();
                Intent fitExport = new Intent(Intent.ACTION_SEND);
                fitExport.setType("text/csv");
                fitExport.putExtra(Intent.EXTRA_SUBJECT, fileName);
                fitExport.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.valueOf(uri)));
                startActivityForResult(Intent.createChooser(fitExport, "Export CSV"), 0);
            } catch (IOException e) {

            } catch (FitRuntimeException e) {

            }
        }
    }

    public void doAutoSaveWork() {
        stravaClient.autoShareWorkout(sessionId, session.getWorkoutName(), session.getWorkoutDescription());
        uaClient.autoShareWorkout(sessionId);
        twoPeakClient.autoShareWorkout(sessionId);
        trainingPeaksClient.autoShareWorkout(sessionId, session.getWorkoutName(), session.getExportFileName());
    }

    void setFooter() {
        if (session.getCalculatedFTP() > 0 && session.getCalculatedFTP() != profileFTP) {
            middleButton.setVisibility(View.VISIBLE);
            newFtpText.setText(getString(R.string.analysis_overview_new_ftp_formatter, session.getCalculatedFTP(), Profile.current().getPowerFTP()));
        } else {
            newFtpText.setVisibility(View.GONE);
        }
        if (session.getWorkoutId() != null) {
            rightButton.setFitButtonStyle(FitButton.DEFAULT);
            rightButton.setText("Re-Ride");
        } else {
            rightButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        RootActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
        finish();
    }

    private class AnalysisPagerAdapter extends FragmentPagerAdapter {

        public AnalysisPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            SessionDataSpanFragment fragment = null;
            fragment = SessionDataSpanFragment_.builder().build();
            fragment.setSession(session);
            fragment.setHasSubscription(hasSubscription);
//            TODO set subscription info on fragment here
            if (position == 0) {
                fragment.setSpan(session);
            } else {
                fragment.setSpan(session.getLaps().get(position - 1));
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return session.getLaps().size() + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Overview";
            }
            if (position == 1) {
                return "Warmup";
            } else {
                return "Lap " + (position - 1);
            }
        }
    }

    @Override
    protected List<FitMenuItem> getMenuItems() {
        List<FitMenuItem> items = new ArrayList<>();
        items.add(new FitMenuItem("Support", R.drawable.material_icon_help_outline) {
            protected void activate() {
                SupportActivity_.intent(AnalysisActivity.this).start();
                super.activate();
            }
        });
        items.add(new FitMenuItem("Settings", R.drawable.material_icon_settings) {
            protected void activate() {
                super.activate();
                SettingsActivity_.intent(AnalysisActivity.this).start();
            }
        });
        items.add(new FitMenuItem("Sensors", R.drawable.material_icon_bluetooth) {
            protected void activate() {
                super.activate();
                SensorsActivity_.intent(AnalysisActivity.this).start();
            }
        });
        return items;
    }

    public Uri getImageUri(int imageId) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), imageId);
        File f = null;
        try {
            f = new File(getFilesDir(), "twitterphoto.jpg");
            FileOutputStream fos = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileProvider.getUriForFile(getApplicationContext(), "com.kinetic.fit.ui.analysis.AnalysisActivity", f);
    }

    void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    void setRateWait() {
        SharedPreferencesInterface.setRequestRateApp(false);
        SharedPreferencesInterface.setWaitTime();
    }

}
