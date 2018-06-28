package com.kinetic.fit.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Category;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.realm_objects.Tag;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.subscriptions.SubscriptionStatusActivity_;
import com.kinetic.fit.ui.widget.FitSearchBar;
import com.kinetic.fit.ui.widget.SubscriptionCalloutWidget;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Saxton on 12/15/16.
 */

@EActivity(R.layout.activity_selection)
public class SelectionActivity extends FitActivity implements WorkoutSelectionSortView.WorkoutSelectionSortViewListener, WorkoutSelectionRecyclerAdapter.SelectionListener, FitSearchBar.FitSearchListener {

    private static final String TAG = "WorkoutSelectActivity";
    private static final int MAX_OFF_SCREEN = 0;
    public static final String CATEGORY_NUM_INT_EXTRA = "categoryNum";
    public static final String FAVORITES_BOOLEAN_EXTRA = "favorites";
    public static final String CATEGORY_NAME = "CategoryName";

    @ViewById
    protected WorkoutSelectionSortView workoutSortView;

    @ViewById(R.id.recyclerview_workout_selection)
    RecyclerView recyclerView;

    @ViewById(R.id.search_bar)
    FitSearchBar searchBar;

    @ViewById(R.id.subscription_callout)
    SubscriptionCalloutWidget callout;

    private WorkoutSelectionRecyclerAdapter mAdapter;

    //    protected Category mCategory;
    int categoryNum;
    protected boolean mFavorites;
    private boolean hasSubscription = false;
    private String categoryName;
    private RealmList<Workout> mWorkouts;
    Realm realm;
    RealmResults<Subscription> subscriptions;
    Profile mProfile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        categoryNum = i.getIntExtra(CATEGORY_NUM_INT_EXTRA, -1);
        mFavorites = i.getBooleanExtra(FAVORITES_BOOLEAN_EXTRA, false);
        categoryName = i.getStringExtra(CATEGORY_NAME);
        realm = Realm.getDefaultInstance();
        mProfile = realm.where(Profile.class).findFirst();
        if (mFavorites) {
            mWorkouts = mProfile.getFavoriteWorkouts();
        } else {
            Category category = realm.where(Category.class).equalTo("name", categoryName).findFirst();
            RealmList<Tag> tags = category.getTags();
            if (tags.size() == 1) {
                mWorkouts = tags.get(0).getWorkouts();
            } else {
                mWorkouts = new RealmList<>();
                Set<Workout> workoutsSet = new HashSet<>();
                for (Tag tag : tags) {
                    for (Workout w : tag.getWorkouts()) {
                        workoutsSet.add(w);
                    }
                }
                for (Workout wrk : workoutsSet) {
                    mWorkouts.add(wrk);
                }
            }
            if (mWorkouts != null && mWorkouts.isManaged()) {
                mWorkouts.sort("duration", Sort.ASCENDING);
            }
        }
        subscriptions = realm.where(Subscription.class).findAll();
        for (Subscription s : subscriptions) {
            if (!s.isCancelled()) {
                hasSubscription = true;
                break;
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("Select Workout");
    }

    @AfterViews
    void afterViews() {
        workoutSortView.setListener(this);
        workoutSortView.getWorkoutsort().check(R.id.workout_duration_sort);
        workoutSortView.requestFocus();
        searchBar.setListener(this);
        if (hasSubscription) {
            callout.setVisibility(View.GONE);
        } else {
            callout.setCalloutText(getString(R.string.selection_activity_subscription_callout_text));
        }
        instantiateAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Click(R.id.subscription_callout)
    void subscribe() {
        SubscriptionStatusActivity_.intent(this).start();
    }

    @Override
    public void workoutSortViewSelected(
            final WorkoutSelectionSortView.SortGroups sortType) {
        realm.beginTransaction();
        Collections.sort(mAdapter.getCurrentWorkoutList(), new Comparator<Workout>() {
            @Override
            public int compare(Workout lhs, Workout rhs) {
                switch (sortType) {
                    case NAME: {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                    case DURATION: {
                        return (int) (lhs.getDuration() - rhs.getDuration());
                    }
                    case IF: {
                        if (lhs.getIntensityFactor() < rhs.getIntensityFactor()) {
                            return -1;
                        } else if (lhs.getIntensityFactor() > rhs.getIntensityFactor()) {
                            return 1;
                        }
                        return 0;
                    }
                    case TSS: {
                        if (lhs.getTrainingStressScore() < rhs.getTrainingStressScore()) {
                            return -1;
                        } else if (lhs.getTrainingStressScore() > rhs.getTrainingStressScore()) {
                            return 1;
                        }
                        return 0;

                    }
                    default: {
                        return 0;
                    }
                }
            }
        });
        realm.commitTransaction();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(0);
        } else {
            instantiateAdapter();
        }
    }

    private void instantiateAdapter() {
        mAdapter = new WorkoutSelectionRecyclerAdapter(mWorkouts);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
        searchBar.setData(mWorkouts);
    }

    @Override
    public void workoutSelected(Workout workout) {
        OverviewActivity_.intent(this)
                .extra("workoutId", workout.getObjectId())
                .start();
        searchBar.clear();
    }

    /**
     * this should be safe unchecked since we are only using List<T> of one type in the activity
     * and not using other generic types
     **/
    @SuppressWarnings("unchecked")
    @Override
    public void searchObjectsUpdated(List<? extends RealmObject> objects) {
        RealmList<Workout> workouts = new RealmList<>();
        for (RealmObject o : objects) {
            workouts.add((Workout) o);
        }
        mAdapter.updateWorkouts(workouts);
        mAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void cancelSearch() {
        //dont need this here. we dont want to hide this one
    }
}
