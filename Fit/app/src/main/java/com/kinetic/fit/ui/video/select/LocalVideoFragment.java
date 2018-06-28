package com.kinetic.fit.ui.video.select;

import android.content.ContentResolver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 8/7/17.
 */

@EFragment(R.layout.fragment_video_select)
public class LocalVideoFragment extends Fragment {
    private static final int MAX_OFF_SCREEN = 0;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    LocalVideoAdapter mAdapter;

    @AfterViews
    void AfterFragViews() {
        ContentResolver cr = getContext().getContentResolver();
        mAdapter = new LocalVideoAdapter();
        mAdapter.discoverVideos(cr);
        mAdapter.setVideoSelectListener((VideoSelectActivity) getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }
}
