package com.kinetic.fit.ui.video.select;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 8/7/17.
 * /
 */

@EFragment(R.layout.fragment_video_select)
public class DropBoxVideoFragment extends Fragment {
    public static final String TAG = "DBVideoFrag";
    public static final int MAX_OFF_SCREEN = 0;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    DropboxClient mClient;
    DropboxAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = DropboxClient_.getInstance_(getContext());
    }

    @AfterViews
    void afterFragmentViews() {
        mAdapter = new DropboxAdapter(mClient.getVideos());
        mAdapter.setVideoSelectListener((VideoSelectActivity) getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }
}
