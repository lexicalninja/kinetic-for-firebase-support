package com.kinetic.fit.ui.hud;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.realm_objects.Subscription;

import org.androidannotations.annotations.EFragment;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

@EFragment
public class HUDPagerFragment extends android.support.v4.app.Fragment implements SessionController.SessionControllerObserver {

    private final static String TAG = "HUDPagerFrag";

    private HUDPagerDataProvider dataProvider;
    private HUDPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private JSONArray hudArray;
    private boolean hasSubscription = false;
    Realm realm;
    RealmResults<Subscription> subscriptions;

    private SessionController_.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController_.SessionControllerBinder) service;
            mSessionController.registerObserver(HUDPagerFragment.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            dataProvider = (HUDPagerDataProvider) context;
            hudArray = dataProvider.getHudPagerData();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement HUDPagerDataProvider");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContext().bindService(SessionController_.intent(getContext()).get(), mSessionConnection, Context.BIND_AUTO_CREATE);
        setRetainInstance(false);
        realm = Realm.getDefaultInstance();
        subscriptions = realm.where(Subscription.class).findAll();
        for (Subscription s : subscriptions) {
            if (s.isValid()) {
                hasSubscription = true;
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewPager = new ViewPager(getActivity());
        if (hudArray != null) {
            ArrayList<JSONArray> huds = new ArrayList<JSONArray>();
            Log.d(TAG, "hudArray.length: " + hudArray.length());
            for (int i = 0; i < hudArray.length(); i++) {
                Log.d(TAG, "hud #" + i + ": " + hudArray.optJSONArray(i).toString());
                JSONArray hud = hudArray.optJSONArray(i);
                if (hud != null) {
                    huds.add(hud);
                }
            }
            Log.d(TAG, "huds.toArray: " + huds.toArray(new JSONArray[huds.size()]).toString());
            pagerAdapter = new HUDPagerAdapter(getChildFragmentManager(), huds.toArray(new JSONArray[huds.size()]));
            viewPager.setOffscreenPageLimit(10);
            viewPager.setAdapter(pagerAdapter);
        }
        return viewPager;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mSessionController != null) {
            getContext().unbindService(mSessionConnection);
            mSessionController.unregisterObserver(this);
        }
        realm.close();
    }

    protected void updateValues(double timeDelta) {
        if (mSessionController != null) {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                if(pagerAdapter.getItem(currentItem) instanceof HUDFragment) {
                    HUDFragment hudFrag = (HUDFragment) pagerAdapter.getItem(currentItem);
                    hudFrag.sessionTick(timeDelta);
                }
            }
        } else {
            // get data from sensors ... not all property views can use this.
        }
    }

    @Override
    public void sessionTick(double timeDelta) {
        if (mSessionController != null) {
            updateValues(timeDelta);
        }
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {

    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {

    }

    public interface HUDPagerDataProvider {
        JSONArray getHudPagerData();
    }

    private class HUDPagerAdapter extends FragmentPagerAdapter {
        JSONArray huds[];
        HashMap<Integer, Fragment> items = new HashMap<>();

        public HUDPagerAdapter(FragmentManager fm, JSONArray[] huds) {
            super(fm);
            this.huds = huds;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (hasSubscription) {
                if (items.containsKey(position)) {
                    return items.get(position);
                }
                items.put(position, HUDFragment.newInstance(huds[position], true));
                return items.get(position);
            } else {
                if(position < 2){
                    if (items.containsKey(position)) {
                        return items.get(position);
                    }
                    items.put(position, HUDFragment.newInstance(huds[position], true));
                    return items.get(position);
                } else {
                    items.put(position, SubscriptionHUD.newInstance());
                    return items.get(position);
                }
            }
        }

    @Override
    public int getCount() {
        return huds.length;
    }

}

    @Override
    public void onResume() {
        super.onResume();
    }

    public ViewPager getViewPager() {
        return viewPager;
    }
}
