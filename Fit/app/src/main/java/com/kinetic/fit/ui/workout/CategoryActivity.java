package com.kinetic.fit.ui.workout;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Category;
import com.kinetic.fit.ui.FitActivity;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Saxton on 12/14/16.
 */

@EActivity(R.layout.activity_category)
public class CategoryActivity extends FitActivity {

    private static final String TAG = "WorkoutCategoryActivity";
    private static final int MAX_OFF_SCREEN = 0;
    ProgressDialog mDialog;

    @ViewById(R.id.recyclerview_workout_category)
    RecyclerView recyclerView;

    Realm realm;

    private RealmResults<Category> mCategories;
    private WorkoutCategoryRecyclerAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("Workout Categories");
        refreshCategoryListData();
    }

    @UiThread
    void refreshCategoryListData() {
        final ProgressDialog dialog;
        mCategories = realm.where(Category.class).sort("order").findAll();
        if (recyclerView != null) {
            instantiateAdapter();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public void instantiateAdapter() {
        mAdapter = new WorkoutCategoryRecyclerAdapter(mCategories);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }
}
