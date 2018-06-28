package com.kinetic.fit.ui.video.select;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;

import org.androidannotations.annotations.EFragment;

import io.realm.RealmResults;

/**
 * Created by Patrick Saxton on 8/3/17.
 * woot
 */

@EFragment(R.layout.fragment_video_select)
public class KineticYouTubePlaylistFragmentFragment extends AbstractYouTubeFragment {
    @Override
    void getVideos() {
        RealmResults<YouTubeVideo> results = realm.where(YouTubeVideo.class).equalTo("author", "Kinetic Fit").findAll();
        for (YouTubeVideo v : results) {
            mVideos.add(v);
        }
    }
}
