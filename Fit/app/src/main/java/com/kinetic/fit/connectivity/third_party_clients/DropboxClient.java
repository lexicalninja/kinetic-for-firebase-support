package com.kinetic.fit.connectivity.third_party_clients;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.VideoMetadata;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.exporting.KINKineticClient;
import com.kinetic.fit.ui.video.VideoControllerItem;
import com.kinetic.fit.util.FitAnalytics;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Saxton on 5/16/17.
 * /
 */


@EBean(scope = EBean.Scope.Singleton)
public class DropboxClient {

    public static final String TAG = "DropboxClient";

    static final String DROPBOX_PREF = "dropbox_token";
    public static final String STATUS_CHANGED = "Drop.CONNECTION_CHANGED";
    private static final String PREF_AUTO_SHARE = "AutoShare";

    private String uuid;
    private ArrayList<VideoControllerItem> mVideos = new ArrayList<>();

    @RootContext
    Context mContext;

    private SharedPreferences sharedPreferences;

    private String getAccessToken() {
        return sharedPreferences.getString(DROPBOX_PREF + uuid, null);
    }

    private void deleteToken() {
        sharedPreferences.edit().remove(DROPBOX_PREF + uuid).apply();
    }

    public boolean getAutoShare() {
        return sharedPreferences.getBoolean(PREF_AUTO_SHARE + uuid, false);
    }

    public void setAutoShare(boolean autoShare) {
        sharedPreferences.edit().putBoolean(PREF_AUTO_SHARE + uuid, autoShare).apply();
    }

    public boolean isConnected() {
        return getAccessToken() != null;
    }

    public ArrayList<VideoControllerItem> getVideos() {
        return mVideos;
    }

    @AfterInject
    void postInject() {
        uuid = Profile.getUUID();
        sharedPreferences = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);

    }

    public void disconnect() {
        deleteToken();
        mContext.sendBroadcast(new Intent(STATUS_CHANGED));
    }

    private static DbxClientV2 getClient(String ACCESS_TOKEN) {
        // Create Dropbox client
        DbxRequestConfig config = new DbxRequestConfig("Kinetic.Fit");
        return new DbxClientV2(config, ACCESS_TOKEN);
    }

    @Background
    public void uploadSession(String sessionId, String uploadFileName) {
        try {
            Uri uri = KINKineticClient.encodeSession(mContext, sessionId, true);
            uploadFileName += ".fit";
            DbxClientV2 client = getClient(getAccessToken());
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            FileMetadata metadata = client.files()
                    .uploadBuilder("/" + uploadFileName)
                    .uploadAndFinish(is);
            FitAnalytics.sendShareKPI(TAG);
            Log.d(TAG, "metadata: " + metadata.toStringMultiline());
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Background
    public void discoverVideos() {
        mVideos.clear();
        DbxClientV2 client = getClient(getAccessToken());
        ListFolderResult result;
        try {
            result = client.files().listFolderBuilder("")
                    .withIncludeDeleted(false)
                    .withRecursive(true)
                    .withIncludeMediaInfo(true)
                    .start();
            while (true) {
                for (Metadata metadata : result.getEntries()) {
                    if (((FileMetadata) metadata).getMediaInfo() != null) {
                        if (((FileMetadata) metadata).getMediaInfo().getMetadataValue() instanceof VideoMetadata) {
                            Log.d(TAG, metadata.getPathLower());
                            createSharedLink(((FileMetadata) metadata), ((VideoMetadata) ((FileMetadata) metadata).getMediaInfo().getMetadataValue()).getDuration());
                        }
                    }
                }
                if (!result.getHasMore()) {
                    break;
                }
                result = client.files().listFolderContinue(result.getCursor());
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void createSharedLink(FileMetadata file, long duration) {
        DbxClientV2 clientV2 = getClient(getAccessToken());
        try {
            GetTemporaryLinkResult media = clientV2.files().getTemporaryLink(file.getPathLower());
            if (media != null && media.getLink() != null) {
                processSharedLink(media, duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Background
    public void processSharedLink(GetTemporaryLinkResult link, long duration) {
        VideoControllerItem video = new VideoControllerItem();
        video.dropboxUrl = link.getLink();
        video.title = link.getMetadata().getName().substring(0, link.getMetadata().getName().indexOf("."));
        video.duration = duration;
        video.workoutSync = false;
        video.hidePopups = false;
        mVideos.add(video);
    }


}
