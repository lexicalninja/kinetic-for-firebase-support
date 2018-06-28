package com.kinetic.fit.ui.displays;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kinetic.fit.R;

import org.androidannotations.annotations.EFragment;

/**
 * Created by Saxton on 5/22/17.
 */


@EFragment(R.layout.fragment_displays_legend)
public class LegendFragment extends Fragment {

    public static LegendFragment newInstance() {
        Bundle args = new Bundle();
        LegendFragment fragment = new LegendFragment();
        fragment.setArguments(args);
        fragment.setRetainInstance(false);
        return fragment;
    }

}
