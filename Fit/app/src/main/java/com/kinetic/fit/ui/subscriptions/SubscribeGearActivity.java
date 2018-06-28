package com.kinetic.fit.ui.subscriptions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.data.objects.SmartSubscription;
import com.kinetic.fit.data.realm_objects.SubscriptionAddOn;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Saxton on 4/26/17.
 */

@EActivity(R.layout.activity_subscribe_gear)
public class SubscribeGearActivity extends FitActivity implements GearSelectionRecyclerAdapter.GearSelectorToggleListener {
    private static final int NUMBER_OF_COLUMNS = 2;
    private static final String TAG = "SubGearActivity";
    @ViewById(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;
    @ViewById(R.id.button_right)
    FitButton rightButton;
    GearSelectionRecyclerAdapter mGearAdapter;
    Integer selectedItem;
    Realm realm;
    RealmResults<SubscriptionAddOn> addOns;


    View.OnClickListener checkoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckoutActivity_.intent(SubscribeGearActivity.this)
                    .extra("subscription", new SmartSubscription())
                    .extra("addon", addOns.get(selectedItem))
                    .start();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        addOns = realm.where(SubscriptionAddOn.class).findAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @AfterViews
    void afterViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mGearAdapter = new GearSelectionRecyclerAdapter();
        mGearAdapter.setAddOns(addOns);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_COLUMNS));
        mGearAdapter.registerListener(this);
        mRecyclerView.setAdapter(mGearAdapter);
        leftButton.setVisibility(View.INVISIBLE);
        middleButton.setVisibility(View.INVISIBLE);
        rightButton.setFitButtonStyle(FitButton.DISABLED);
        rightButton.setText(getString(R.string.checkout));
    }


    @Override
    public void toggleGearSelection(int position) {
        Log.d(TAG, "" + position);
        if (selectedItem == null) {
            rightButton.setOnClickListener(checkoutListener);
            rightButton.setFitButtonStyle(FitButton.DEFAULT);
            if(mRecyclerView.getChildAt(position) != null) {
                mRecyclerView.getChildAt(position).setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            }
            selectedItem = position;
        } else if (selectedItem != position) {
            if(mRecyclerView.getChildAt(selectedItem) != null) {
                mRecyclerView.getChildAt(selectedItem).setBackgroundColor(ViewStyling.getColor(R.attr.colorFitBg0, this));
            }
            if(mRecyclerView.getChildAt(position) != null) {
                mRecyclerView.getChildAt(position).setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            }
            selectedItem = position;
        } else {
            rightButton.setOnClickListener(null);
            rightButton.setFitButtonStyle(FitButton.DISABLED);
            if(mRecyclerView.getChildAt(position) != null) {
                mRecyclerView.getChildAt(position).setBackgroundColor(ViewStyling.getColor(R.attr.colorFitBg0, this));
            }
            selectedItem = null;
        }
    }

}
