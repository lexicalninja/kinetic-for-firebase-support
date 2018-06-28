package com.kinetic.fit.data.realm_objects;

import android.util.Log;

import com.google.gson.JsonObject;
import com.kinetic.fit.data.KineticAPI;

import java.net.URLEncoder;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/19/17.
 */

public class Video extends RealmObject implements KineticAPI.KineticObject {
    public static final String CLASS_NAME = "Video";

    @PrimaryKey
    private String objectId;
    private String name;
    private String author;
    private String videoUrl;
    private double duration;
    private boolean workoutSync;
    private boolean hidePopups;
    private String logoUrl;
    private String thumbUrl;

    public Video() {
    }

    public Video(JsonObject jsonObject) {
        this.setObjectId(jsonObject.get("objectId").getAsString());
        this.setName(jsonObject.get("name").getAsString());
        this.setAuthor(jsonObject.get("author").getAsString());
        this.setDuration(jsonObject.get("duration").getAsDouble());
        this.setWorkoutSynced(jsonObject.get("workoutSync").getAsBoolean());
        this.setHidePopups(jsonObject.get("hidePopups").getAsBoolean());
        if (jsonObject.getAsJsonObject("logo") != null) {
            this.setLogoUrl(jsonObject.getAsJsonObject("logo").get("url").getAsString());
        }
        if (jsonObject.getAsJsonObject("thumb") != null) {
            this.setThumbUrl(jsonObject.getAsJsonObject("thumb").get("url").getAsString());
        }
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setVideoUrl(String url) {
        this.videoUrl = url;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public boolean isWorkoutSynced() {
        return workoutSync;
    }

    public void setWorkoutSynced(boolean workoutSync) {
        this.workoutSync = workoutSync;
    }

    public boolean hidePopups() {
        return hidePopups;
    }

    public void setHidePopups(boolean hidePopups) {
        this.hidePopups = hidePopups;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @Override
    public String getClassName() {
        return "Video";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Video getRealmFromJson(JsonObject jsonObject) {
        Video ro = new Video();
        ro.setName(jsonObject.get("Name").toString());
        ro.setAuthor(jsonObject.get("Author").toString());
//        TODO get the rest of the stuff after lookin gat the response object
        return ro;
    }


}
