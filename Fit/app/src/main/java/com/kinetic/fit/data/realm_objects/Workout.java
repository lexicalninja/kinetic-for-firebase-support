package com.kinetic.fit.data.realm_objects;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.KineticAPI;
import com.kinetic.fit.data.objects.WorkoutInterval;
import com.kinetic.fit.util.RealmUtils;
import com.kinetic.fit.util.WorkoutParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/17/17.
 */

public class Workout extends RealmObject implements KineticAPI.KineticObject {

    public static final String CLASS_NAME = "Workout";


    private RealmList<Tag> tags;
    @PrimaryKey
    private String objectId;
    private String name;
    private String overview;
    private String creator;
    private double duration;
    private String uuid;
    private int powerAvg;
    private int powerMin;
    private int powerMax;
    private int ftpCalcProp;
    private int ftpCalcMod;
    private String intervalData;
    @Ignore
    private ArrayList<WorkoutInterval> intervalsCache;
    private double mIntensityFactor = 0.0;
    private double mTrainingStressScore = 0.0;

    public Workout(final JsonObject jsonObject, Realm realm) {
        tags = new RealmList<>();
        this.setObjectId(jsonObject.get("objectId").getAsString());
        if (jsonObject.get("name") != null) {
            this.setName(jsonObject.get("name").getAsString());
        }
        if (jsonObject.get("creator") != null) {
            this.setCreator(jsonObject.get("creator").getAsString());
        }
        if (jsonObject.get("overview") != null) {
            this.setOverview(jsonObject.get("overview").getAsString());
        }
        if (jsonObject.get("duration") != null) {
            this.setDuration(jsonObject.get("duration").getAsDouble());
        }
        if (jsonObject.get("uuid") != null) {
            this.setUuid(jsonObject.get("uuid").getAsString());
        }
        if (jsonObject.get("powerAvg") != null) {
            this.setPowerAvg(jsonObject.get("powerAvg").getAsInt());
        }
        if (jsonObject.get("powerMin") != null) {
            this.setPowerMin(jsonObject.get("powerMin").getAsInt());
        }
        if (jsonObject.get("powerMax") != null) {
            this.setPowerMax(jsonObject.get("powerMax").getAsInt());
        }
        if (jsonObject.get("ftpCalcProp") != null) {
            this.setFtpCalcProp(jsonObject.get("ftpCalcProp").getAsInt());
        }
        if (jsonObject.get("ftpcalcMod") != null) {
            this.setFtpCalcProp(jsonObject.get("ftpCalcProp").getAsInt());
        }
        if (jsonObject.get("intervalData") != null) {
            this.setIntervalData(jsonObject.getAsJsonArray("intervalData").toString());
            JSONArray cache = new JSONArray();
            try {
                cache = new JSONArray(intervalData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.intervalsCache = WorkoutInterval.jsonToWorkoutIntervals(cache);
        }

        if (jsonObject.get("intensityFactor") != null) {
            this.setIntensityFactor(jsonObject.get("intensityFactor").getAsDouble());
        }
        if (jsonObject.get("trainingStressScore") != null) {
            this.setTrainingStressScore(jsonObject.get("trainingStressScore").getAsDouble());
        }
        if (jsonObject.get("tags") != null) {

            for (JsonElement tag : jsonObject.getAsJsonArray("tags")) {
                String tagName = tag.getAsString();
                Tag tagRo = realm.where(Tag.class).equalTo("name", tagName).findFirst();
                if (tagRo == null) {
                    tagRo = new Tag(tagName);
                }
                tagRo = realm.copyToRealmOrUpdate(tagRo);
                tagRo.addWorkout(Workout.this);
                tags.add(tagRo);
            }
        }
    }

    @Override
    public String getClassName() {
        return "Workout";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Workout getRealmFromJson(JsonObject jsonObject) {
        Workout ro = new Workout();
        ro.setObjectId(jsonObject.get("objectId").toString());
        ro.setName(jsonObject.get("name").toString());
        ro.setCreator(jsonObject.get("creator").toString());
        ro.setOverview(jsonObject.get("overview").toString());
        ro.setDuration(jsonObject.get("duration").getAsDouble());
        ro.setUuid(jsonObject.get("uuid").toString());
        ro.setPowerAvg(jsonObject.get("powerAvg").getAsInt());
        ro.setPowerMin(jsonObject.get("powerMin").getAsInt());
        ro.setPowerMax(jsonObject.get("powerMax").getAsInt());
        ro.setFtpCalcProp(jsonObject.get("ftpCalcProp").getAsInt());
        ro.setFtpCalcMod(jsonObject.get("ftpcalcMod").getAsInt());
        ro.setIntervalData(jsonObject.get("intervalData").toString());
//        ro.setTagString(jsonObject.get("tags").toString());
        JSONArray cache = new JSONArray();
        try {
            cache = new JSONArray(intervalData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ro.intervalsCache = WorkoutInterval.jsonToWorkoutIntervals(cache);
        ro.setIntensityFactor(jsonObject.get("intensityFactor").getAsInt());
        ro.setTrainingStressScore(jsonObject.get("trainingStressScore").getAsInt());
        return ro;
    }

    public Workout() {
        super();
    }

    public Workout(WorkoutParser.WorkoutDefinition definition){
        super();
        this.name = definition.title != null ? definition.title : "Imported Workout";
        this.creator = definition.author != null ? definition.author : "Unknown";
        this.overview = definition.description != null ? definition.description : "";
        this.uuid = UUID.randomUUID().toString();
        intervalsCache = new ArrayList<>();
        for(WorkoutParser.WorkoutDefinition.Interval i : definition.intervals){
            intervalsCache.add(new WorkoutInterval(i));
        }
        intervalData = intervalsCache.toString();
        this.tags = new RealmList<>();
        this.tags.add(new Tag("Custom"));
        calculateTotals();
    }

    public double getIntensityFactor() {
        return mIntensityFactor;
    }

    public void setIntensityFactor(double mIntensityFactor) {
        this.mIntensityFactor = mIntensityFactor;
    }

    public double getTrainingStressScore() {
        return mTrainingStressScore;
    }

    public void setTrainingStressScore(double mTrainingStressScore) {
        this.mTrainingStressScore = mTrainingStressScore;
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

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getPowerAvg() {
        return powerAvg;
    }

    public void setPowerAvg(int powerAvg) {
        this.powerAvg = powerAvg;
    }

    public int getPowerMin() {
        return powerMin;
    }

    public void setPowerMin(int powerMin) {
        this.powerMin = powerMin;
    }

    public int getPowerMax() {
        return powerMax;
    }

    public void setPowerMax(int powerMax) {
        this.powerMax = powerMax;
    }

    public int getftpCalcProp() {
        return ftpCalcProp;
    }

    public void setFtpCalcProp(int ftpCalcProp) {
        this.ftpCalcProp = ftpCalcProp;
    }

    public int getftpCalcMod() {
        return ftpCalcMod;
    }

    public void setFtpCalcMod(int ftpCalcMod) {
        this.ftpCalcMod = ftpCalcMod;
    }

//    public JSONArray getIntervalData() {
//        JSONArray data = new JSONArray();
//        try {
//            data = new JSONArray(intervalData);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return data;
//    }

    public void setIntervalData(JSONArray intervalData) {
        this.intervalData = intervalData.toString();
    }

    public void setIntervalData(String intervalData) {
        this.intervalData = intervalData;
    }

    public void setIntervalsCache(ArrayList<WorkoutInterval> intervalsCache) {
        this.intervalsCache = intervalsCache;
    }

    public RealmList<Tag> getTags() {
        return tags;
    }

    public ArrayList<WorkoutInterval> getIntervals() {
        if (intervalsCache != null) {
            return intervalsCache;
        }
        JSONArray cache = new JSONArray();
        try {
            cache = new JSONArray(intervalData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intervalsCache = WorkoutInterval.jsonToWorkoutIntervals(cache);
        return intervalsCache;
    }

    public JsonElement getSerializedTags(){
        ArrayList<String> temp = new ArrayList<>();
        for(Tag t : getTags()){
            temp.add(t.getName());
        }
        return new Gson().toJsonTree(temp);
    }

    public JsonElement getSerializedIntervals(){
        ArrayList<String> temp = new ArrayList<>();
        for(WorkoutInterval i : getIntervals()){
            temp.add(i.toString());
        }
        return new Gson().toJsonTree(temp);
    }

    public boolean isFTPTest() {
        return getftpCalcProp() > 0;
    }

    public void calculateTotals() {
        double duration = 0;
        for (WorkoutInterval interval : getIntervals()) {
            duration += interval.duration;
        }
        setDuration(duration);
        mIntensityFactor = 0;
        mTrainingStressScore = 0;
        if (duration > 0) {
            for (WorkoutInterval interval : getIntervals()) {
                double avgPower = interval.averagePower(null);
                double weight = interval.duration / duration;
                mIntensityFactor += (avgPower * weight) / 100.0;
                mTrainingStressScore += avgPower * (interval.duration / 3600.0);
            }
        }
    }

    public double getKilojoules(Profile profile) {
        double kjs = 0;
        if (getDuration() > 0) {
            for (WorkoutInterval interval : getIntervals()) {
                kjs += interval.getKilojoules(profile);
            }
        }
        return kjs;
    }

    private JsonObject getACLs() {
        JsonObject j = new JsonObject();
        j.addProperty("read", true);
        j.addProperty("write", true);
        JsonObject acl = new JsonObject();
        acl.add(Profile.profileId(), j);
        return acl;

    }

    public JsonObject serializeToJson(){
        JsonObject j = new JsonObject();
        j.addProperty("intensityFactor", getIntensityFactor());
        j.addProperty("trainingStressScore", getTrainingStressScore());
        j.add("ACL", getACLs());
        j.addProperty("ftpCalcMod", getftpCalcMod());
        j.addProperty("name", getName());
        j.addProperty("creator", getCreator());
        j.add("intervalData", getSerializedIntervals());
        j.addProperty("duration", getDuration());
        j.add("tags", getSerializedTags());
        j.addProperty("overview", getOverview());
        j.addProperty("ftpCalcProp", getftpCalcProp());
        j.addProperty("uuid", getUuid());
        return j;
    }

}
