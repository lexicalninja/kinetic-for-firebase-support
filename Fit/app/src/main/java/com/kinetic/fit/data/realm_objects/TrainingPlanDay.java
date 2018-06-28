package com.kinetic.fit.data.realm_objects;

import com.google.gson.JsonObject;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 10/19/16.
 */

public class TrainingPlanDay extends RealmObject {
    public static final String CLASS_NAME = "TrainingPlanDay";

    @PrimaryKey
    private String objectId;
    private String name;
    private int day;
    private String instructions;
    private String postRide;
    private TrainingPlan trainingPlan;
    private Workout workout;

    public TrainingPlanDay() {
    }

    public TrainingPlanDay(JsonObject jsonObject, Realm realm) {
        this.setObjectId(jsonObject.get("objectId").getAsString());
        this.setName(jsonObject.get("name").getAsString());
        this.setInstructions(jsonObject.get("instructions").getAsString());
        this.setPostRide(jsonObject.get("postRide").getAsString());
        this.setDay(jsonObject.get("day").getAsInt());
        if (jsonObject.get("workout") != null) {
            this.setWorkout(jsonObject.getAsJsonObject("workout"), realm);
        }
        this.setTrainingPlan(jsonObject.getAsJsonObject("trainingPlan"), realm);
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getPostRide() {
        return postRide;
    }

    public void setPostRide(String postRide) {
        this.postRide = postRide;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public void setTrainingPlan(JsonObject jsonObject, Realm realm) {
        TrainingPlan tp = realm.where(TrainingPlan.class).equalTo("objectId", jsonObject.get("objectId").getAsString()).findFirst();
        if (realm.isInTransaction()) {
            if (tp != null) {
                tp.addTrainingPlanDay(this);
                this.trainingPlan = realm.copyToRealmOrUpdate(tp);
            }
        }
    }

    public Workout getWorkout() {
        return workout;
    }

    public void setWorkout(JsonObject jsonObject, Realm realm) {
        Workout w = realm.where(Workout.class).equalTo("objectId", jsonObject.get("objectId").getAsString()).findFirst();
        this.workout = w;
    }
}
