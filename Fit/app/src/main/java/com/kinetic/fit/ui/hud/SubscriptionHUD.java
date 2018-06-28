package com.kinetic.fit.ui.hud;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinetic.fit.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by Saxton on 5/11/17.
 */

@EFragment
public class SubscriptionHUD extends Fragment {

    public static SubscriptionHUD newInstance() {
        SubscriptionHUD fragment = new SubscriptionHUD_();
        fragment.setRetainInstance(false);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subscription_hud_callout, null);
        return v;
    }
}
