package com.kinetic.fit.ui.trainingplans;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.TrainingPlanProgress;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.subscriptions.SubscriptionStatusActivity_;
import com.kinetic.fit.ui.widget.FitSearchBar;
import com.kinetic.fit.ui.widget.SubscriptionCalloutWidget;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Saxton on 10/20/16.
 */

@EActivity(R.layout.activity_training_plans)
public class TrainingPlanActivity extends FitActivity implements FitSearchBar.FitSearchListener {
    private static final String TAG = "TPActivity";
    private static final int MAX_OFF_SCREEN = 10;
    private static final String DIFFICULTY_ORDINAL_KEY = "Difficulty Ordinal";
    private static final String VOLUME_ORDINAL_KEY = "Volume Ordinal";
    Realm realm;
    private boolean hasSubscription = false;


    public enum PlanDifficultyLevel {
        BEGINNER("Beginner", R.mipmap.icon_xp_beginner),
        INTERMEDIATE("Intermediate", R.mipmap.icon_xp_intermediate),
        ADVANCED("Advanced", R.mipmap.icon_xp_advanced);

        private final String mLevel;
        private final int imageResourceId;

        PlanDifficultyLevel(String mLevel, int id) {
            this.mLevel = mLevel;
            this.imageResourceId = id;
        }

        public String getLevel() {
            return mLevel;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }
    }

    public enum PlanTrainingVolume {
        ALL("All", R.mipmap.icon_vol_high),
        LOW("Low", R.mipmap.icon_vol_low),
        MEDIUM("Medium", R.mipmap.icon_vol_medium),
        HIGH("High", R.mipmap.icon_vol_high);

        private final String mVolume;
        private final int imageResourceId;

        PlanTrainingVolume(String volume, int id) {
            mVolume = volume;
            imageResourceId = id;
        }

        public String getVolume() {
            return mVolume;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }
    }


    @ViewById(R.id.training_plans_selection_difficulty_icon)
    ImageView planDifficultyIcon;

    @ViewById(R.id.training_plans_selection_difficulty_text)
    TextView planDifficultyText;

    @ViewById(R.id.training_plans_selection_volume_icon)
    ImageView planVolumeIcon;

    @ViewById(R.id.training_plans_selection_volume_text)
    TextView planVolumeText;

    @ViewById(R.id.search_bar)
    FitSearchBar searchBar;

    @ViewById(R.id.subscription_callout)
    SubscriptionCalloutWidget subscriptionCallout;

    private int mPlanDifficultyOrdinal;
    private int mPlanVolumeOrdinal;

    RecyclerView recyclerView;

    TrainingPlanRecyclerAdapter recyclerAdapter;

    RealmResults<TrainingPlan> mTrainingPlans;
    List<TrainingPlan> mFilteredTrainingPlans;
    List<TrainingPlan> mSuggestedPlans = new ArrayList<>();
    RealmResults<Subscription> subscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        fetchSuggestedPlans();
        mPlanDifficultyOrdinal = 0;
        mPlanVolumeOrdinal = 0;
        subscriptions = realm.where(Subscription.class).findAll();
        for(Subscription s : subscriptions){
            if(!s.isCancelled()){
                hasSubscription = true;
                break;
            }
        }
    }


    @AfterViews
    public void afterView() {
        recyclerView = (RecyclerView) findViewById(R.id.training_plans_recycler_view);
        planDifficultyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlanDifficultyOrdinal = ++mPlanDifficultyOrdinal % PlanDifficultyLevel.values().length;
                setPlanDifficultyUIAssets();
                refreshTrainingPlans();
                searchBar.clear();
                planDifficultyIcon.requestFocus();
            }
        });

        planVolumeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlanVolumeOrdinal = ++mPlanVolumeOrdinal % PlanTrainingVolume.values().length;
                setPlanVolumeUIAssets();
                refreshTrainingPlans();
                searchBar.clear();
                planVolumeText.requestFocus();
            }
        });
        searchBar.setListener(this);
        if (hasSubscription) {
            subscriptionCallout.setVisibility(View.GONE);
        } else {
            subscriptionCallout.setCalloutText(getString(R.string.training_plan_activity_subscription_callout_text));
        }
        refreshTrainingPlans();

    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportActionBar().setTitle("Training Plans");
        setPlanVolumeUIAssets();
        setPlanDifficultyUIAssets();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        searchBar.setListener(null);
    }

    @Click(R.id.subscription_callout)
    void subscribe(){
        SubscriptionStatusActivity_.intent(this).start();
    }

    public void refreshTrainingPlans() {
        mTrainingPlans = getTrainingPlanQuery().sort("order").findAll();
        addHeadersToPlanList();
        recyclerAdapter.setTrainingPlans(mFilteredTrainingPlans);
        searchBar.setData(mFilteredTrainingPlans);
        recyclerAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(0);
    }

    public void instantiateAdapter() {
        recyclerAdapter = new TrainingPlanRecyclerAdapter(mFilteredTrainingPlans);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        recyclerAdapter.notifyDataSetChanged();
    }

    public void addHeadersToPlanList() {
        if (recyclerAdapter == null) {
            instantiateAdapter();
        }
        String currentCategory;
        List<TrainingPlan> temp = new ArrayList<>();
        TrainingPlan tempPlan = new TrainingPlan();
        tempPlan.setIsHeader(true);
        int tempPosition = 0;

        //Insert Recommended header
        currentCategory = "SUGGESTED";
        tempPlan.setCategoryName(currentCategory);
        temp.add(tempPosition++, tempPlan);
        for (TrainingPlan tp : mSuggestedPlans) {
            temp.add(tempPosition++, tp);
        }

        //TODO find suggested plan here


        //add Fitness Category header and then iterate mTrainingPlans to get all Fitness Plans
        currentCategory = TrainingPlan.FITNESS_CATEGORY;
        tempPlan = new TrainingPlan();
        tempPlan.setIsHeader(true);
        tempPlan.setCategoryName(currentCategory);
        temp.add(tempPosition++, tempPlan);

        for (TrainingPlan tp : mTrainingPlans) {
            if (tp.getCategory().equals(currentCategory)) {
                temp.add(tempPosition++, tp);
            }
        }

        //add Road Category header and then iterate mTrainingPlans to get all Fitness Plans
        currentCategory = TrainingPlan.ROAD_CATEGORY;
        tempPlan = new TrainingPlan();
        tempPlan.setIsHeader(true);
        tempPlan.setCategoryName(currentCategory);
        temp.add(tempPosition++, tempPlan);

        for (TrainingPlan tp : mTrainingPlans) {
            if (tp.getCategory().equals(currentCategory)) {
                temp.add(tempPosition++, tp);
            }
        }

        //add Offroad Category header and then iterate mTrainingPlans to get all Fitness Plans
        currentCategory = TrainingPlan.OFFROAD_CATEGORY;
        tempPlan = new TrainingPlan();
        tempPlan.setIsHeader(true);
        tempPlan.setCategoryName(currentCategory);
        temp.add(tempPosition++, tempPlan);

        for (TrainingPlan tp : mTrainingPlans) {
            if (tp.getCategory().equals(currentCategory)) {
                temp.add(tempPosition++, tp);
            }
        }

        //add Triathlon Category header and then iterate mTrainingPlans to get all Fitness Plans
        currentCategory = TrainingPlan.TRIATHLON_CATEGORY;
        tempPlan = new TrainingPlan();
        tempPlan.setIsHeader(true);
        tempPlan.setCategoryName(currentCategory);
        temp.add(tempPosition++, tempPlan);

        for (TrainingPlan tp : mTrainingPlans) {
            if (tp.getCategory().equals(currentCategory)) {
                temp.add(tempPosition++, tp);
            }
        }
        mFilteredTrainingPlans = temp;
//        Log.d(TAG, "Add headers end");
    }

    private RealmQuery<TrainingPlan> getTrainingPlanQuery() {
        if (!PlanTrainingVolume.values()[mPlanVolumeOrdinal].getVolume().equals("All")) {
            return realm.where(TrainingPlan.class)
                    .equalTo("trainingVolume", PlanTrainingVolume.values()[mPlanVolumeOrdinal].getVolume())
                    .equalTo("experienceLevel", PlanDifficultyLevel.values()[mPlanDifficultyOrdinal].getLevel());
        } else {
            return realm.where(TrainingPlan.class)
                    .equalTo("experienceLevel", PlanDifficultyLevel.values()[mPlanDifficultyOrdinal].getLevel());
        }
    }

    private void setPlanVolumeUIAssets() {
        planVolumeText.setText(PlanTrainingVolume.values()[mPlanVolumeOrdinal].getVolume().toUpperCase());
        planVolumeIcon.setBackground(ContextCompat.getDrawable(TrainingPlanActivity.this, PlanTrainingVolume.values()[mPlanVolumeOrdinal].getImageResourceId()));
    }

    private void setPlanDifficultyUIAssets() {
        planDifficultyText.setText(PlanDifficultyLevel.values()[mPlanDifficultyOrdinal].getLevel().toUpperCase());
        planDifficultyIcon.setBackground(ContextCompat.getDrawable(TrainingPlanActivity.this, PlanDifficultyLevel.values()[mPlanDifficultyOrdinal].getImageResourceId()));
    }

    @Override
    public void cancelSearch() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void searchObjectsUpdated(List<? extends RealmObject> objects) {
        mFilteredTrainingPlans = (List<TrainingPlan>) objects;
        recyclerAdapter.setTrainingPlans(mFilteredTrainingPlans);
        recyclerAdapter.notifyDataSetChanged();
    }

    private void fetchSuggestedPlans() {
        mSuggestedPlans.clear();
        RealmResults<TrainingPlanProgress> progress = realm.where(TrainingPlanProgress.class).isNull("finishDate").sort("startDate").findAll();
        if (progress.size() != 0) {
            TrainingPlan current = progress.get(0).getTrainingPlan();
            if (current.getNextPlanId() != null) {
                TrainingPlan suggested = realm.where(TrainingPlan.class).equalTo("objectId", current.getNextPlanId()).findFirst();
                if(suggested != null) {
                    mSuggestedPlans.add(suggested);
                }
            } else {
                mSuggestedPlans.add(current);
            }
        } else {
            RealmResults<TrainingPlan> suggested = realm.where(TrainingPlan.class).equalTo("author", "Kinetic").sort("order").findAll();
            if(suggested != null) {
                mSuggestedPlans.add(suggested.first());
            }
        }

    }
}
