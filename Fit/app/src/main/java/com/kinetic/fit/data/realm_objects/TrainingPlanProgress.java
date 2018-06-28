package com.kinetic.fit.data.realm_objects;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.kinetic.fit.util.RealmUtils;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 10/19/16.
 */

public class TrainingPlanProgress extends RealmObject{
    public static final String CLASS_NAME = "PlanProgress";

    @PrimaryKey
    private String uuid;
    String objectId;
    private TrainingPlan trainingPlan;
    private Date startDate;
    private Date finishDate;

    public TrainingPlanProgress(){
        this.uuid = UUID.randomUUID().toString();
    }

    public TrainingPlanProgress(JsonObject jsonObject, Realm realm){
        if(jsonObject.get("uuid") == null){
            this.uuid = UUID.randomUUID().toString();
        } else {
            this.uuid = jsonObject.get("uuid").getAsString();
        }
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        JsonObject date;
        this.setObjectId(jsonObject.get("objectId").getAsString());
        this.setTrainingPlan(jsonObject.getAsJsonObject("trainingPlan"), realm);
        date = jsonObject.getAsJsonObject("startDate");
        this.setStartDate(df.parseDateTime(date.get("iso").getAsString()).toDate());
        if(jsonObject.get("finishDate") != null) {
            date = jsonObject.getAsJsonObject("finishDate");
            this.setFinishDate(df.parseDateTime(date.get("iso").getAsString()).toDate());
        }
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public void setTrainingPlan(JsonObject jsonObject, Realm realm) {
        Crashlytics.log(jsonObject.toString());
        TrainingPlan tp = realm.where(TrainingPlan.class).equalTo("objectId", jsonObject.get("objectId").getAsString()).findFirst();
        this.trainingPlan = tp;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
    }

    public String getUuid() {
        return uuid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public JsonObject toJsonObject(){
        JsonObject j = new JsonObject();
        j.addProperty("uuid", uuid);
        j.add("startDate", RealmUtils.serializeToJson(startDate));
        if(finishDate != null){
            j.add("finishDate", RealmUtils.serializeToJson(finishDate));
        }
        j.add("trainingPlan", RealmUtils.serializeTraininglanPointerToJson(trainingPlan));
        j.add("ACL", RealmUtils.getACLs());
        j.add("user", RealmUtils.serializeProfilePointerToJson());
        return j;
    }
}
