package com.kinetic.fit.ui.root;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.cast.FitCastService;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanDay;
import com.kinetic.fit.data.realm_objects.TrainingPlanProgress;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.databinding.ActivityRootBinding;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.displays.DisplaysActivity_;
import com.kinetic.fit.ui.settings.profile.ProfileActivity_;
import com.kinetic.fit.ui.settings.profile.SocialActivity_;
import com.kinetic.fit.ui.trainingplans.TrainingPlanActivity_;
import com.kinetic.fit.ui.trainingplans.TrainingPlanDayActivity;
import com.kinetic.fit.ui.trainingplans.TrainingPlanDayActivity_;
import com.kinetic.fit.ui.trainingplans.TrainingPlanOverViewActivity_;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.video.VideoController_;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.workout.CategoryActivity_;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;
import com.kinetic.fit.viewModels.ProfileViewModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

@EActivity
public class RootActivity extends FitActivity {

    private static final String TAG = "RootActivity";
    private static final int MAX_OFF_SCREEN = 0;

    @Bean
    VideoController videoController;

    @ViewById(R.id.history_session_list)
    RecyclerView recyclerView;

    @ViewById(R.id.button_left)
    FitButton buttonLeft;

    @ViewById(R.id.button_middle)
    FitButton buttonMiddle;

    @ViewById(R.id.button_right)
    FitButton buttonRight;

    @ViewById(R.id.header_distance_text)
    TextView mTotalDistanceTextView;

    @ViewById(R.id.header_duration_text)
    TextView mTotalDurationTextView;

    @ViewById(R.id.header_ftp_text)
    TextView mCurrentFtpTextView;

    @ViewById(R.id.header_kilojoules_text)
    TextView mTotalKilojoulesTextView;

    @ViewById(R.id.swipe_container_root)
    SwipeRefreshLayout swipeLayout;

    private HistoryRecyclerAdapter mAdapter;
    Realm realm;
    RealmResults<Session> mSessionROs;
    TrainingPlanProgress mTrainingPlanProgress;
    TrainingPlanCardListener trainingPlanCardListener;
    TSSCardListener tssCardListener;
    RealmResults<Session> badSessions;

    TrainingPlanDay today;

    ProgressDialog mProgressDialog;
    FitAlertDialog kitKatAlertDialog;

    ProfileViewModel mProfileVM;

    @Click(R.id.button_right)
    void onButtonRide() {
        CategoryActivity_.intent(this).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        ActivityRootBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_root);
        mProfileVM = ViewModelProviders.of(this).get(ProfileViewModel.class);
        binding.setProfile(mProfileVM);
        binding.setLifecycleOwner(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String actionTitleBarTitle = Profile.current() != null ? Profile.current().getName() : "Kinetic Fit";
        setTitle(actionTitleBarTitle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please grant location access");
                builder.setMessage("This app needs location access to connect to BLE Sensors");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        ActivityCompat.requestPermissions(RootActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1010);
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLifetimeValues();
        if (trainingPlanCardListener != null) {
            fetchSessionsFromRealm();
            setupTrainingPlanCard();
        }
        if (tssCardListener != null) {
            tssCardListener.setTSSValues(getTSSValues());
        }
        mSessionROs.addChangeListener(new RealmChangeListener<RealmResults<Session>>() {
            @Override
            public void onChange(RealmResults<Session> element) {
                if (mAdapter != null) {
                    mAdapter.setSessionList(mSessionROs);
                }
            }
        });
    }

    @AfterViews
    void afterViews() {
        fetchSessionsFromRealm();
        buttonLeft.setVisibility(View.INVISIBLE);
        buttonMiddle.setVisibility(View.GONE);
        buttonRight.setText("Workout");
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchSessionsFromRealm();
                refreshLifetimeValues();
                if (tssCardListener != null) {
                    tssCardListener.setTSSValues(getTSSValues());
                }
            }
        });
        swipeLayout.setProgressBackgroundColorSchemeColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        swipeLayout.setColorSchemeResources(R.color.white, R.color.black);
        swipeLayout.setSize(SwipeRefreshLayout.LARGE);
        if (Profile.current() != null) {
            autoScanDevices();
        }
        instantiateAdapter();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            kitKatAlertDialog = FitAlertDialog.show(this,
                    getString(R.string.important).toUpperCase(Locale.getDefault()),
                    getString(R.string.no_longer_supporting_this_version_of_android),
                    null,
                    getString(R.string.okay),
                    null,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            kitKatAlertDialog.dismiss();
                        }
                    },
                    false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSessionROs.removeAllChangeListeners();
    }

    @Override
    public void onDestroy() {
//        Log.d(TAG, "onTerminate");
        realm.close();
        videoController.wifiLockRelease();
        FitCastService.stopService();
        super.onDestroy();
    }

    @Override
    protected List<FitMenuItem> getMenuItems() {
        List<FitMenuItem> items = super.getMenuItems();
        items.add(new FitMenuItem("Profile", R.drawable.material_icon_person) {
            protected void activate() {
                super.activate();
                ProfileActivity_.intent(RootActivity.this).start();
            }
        });
        items.add(new FitMenuItem("Chromecast", R.drawable.material_icon_cast) {
            protected void activate() {
                super.activate();
                VideoController_.getInstance_(RootActivity.this).selectChromeCast(RootActivity.this);
            }
        });
        items.add(new FitMenuItem("Displays", R.drawable.material_icon_dashboard) {
            protected void activate() {
                DisplaysActivity_.intent(RootActivity.this).start();
            }
        });
        items.add(new FitMenuItem("Connections", R.drawable.material_icon_share) {
            protected void activate() {
                super.activate();
                SocialActivity_.intent(RootActivity.this).start();
            }
        });
        items.add(new FitMenuItem("Workouts", R.drawable.material_icon_bike) {
            protected void activate() {
                super.activate();
                CategoryActivity_.intent(RootActivity.this).start();
            }
        });
        items.add(new FitMenuItem("Training Plans", R.drawable.material_icon_calendar) {
            protected void activate() {
                TrainingPlanActivity_.intent(RootActivity.this).start();
            }
        });
//        items.add(new FitMenuItem("DumbActivity", R.drawable.material_icon_cast) {
//            protected void activate() {
//                super.activate();
////                SensorUpdateActivity_.intent(RootActivity.this).start();
//                Intent i = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_STARTED);
//                sendBroadcast(i);
//                }
//        });
        return items;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1010: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Try Again");
                    builder.setMessage("This app is worthless without sensors... Try that again...");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            ActivityCompat.requestPermissions(RootActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1010);
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void instantiateAdapter() {
        mAdapter = new HistoryRecyclerAdapter(mSessionROs);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
        swipeLayout.setRefreshing(false);
    }

    void fetchSessionsFromRealm() {
        mSessionROs = realm.where(Session.class)
                .notEqualTo("parseFlag", Session.DELETE_FLAG)
                .isNotNull("workoutName")
                .isNotNull("workoutDescription")
                .sort("workoutDate", Sort.DESCENDING)
                .findAll();
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        mTrainingPlanProgress = realm.where(TrainingPlanProgress.class).isNull("finishDate").findFirst();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @UiThread
    void refreshLifetimeValues() {
        Profile profile = Profile.current();
        if (profile == null) {
            return;
        }
        double distanceKM = profile.getTotalDistanceKM();
        double kjs = profile.getTotalKilojoules();
        double time = profile.getTotalTime();
        int ftp = profile.getPowerFTP();
//        mTotalKilojoulesTextView.setText(String.format("%.2f kJ", kjs));
        mTotalDurationTextView.setText(ViewStyling.timeToStringHM(time));
        mCurrentFtpTextView.setText(ftp + " FTP");
        if (SharedPreferencesInterface.isMetric()) {
            mTotalDistanceTextView.setText(String.format("%.2f km", distanceKM));
        } else {
            mTotalDistanceTextView.setText(String.format("%.2f mi", Conversions.kph_to_mph(distanceKM)));
        }
    }

    void autoScanDevices() {
        sendBroadcast(new Intent(SessionController.START_SENSOR_SCAN));
    }

    void setupTrainingPlanCard() {
        if (mTrainingPlanProgress == null) {
            showNoTrainingPlanCard();
        } else {
            if (mTrainingPlanProgress.getTrainingPlan() == null) {
                showNoTrainingPlanCard();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mTrainingPlanProgress.setFinishDate(new Date());
                    }
                });
            } else {
                DateTime start = new DateTime(mTrainingPlanProgress.getStartDate());
                DateTime now = new DateTime();
                ((HistoryRecyclerAdapter.TPViewHolder) trainingPlanCardListener).planProgress.setVisibility(View.VISIBLE);
                TrainingPlan tp = mTrainingPlanProgress.getTrainingPlan();
                trainingPlanCardListener.setMessageTitle(tp.getPlanName().toUpperCase());
                int planDay = Days.daysBetween(start.toDateMidnight(), now.toDateMidnight()).getDays() + 1;
                if (planDay > mTrainingPlanProgress.getTrainingPlan().getTotalDays()) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            mTrainingPlanProgress.setFinishDate(new Date());
                            realm.copyToRealmOrUpdate(mTrainingPlanProgress);
                            mTrainingPlanProgress = null;
                            setupTrainingPlanCard();
                        }
                    });
                }
                if (mTrainingPlanProgress != null && start.toDateMidnight().getMillis() <= now.toDateMidnight().getMillis()) {
                    double progress = (double) planDay / (double) tp.getTotalDays() * 100.0;
                    trainingPlanCardListener.setPlanProgressBar((int) progress);
                    for (TrainingPlanDay day : tp.getTrainingPlanDays()) {
                        if (day.getDay() == planDay) {
                            today = day;
                            trainingPlanCardListener.setTodaysMessage(day.getInstructions());
                            trainingPlanCardListener.setHeadline(today.getName());
                            if (day.getWorkout() == null) {
                                trainingPlanCardListener.setButtonText(getString(R.string.rest));
                            } else {
                                trainingPlanCardListener.setButtonText(getString(R.string.ride));
                            }
                            trainingPlanCardListener.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    TrainingPlanDayActivity_.intent(RootActivity.this)
                                            .extra(TrainingPlanDayActivity.TP_EXTRA, mTrainingPlanProgress.getTrainingPlan().getObjectId())
                                            .extra(TrainingPlanDayActivity.TPDAY_EXTRA, today == null ? null : today.getObjectId())
                                            .start();
                                }
                            });
                            break;
                        } else if (day.getDay() > planDay) {
                            trainingPlanCardListener.setMessageTitle(mTrainingPlanProgress.getTrainingPlan().getPlanName().toUpperCase());
                            trainingPlanCardListener.setHeadline(getString(R.string.training_plan_day_no_workout_text));
                            trainingPlanCardListener.setTodaysMessage("");
                            trainingPlanCardListener.setButtonText(getString(R.string.training_plan_day_view_plan_button_text));
                            trainingPlanCardListener.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    TrainingPlanOverViewActivity_.intent(RootActivity.this)
                                            .extra("planId", mTrainingPlanProgress.getTrainingPlan().getObjectId())
                                            .start();
                                }
                            });
                        }
                    }
                } else if (mTrainingPlanProgress != null && start.toDateMidnight().getMillis() > now.toDateMidnight().getMillis()) {
                    trainingPlanCardListener.setMessageTitle(mTrainingPlanProgress.getTrainingPlan().getPlanName().toUpperCase());
                    trainingPlanCardListener.setHeadline(getString(R.string.training_plan_day_no_workout_text));
                    trainingPlanCardListener.setTodaysMessage(getString(R.string.training_plan_starts_soon_message));
                    trainingPlanCardListener.setPlanProgressBar(0);
                    trainingPlanCardListener.setButtonText(getString(R.string.training_plan_day_view_plan_button_text));
                    trainingPlanCardListener.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            TrainingPlanOverViewActivity_.intent(RootActivity.this)
                                    .extra("planId", mTrainingPlanProgress.getTrainingPlan().getObjectId())
                                    .start();
                        }
                    });
                }
            }
        }
    }

    void setTrainingPlanCardListener(TrainingPlanCardListener listener) {
        this.trainingPlanCardListener = listener;
        setupTrainingPlanCard();
    }

    interface TrainingPlanCardListener {
        void setMessageTitle(String text);

        void setTodaysMessage(String text);

        void setButtonText(String text);

        void setOnClickListener(View.OnClickListener listener);

        void setPlanProgressBar(int progress);

        void hideButton();

        void setHeadline(String text);
    }

    void setTSSCardListener(TSSCardListener listener) {
        this.tssCardListener = listener;
    }

    interface TSSCardListener {
        void setTSSValues(HashMap<String, Integer> map);
    }

    View.OnClickListener noPlanClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TrainingPlanActivity_.intent(RootActivity.this)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
        }
    };

    View.OnClickListener planDayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TrainingPlanDayActivity_.intent(RootActivity.this)
                    .extra(TrainingPlanDayActivity.TP_EXTRA, mTrainingPlanProgress.getTrainingPlan().getObjectId())
                    .extra(TrainingPlanDayActivity.TPDAY_EXTRA, today == null ? null : today.getObjectId())
                    .start();
        }
    };

    HashMap<String, Integer> getTSSValues() {
        double thisWeekTssAgreggate = 0;
        double lastWeekTssAgreggate = 0;
        double thisMonthTssAgreggate = 0;
        double lastMonthTssAgreggate = 0;
        Date aWeekAgo = new DateTime(System.currentTimeMillis()).minusDays(7).toDateMidnight().toDate();
        Date twoWeeksAgo = new DateTime(System.currentTimeMillis()).minusDays(14).toDateMidnight().toDate();
        Date aMonthAgo = new DateTime(System.currentTimeMillis()).minusDays(30).toDateMidnight().toDate();
        Date twoMonthsAgo = new DateTime(System.currentTimeMillis()).minusDays(60).toDateMidnight().toDate();
        RealmResults<Session> thisWeekResults = realm.where(Session.class)
                .greaterThan("workoutDate", aWeekAgo)
                .findAll();
        RealmResults<Session> lastWeekResults = realm.where(Session.class)
                .greaterThan("workoutDate", twoWeeksAgo)
                .lessThan("workoutDate", aWeekAgo)
                .findAll();
        RealmResults<Session> thisMonthResults = realm.where(Session.class)
                .greaterThan("workoutDate", aMonthAgo)
                .findAll();
        RealmResults<Session> lastMonthResults = realm.where(Session.class)
                .greaterThan("workoutDate", twoMonthsAgo)
                .lessThan("workoutDate", aMonthAgo)
                .findAll();
        for (Session session : thisWeekResults) {
            thisWeekTssAgreggate += Math.max(session.getTrainingStressScore(), 0);
        }
        for (Session session : lastWeekResults) {
            lastWeekTssAgreggate += Math.max(session.getTrainingStressScore(), 0);
        }
        for (Session session : thisMonthResults) {
            thisMonthTssAgreggate += Math.max(session.getTrainingStressScore(), 0);
        }
        for (Session session : lastMonthResults) {
            lastMonthTssAgreggate += Math.max(session.getTrainingStressScore(), 0);
        }

        HashMap<String, Integer> map = new HashMap<>();
        map.put("thisWeek", (int) thisWeekTssAgreggate);
        map.put("lastWeek", (int) lastWeekTssAgreggate);
        map.put("thisMonth", (int) thisMonthTssAgreggate);
        map.put("lastMonth", (int) lastMonthTssAgreggate);
        return map;
    }

    private void showNoTrainingPlanCard() {
        trainingPlanCardListener.setMessageTitle(getString(R.string.train_smarter));
        trainingPlanCardListener.setButtonText(getString(R.string.start_a_training_plan));
        trainingPlanCardListener.setTodaysMessage(getString(R.string.root_training_plan_no_plan_body_text));
        trainingPlanCardListener.setHeadline("");
        trainingPlanCardListener.setOnClickListener(noPlanClickListener);
        ((HistoryRecyclerAdapter.TPViewHolder) trainingPlanCardListener).planProgress.setVisibility(View.GONE);
    }
}
