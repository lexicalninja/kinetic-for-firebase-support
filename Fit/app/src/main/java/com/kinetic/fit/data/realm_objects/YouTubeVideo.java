package com.kinetic.fit.data.realm_objects;

import android.util.Log;

import com.google.gson.JsonObject;

import java.net.URLEncoder;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/19/17.
 */

public class YouTubeVideo extends RealmObject {
    public static final String CLASS_NAME = "Video";

    @PrimaryKey
    private String objectId;
    private String title;
    private String author;
    private boolean workoutSync;
    private boolean hidePopups;
    private String thumbUrl;
    private String youtubeId;

    public YouTubeVideo() {
    }

    public YouTubeVideo(JsonObject jsonObject) {
        this.setObjectId(UUID.randomUUID().toString());
        this.setTitle(jsonObject.getAsJsonObject("snippet").getAsJsonObject("localized").get("title").getAsString());
        this.setAuthor(jsonObject.getAsJsonObject("snippet").get("channelTitle").getAsString());
        this.setWorkoutSynced(false);
        this.setHidePopups(true);
        this.setThumbUrl(jsonObject.getAsJsonObject("snippet").getAsJsonObject("thumbnails").getAsJsonObject("default").get("url").getAsString());
        this.setYoutubeId(jsonObject.get("id").getAsString());
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVideoUrl() {
        String sessionToken = Profile.current().getSessionToken();
        if (sessionToken != null) {
            try {
                return "https://app.kinetic.fit/app/videos/stream/" + getObjectId() + "?token=" + URLEncoder.encode(sessionToken, "utf-8");
            } catch (Exception e) {
                Log.e(CLASS_NAME, e.getLocalizedMessage());
            }
        }
        return null;
    }

    public boolean isWorkoutSynced() {
        return workoutSync;
    }

    public void setWorkoutSynced(boolean workoutSync) {
        this.workoutSync = workoutSync;
    }

    public boolean getHidePopups() {
        return hidePopups;
    }

    public void setHidePopups(boolean hidePopups) {
        this.hidePopups = hidePopups;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getYoutubeId() {
        return youtubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.youtubeId = youtubeId;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;


    }

}
