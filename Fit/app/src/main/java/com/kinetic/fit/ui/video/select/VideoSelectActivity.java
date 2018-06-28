package com.kinetic.fit.ui.video.select;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.third_party_clients.DropboxClient_;
import com.kinetic.fit.data.realm_objects.Subscription;
import com.kinetic.fit.data.realm_objects.Video;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.video.VideoController;
import com.kinetic.fit.ui.video.VideoControllerItem;
import com.kinetic.fit.ui.widget.FitButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import javax.annotation.Nonnull;

import io.realm.Realm;
import io.realm.RealmResults;

@EActivity(R.layout.activity_video_select)
public class VideoSelectActivity extends FitActivity implements VideoRecyclerAdapter.VideoSelectListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 14;
    private int mPages = 2;

    @ViewById
    ViewPager videoPager;
    @ViewById(R.id.button_left)
    FitButton leftButton;
    @ViewById(R.id.button_right)
    FitButton noVideoButton;
    @ViewById(R.id.button_middle)
    FitButton middleButton;

    VideosPagerAdapter mVideosPagerAdapter = new VideosPagerAdapter(getSupportFragmentManager());

    @Bean
    VideoController videoController;

    Realm realm;
    RealmResults<Subscription> subscriptions;
    boolean hasSufferfest = false;
    boolean hasLocalVideo = false;
    boolean hasSmartSub = false;
    boolean hasDropBoxVideo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkDeviceStoragePermission();
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        subscriptions = realm.where(Subscription.class).findAll();
        for (Subscription s : subscriptions) {
            if (!s.isCancelled()) {
                hasSmartSub = true;
                hasDropBoxVideo = DropboxClient_.getInstance_(this).isConnected();
                if (hasDropBoxVideo) {
                    mPages++;
                }
                break;
            }
        }
        if (realm.where(Video.class).findAll().size() > 0) {
            mPages++;
            hasSufferfest = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Videos");
    }

    @Click(R.id.button_right)
    void noVideoHandler() {
        videoController.setVideo(null);
        finish();
    }


    @AfterViews
    protected void afterViews() {
        videoPager.setAdapter(mVideosPagerAdapter);
        leftButton.setVisibility(View.GONE);
        middleButton.setVisibility(View.GONE);
        noVideoButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
        noVideoButton.setText(getString(R.string.no_video));
    }

    private class VideosPagerAdapter extends FragmentPagerAdapter {

        VideosPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0) {
                return KineticYouTubePlaylistFragmentFragment_.builder().build();
//            }
            } else if (position == 1) {
                return PersonalYouTubePlaylistFragmentFragment_.builder().build();
            } else if (position == 2) {
                if (hasSmartSub) {
                    if (hasLocalVideo) {
                        return LocalVideoFragment_.builder().build();
                    } else if (hasDropBoxVideo) {
                        return DropBoxVideoFragment_.builder().build();
                    }
                } else {
                    return StreamingVideoFragment_.builder().build();
                }
            } else if (position == 3) {
                if (hasSmartSub && hasDropBoxVideo) {
                    return DropBoxVideoFragment_.builder().build();
                } else {
                    return StreamingVideoFragment_.builder().build();
                }
            } else if (position == 4) {
                return StreamingVideoFragment_.builder().build();

            }
            return null;
        }

        @Override
        public int getCount() {
            return mPages;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Kinetic Playlists";
            } else if (position == 1) {
                return "Your Playlists";
            } else if (position == 2) {
                if (hasSmartSub) {
                    if (hasLocalVideo) {
                        return "Local Videos";
                    } else if (hasDropBoxVideo) {
                        return "DropBox Videos";
                    }
                } else {
                    return "Sufferfest Videos";
                }
            } else if (position == 3) {
                if (hasSmartSub && hasDropBoxVideo) {
                    return "DropBox Videos";
                } else {
                    return "Sufferfest Videos";
                }
            } else if (position == 4) {
                return "Sufferfest Videos";
            }
            return "";
        }
    }

    private void checkDeviceStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            mPages++;
            hasLocalVideo = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @Nonnull String permissions[], @Nonnull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    hasLocalVideo = true;
                    mPages++;
                    videoPager.getAdapter().notifyDataSetChanged();
                    // permission was granted, yay! Do the
                    // storage-related task you need to do.
                } else {
                    hasLocalVideo = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }

    @Override
    public void youTubeVideoSelected(YouTubeVideo video) {
        VideoControllerItem vcv = new VideoControllerItem();
        vcv.youTubeId = video.getYoutubeId();
        vcv.hidePopups = video.getHidePopups();
        vcv.title = video.getTitle();
        vcv.workoutSync = video.isWorkoutSynced();
        videoController.setVideo(vcv);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void streamingVideoSelected(Video video) {
        VideoControllerItem vcv = new VideoControllerItem();
        vcv.uri = Uri.parse(video.getVideoUrl());
        vcv.hidePopups = video.hidePopups();
        vcv.title = video.getName();
        vcv.workoutSync = video.isWorkoutSynced();
        videoController.setVideo(vcv);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void localVideoSelected(VideoControllerItem vcv) {
        videoController.setVideo(vcv);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void dropboxVideoSelected(VideoControllerItem vcv) {
        videoController.setVideo(vcv);
        setResult(Activity.RESULT_OK);
        finish();
    }
}
