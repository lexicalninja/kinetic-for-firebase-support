package com.kinetic.fit.data.realm_objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.KineticAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/17/17.
 */

public class Category extends RealmObject implements KineticAPI.KineticObject {

    public static final String CLASS_NAME = "Category";

    @Override
    public String getClassName() {
        return "Category";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Category getRealmFromJson(JsonObject jsonObject) {
        Category ro = new Category();
        ro.setObjectId(jsonObject.get("objectId").getAsString());
        ro.setName(jsonObject.get("name").getAsString());
        ro.setShortDescription(jsonObject.get("shortDescription").getAsString());
        ro.setOrder(jsonObject.get("order").getAsInt());
        if (jsonObject.get("image") != null) {
            ro.setImageUrl(jsonObject.getAsJsonObject("image").get("url").getAsString());
        }
        for (JsonElement tag : jsonObject.getAsJsonArray("tags")) {
            String tagName = tag.getAsString();
        }
        return ro;
    }

    @PrimaryKey
    private String objectId;
    private String name;
    private String shortDescription;
    private int order;
    private String imageUrl;
    private RealmList<Tag> tags;
    private int mCountedWorkouts;

    public Category() {
        super();
    }

    public Category(final JsonObject jsonObject, Realm realm) {
        tags = new RealmList<>();
        this.setObjectId(jsonObject.get("objectId").getAsString());
        this.setName(jsonObject.get("name").getAsString());
        this.setShortDescription(jsonObject.get("shortDescription").getAsString());
        this.setOrder(jsonObject.get("order").getAsInt());
        if (jsonObject.get("image") != null) {
            this.setImageUrl(jsonObject.getAsJsonObject("image").get("url").getAsString());
        }
        for (JsonElement tag : jsonObject.getAsJsonArray("tags")) {
            String tagName = tag.getAsString();
            Tag tagRo = new Tag(tagName);
            tagRo = realm.copyToRealmOrUpdate(tagRo);
            tagRo.addCategory(Category.this);
            tags.add(tagRo);
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCountedWorkouts() {
        int workouts = 0;
        for (Tag t : tags) {
            workouts += t.getWorkouts().size();
        }
        return workouts;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public RealmList<Tag> getTags() {
        return tags;
    }

    public List<Workout> getWorkouts() {
        Set<Workout> workouts = new HashSet<>();
        for (Tag tag : tags) {
            for (Workout wrk : tag.getWorkouts()) {
                workouts.add(wrk);
            }
        }
        return new ArrayList<>(workouts);
    }
}
