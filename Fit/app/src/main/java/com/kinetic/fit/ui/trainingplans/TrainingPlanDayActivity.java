package com.kinetic.fit.ui.trainingplans;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanDay;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.ui.workout.OverviewActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;

import static android.view.View.GONE;

/**
 * Created by Saxton on 3/6/17.
 */

@EActivity(R.layout.activity_training_plan_day)
public class TrainingPlanDayActivity extends FitActivity {
    private static final String TAG = "PlanDayActivity";
    public static final String TP_EXTRA = "TP_EXTRA";
    public static final String TPDAY_EXTRA = "TPDAY_EXTRA";

    @ViewById(R.id.training_plan_day_plan_name)
    TextView planName;
    @ViewById(R.id.training_plan_day_creator_name)
    TextView creatorName;
    @ViewById(R.id.training_plan_day_duration)
    TextView planDuration;
    @ViewById(R.id.training_plan_day_volume_icon)
    ImageView volumeIcon;
    @ViewById(R.id.training_plan_day_difficulty_icon)
    ImageView difficultyIcon;
    @ViewById(R.id.training_plan_day_category_icon)
    ImageView categoryIcon;
    @ViewById(R.id.training_plan_day_day_overview_header)
    TextView planDayHeader;
    @ViewById(R.id.training_plan_day_overview_text_area)
    TextView dayOverviewText;
    @ViewById(R.id.training_plan_day_instruction_text_area)
    TextView instructionText;
    @ViewById(R.id.training_plan_day_workout_text_area)
    TextView workoutText;
    @ViewById(R.id.training_plan_post_ride_text_area)
    TextView postRideText;
    @ViewById(R.id.button_left)
    FitButton buttonLeft;
    @ViewById(R.id.button_middle)
    FitButton buttonMiddle;
    @ViewById(R.id.button_right)
    FitButton buttonRight;
    @ViewById(R.id.training_plan_day_post_ride_area)
    LinearLayout postWorkoutArea;


    Realm realm;
    TrainingPlan tp;
    TrainingPlanDay today;
    Workout todaysWorkout;

    @Extra(TP_EXTRA)
    String tpExtra;
    @Extra(TPDAY_EXTRA)
    String tpDayExtra;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        tp = realm.where(TrainingPlan.class).equalTo("objectId", tpExtra).findFirst();
        today = realm.where(TrainingPlanDay.class).equalTo("objectId", tpDayExtra).findFirst();
        if (today != null) {
            todaysWorkout = today.getWorkout();
        }
    }

    @AfterViews
    protected void afterViews() {
        planName.setText(tp.getPlanName());
        creatorName.setText(tp.getAuthor());
        planDuration.setText(getString(R.string.training_plan_duration_string_formatter, tp.getPlanLengthInWeeks()));
        volumeIcon.setImageResource(tp.getPlanVolumeIconId());
        categoryIcon.setImageResource(tp.getCategoryIconResourceId());
        difficultyIcon.setImageResource(tp.getExperienceLevelIconId());
        planDayHeader.setText(getString(R.string.training_plan_day_days_string_formatter, today.getDay()));
        dayOverviewText.setText(today.getName());
        instructionText.setText(today.getInstructions());
        postRideText.setText(today.getPostRide());
        buttonMiddle.setFitButtonStyle("basic");
        buttonMiddle.setText(getString(R.string.training_plan_day_view_plan_button_text));
        buttonMiddle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrainingPlanOverViewActivity_.intent(TrainingPlanDayActivity.this)
                        .extra("planId", tp.getObjectId())
                        .start();
            }
        });
        buttonLeft.setVisibility(View.INVISIBLE);
        if (today.getWorkout() == null) {
            buttonRight.setVisibility(GONE);
            workoutText.setText(getString(R.string.training_plan_day_no_workout_text));
            postWorkoutArea.setVisibility(GONE);

        } else {
            workoutText.setText(todaysWorkout.getName());
            buttonRight.setFitButtonStyle("default");
            buttonRight.setText(getString(R.string.ride));
            buttonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    OverviewActivity_.intent(TrainingPlanDayActivity.this)
                            .extra("workoutId", todaysWorkout.getObjectId())
                            .start();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
