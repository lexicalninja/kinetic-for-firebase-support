package com.kinetic.fit.ui.video.select;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Video;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by Saxton on 8/7/17.
 * /
 */

@EFragment(R.layout.fragment_video_select)
public class StreamingVideoFragment extends Fragment {
    private static final int MAX_OFF_SCREEN = 0;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    StreamingVideoAdapter mAdapter;

    Realm realm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @AfterViews
    void AfterFragViews() {
        RealmResults<Video> results = realm.where(Video.class).findAll();
        RealmList<Video> vids = new RealmList<>();
        for (Video v : results) {
            vids.add(v);
        }
        mAdapter = new StreamingVideoAdapter(vids);
        mAdapter.setVideoSelectListener((VideoSelectActivity) getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
