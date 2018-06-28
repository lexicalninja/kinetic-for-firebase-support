package com.kinetic.fit.ui.workout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.realm_objects.Tag;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.subscriptions.SubscriptionStatusActivity_;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.video.select.VideoSelectActivity_;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.widget.WorkoutGraphView;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static android.view.View.GONE;

/**
 * Created by Saxton on 12/15/16.
 */

@EActivity(R.layout.activity_overview)
public class OverviewActivity extends FitActivity {

    private String mWorkoutId;
    private Workout mWorkout;
    static final int VIDEO_REQUEST_CODE = 34;
    static final int MAX_FREE_CUSTOM = 3;
    private boolean hasSubscription = false;
    private boolean isCustomWorkout = false;

    @ViewById(R.id.workout_overview_graph)
    WorkoutGraphView graphView;

    @ViewById(R.id.workout_overview_text_overview)
    TextView overviewText;

    @ViewById(R.id.workout_overview_text_video)
    TextView videoText;

    @ViewById(R.id.button_left)
    FitButton videoButton;

    @ViewById(R.id.button_middle)
    FitButton middleButton;

    @ViewById(R.id.button_right)
    FitButton warmupButton;

    @ViewById(R.id.workout_overview_workout_name)
    TextView workoutName;

    @ViewById(R.id.workout_overview_workout_category)
    TextView workoutCategory;

    @ViewById(R.id.workout_overview_favorite_button)
    ImageButton favoriteButton;
    @ViewById(R.id.workout_overview_workout_name_edit)
    EditText nameEdit;

    @Bean
    VideoController videoController;

    Realm realm;
    Profile mProfile;
    RealmResults<Subscription> subscriptions;
    String freeCustomString;
    ArrayList<String> freeCustom;
    InputMethodManager imm;

    boolean inFreeCustom = false;

    FitAlertDialog mAlertDialog;


    private SessionController.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController.SessionControllerBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWorkoutId = getIntent().getStringExtra("workoutId");
        realm = Realm.getDefaultInstance();
        mWorkout = realm.where(Workout.class).equalTo("objectId", mWorkoutId).findFirst();
        mProfile = Profile.current();
        freeCustom = new ArrayList<>();
        subscriptions = realm.where(Subscription.class).findAll();
        for (Subscription s : subscriptions) {
            if (s.isValid()) {
                hasSubscription = true;
                break;
            }
        }
        if (!hasSubscription) {
            freeCustomString = mProfile.getFreeCustom();
            if(freeCustomString != null) {
                for (String id : freeCustomString.replace("[", "").replace("]","").split("\\|")) {
                    freeCustom.add(id);
                }
            }
        }
        for (String id : freeCustom) {
            if (id.equals(mWorkoutId)) {
                inFreeCustom = true;
            }
        }
        if (mWorkout != null) {
            for (Tag tag : mWorkout.getTags()) {
                if (tag.getName().equalsIgnoreCase("custom")) {
                    isCustomWorkout = true;
                }
            }
        }
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("Workout Overview");
        bindService(SessionController_.intent(this).get(), mSessionConnection, BIND_AUTO_CREATE);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onBackPressed() {
        if (!videoController.videoIsNull()) {
            videoController.setVideo(null);
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            setVideoText();
        }
    }

    @AfterViews
    void afterCreateViews() {
        videoButton.setText("Video");
        warmupButton.setText("Warmup");
        middleButton.setVisibility(GONE);
        warmupButton.setFitButtonStyle(FitButton.DEFAULT);
        updateViews();
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFavorite();
            }
        });
        nameEdit.setFocusableInTouchMode(true);
        nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
               if(event != null && event.getKeyCode() != KeyEvent.ACTION_DOWN){
                    return false;
                } else if(actionId == EditorInfo.IME_ACTION_DONE){
                   if(!mWorkout.getName().equals(v.getText().toString())){
                       realm.executeTransaction(new Realm.Transaction() {
                           @Override
                           public void execute(Realm realm) {
                               mWorkout.setName(nameEdit.getText().toString().trim());
                               mWorkout = realm.copyToRealmOrUpdate(mWorkout);
                           }
                       });
                       mDataSync.updateCustomWorkout(mWorkout);
                       workoutName.setText(nameEdit.getText().toString());
                       nameEdit.setVisibility(GONE);
                       workoutName.setVisibility(View.VISIBLE);
                       imm.hideSoftInputFromInputMethod(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                       return true;
                   } else {
                       imm.hideSoftInputFromInputMethod(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                       return false;
                   }
               }
                imm.hideSoftInputFromInputMethod(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return false;
            }
        });
    }

    @Click(R.id.button_left)
    void onVideoButton() {
        VideoSelectActivity_.intent(this).startForResult(VIDEO_REQUEST_CODE);
    }

    @Click(R.id.button_right)
    void onWarmupButton() {
        if (!hasSubscription && isCustomWorkout && !inFreeCustom) {
            if (freeCustom.size() <= MAX_FREE_CUSTOM) {
                mAlertDialog = FitAlertDialog.show(this,
                        getString(R.string.subscription_limited_custom_workouts_title),
                        getString(R.string.subscription_free_custom_workouts_message, MAX_FREE_CUSTOM, MAX_FREE_CUSTOM),
                        getString(R.string.no),
                        getString(R.string.add),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                freeCustom.add(mWorkoutId);
                                freeCustomString = freeCustom.toString().replace(", ", "|");
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        mProfile.setFreeCustom(freeCustomString);
                                        mDataSync.syncProfile();
                                    }
                                });
                                startWarmup();
                            }
                        },
                        true
                );
            } else {
                mAlertDialog = FitAlertDialog.show(this,
                        getString(R.string.subscription_activate_subscription_alert_title),
                        getString(R.string.subscription_activate_subscription_alert_message),
                        getString(R.string.okay),
                        getString(R.string.subscribe),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        },
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SubscriptionStatusActivity_.intent(OverviewActivity.this).start();
                            }
                        },
                        true
                );
            }
        } else {
            startWarmup();
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mSessionConnection);
        unbindService(mDataSyncConnection);
        mDataSync = null;
        mSessionController = null;
        super.onDestroy();
        realm.close();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    void updateViews() {
        if (mWorkout == null) {
            graphView.setVisibility(GONE);
            workoutName.setText("Free Ride");
            overviewText.setText("Like the name suggests, this ride is a blank canvas. There are no preset intervals or lap markers in Free Ride mode, so make it up as you ride. Set laps as you go and you'll be able to do intervals in an unstructured way like riding outdoors. If you'd like more structure, try one of our pre-loaded workouts.");
            favoriteButton.setVisibility(GONE);
        } else {
            favoriteButton.setVisibility(View.VISIBLE);
            overviewText.setText(mWorkout.getOverview());
            String tags = "";
            RealmList<Tag> tagRO = mWorkout.getTags();

            for (int i = 0; i < tagRO.size(); i++) {
                tags += tagRO.get(i).getName();
                if (i < tagRO.size() - 1) {
                    tags += "  \u2022  ";
                }
            }
            if (inFreeCustom) {
                tags += " \u2022 Limited";
            }
            workoutCategory.setText(tags);
            workoutName.setText(mWorkout.getName());
            graphView.setVisibility(View.VISIBLE);
            graphView.drawEntireWorkoutPower(mWorkout);
            graphView.setGradient(R.attr.colorFitPrimary, R.attr.colorFitBg0);
            if(isCustomWorkout){
                workoutName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        workoutName.setVisibility(GONE);
                        nameEdit.setText(mWorkout.getName());
                        nameEdit.setVisibility(View.VISIBLE);
                        nameEdit.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
                        nameEdit.setInputType(InputType.TYPE_CLASS_TEXT);
                        nameEdit.requestFocus();
                    }
                });

            }
        }
        setVideoText();
        checkIfFavorited();
    }

    public void setVideoText() {
        if (!videoController.videoIsNull()) {
            if (videoController.getVideo().uri != null) {
                videoText.setText(videoController.getVideo().title);
            } else if (videoController.getVideo().youTubeId != null) {
                videoText.setText("YouTube playlist selected");
            } else {
                videoText.setText("Local Video Selected");
            }
            videoText.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        } else {
            videoText.setText("No Video Selected");
        }
    }

    public void checkIfFavorited() {
        if (isFavorite()) {
            favoriteButton.setImageResource(R.drawable.material_icon_star);
        } else {
            favoriteButton.setImageResource(R.drawable.material_icon_star_outline);
        }
    }

    public void setFavorite() {
        realm.beginTransaction();
        if (isFavorite()) {
            mProfile.removeFavorite(mWorkout);
            ViewStyling.getCustomToast(this, getLayoutInflater(), "Workout removed from Favorites").show();
        } else {
            mProfile.addFavorite(mWorkout);
            ViewStyling.getCustomToast(this, getLayoutInflater(), "Workout added to Favorites").show();
        }
        mProfile = realm.copyToRealmOrUpdate(mProfile);
        mDataSync.syncProfile();
        realm.commitTransaction();
        checkIfFavorited();
    }

    public boolean isFavorite() {
        RealmList<Workout> array = Profile.current().getFavoriteWorkouts();
        return array != null && mWorkout != null && array.contains(mWorkout);
    }

    void startWarmup() {
        mSessionController.setWorkout(mWorkout);
        WarmupActivity_.intent(this).start();
        finish();
    }
}
