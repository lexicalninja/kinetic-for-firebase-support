package com.kinetic.fit.ui.video.select;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;
import com.kinetic.fit.ui.video.VideoController;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Saxton on 4/20/16.
 * //
 */

@EFragment
public abstract class AbstractYouTubeFragment extends Fragment {
    public static final String TAG = "YTVideos";
    private static final int MAX_OFF_SCREEN = 0;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;
    YouTubeVideoAdapter mAdapter;

    @Bean
    VideoController videoController;

    Realm realm;
    RealmList<YouTubeVideo> mVideos = new RealmList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        getVideos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @AfterViews
    void AfterFragViews() {
        mAdapter = new YouTubeVideoAdapter(mVideos);
        mAdapter.setVideoSelectListener((VideoSelectActivity) getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemViewCacheSize(MAX_OFF_SCREEN);
        mAdapter.notifyDataSetChanged();
    }

    abstract void getVideos();
}
