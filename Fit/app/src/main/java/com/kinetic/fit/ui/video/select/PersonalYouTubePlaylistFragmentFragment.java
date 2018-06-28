package com.kinetic.fit.ui.video.select;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;

import org.androidannotations.annotations.EFragment;

import io.realm.RealmResults;

/**
 * Created by Saxton on 8/3/17.
 * /
 */

@EFragment(R.layout.fragment_video_select)
public class PersonalYouTubePlaylistFragmentFragment extends AbstractYouTubeFragment {
    @Override
    void getVideos() {
        RealmResults<YouTubeVideo> results = realm.where(YouTubeVideo.class).notEqualTo("author", "Kinetic Fit").findAll();
        for (YouTubeVideo v : results) {
            mVideos.add(v);
        }
    }
}
