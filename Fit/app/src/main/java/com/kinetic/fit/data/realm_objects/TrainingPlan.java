package com.kinetic.fit.data.realm_objects;


import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.kinetic.fit.R;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 10/19/16.
 */

public class TrainingPlan extends RealmObject {
    public static final String CLASS_NAME = "TrainingPlan";
    public static final String FITNESS_CATEGORY = "Fitness";
    public static final String ROAD_CATEGORY = "Road";
    public static final String OFFROAD_CATEGORY = "Offroad";
    public static final String TRIATHLON_CATEGORY = "Triathlon";

    public static final String PLAN_EXPERIENCE_BEGINNER = "Beginner";
    public static final String PLAN_EXPERIENCE_INTERMEDIATE = "Intermediate";
    public static final String PLAN_EXPERIENCE_ADVANCED = "Advanced";

    public static final String PLAN_VOLUME_LOW = "Low";
    public static final String PLAN_VOLUME_MEDIUM = "Medium";
    public static final String PLAN_VOLUME_HIGH = "High";


    private boolean mIsHeader = false;
    private String categoryName;

    @PrimaryKey
    private String objectId;
    private int totalDays;
    private String author;
    private String planName;
    private String trainingVolume;
    private String category;
    private String experienceLevel;
    private String targetRider;
    private String planGoals;
    private String planOverview;
    private RealmList<TrainingPlanDay> trainingPlanDays = new RealmList<>();
    private int order;
    private String nextPlanId;
    private int startDay;

    public enum PlanStartDay {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday
    }

    public TrainingPlan() {
    }

    public TrainingPlan(JsonObject jsonObject) {
        this.setObjectId(jsonObject.get("objectId").getAsString());
        this.setPlanName(jsonObject.get("name").getAsString());
        this.setTrainingVolume(jsonObject.get("trainingVolume").getAsString());
        this.setCategory(jsonObject.get("category").getAsString());
        this.setAuthor(jsonObject.get("author").getAsString());
        this.setExperienceLevel(jsonObject.get("experienceLevel").getAsString());
        this.setTargetRider(jsonObject.get("targetRider").getAsString());
        this.setPlanGoals(jsonObject.get("goals").getAsString());
        this.setPlanOverview(jsonObject.get("overview").getAsString());
        this.setOrder(jsonObject.get("order").getAsInt());
        if (jsonObject.get("nextPlan") != JsonNull.INSTANCE && jsonObject.getAsJsonObject("nextPlan") != null) {
            this.setNextPlanId(jsonObject.getAsJsonObject("nextPlan").get("objectId").getAsString());
        }
        this.setStartDay(jsonObject.get("startDay").getAsInt());
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getTrainingVolume() {
        return trainingVolume;
    }

    public void setTrainingVolume(String trainingVolume) {
        this.trainingVolume = trainingVolume;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getTargetRider() {
        return targetRider;
    }

    public void setTargetRider(String targetRider) {
        this.targetRider = targetRider;
    }

    public String getPlanGoals() {
        return planGoals;
    }

    public void setPlanGoals(String planGoals) {
        this.planGoals = planGoals;
    }

    public String getPlanOverview() {
        return planOverview;
    }

    public void setPlanOverview(String planOverview) {
        this.planOverview = planOverview;
    }

    public int getPlanLengthInWeeks() {
        int weeks = 0;
        weeks += totalDays / 7;
        if (totalDays % 7 > 0) {
            weeks++;
        }
        return weeks;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getNextPlanId() {
        return nextPlanId;
    }

    public void setNextPlanId(String nextPlanId) {
        this.nextPlanId = nextPlanId;
    }

    public boolean getIsHeader() {
        return mIsHeader;
    }

    public void setIsHeader(boolean isHeader) {
        mIsHeader = isHeader;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public RealmList<TrainingPlanDay> getTrainingPlanDays() {
        return trainingPlanDays;
    }

    public void addTrainingPlanDay(TrainingPlanDay day) {
        this.trainingPlanDays.add(day);
//        may need to offload this somewhere else to reduce overhead
        this.trainingPlanDays.sort("day");
        this.totalDays = trainingPlanDays.get(trainingPlanDays.size() - 1).getDay();
    }

    public void setTrainingPlanDays(RealmList<TrainingPlanDay> trainingPlanDays) {
        this.trainingPlanDays = trainingPlanDays;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getSortOrdinalForCategory(String category) {
        switch (category) {
            case FITNESS_CATEGORY: {
                return 0;
            }
            case ROAD_CATEGORY: {
                return 1;
            }
            case OFFROAD_CATEGORY: {
                return 2;
            }
            case TRIATHLON_CATEGORY: {
                return 3;
            }
            default: {
                return 0;
            }
        }
    }

    public int getImageResourceId() {
        if (categoryName.equals(FITNESS_CATEGORY)) {
            return R.mipmap.icon_type_fitness;
        } else if (categoryName.equals(ROAD_CATEGORY)) {
            return R.mipmap.icon_type_road;
        } else if (categoryName.equals(OFFROAD_CATEGORY)) {
            return R.mipmap.icon_type_offroad;
        } else if (categoryName.equals(TRIATHLON_CATEGORY)) { //return Triathlon icon id number here
            return R.mipmap.icon_type_triathlon;
        } else {
            return 0;
        }
    }

    public int getCategoryIconResourceId() {
        if (getCategory().equals(FITNESS_CATEGORY)) {
            return R.mipmap.icon_type_fitness;
        } else if (getCategory().equals(ROAD_CATEGORY)) {
            return R.mipmap.icon_type_road;
        } else if (getCategory().equals(OFFROAD_CATEGORY)) {
            return R.mipmap.icon_type_offroad;
        } else { //return Triathlon icon id number here
            return R.mipmap.icon_type_triathlon;
        }
    }

    public int getExperienceLevelIconId() {
        if (getExperienceLevel().equals(PLAN_EXPERIENCE_BEGINNER)) {
            return R.mipmap.icon_xp_beginner;
        } else if (getExperienceLevel().equals(PLAN_EXPERIENCE_INTERMEDIATE)) {
            return R.mipmap.icon_xp_intermediate;
        } else {
            return R.mipmap.icon_xp_advanced;
        }
    }

    public int getPlanVolumeIconId() {
        if (getExperienceLevel().equals(PLAN_VOLUME_LOW)) {
            return R.mipmap.icon_vol_low;
        } else if (getExperienceLevel().equals(PLAN_VOLUME_MEDIUM)) {
            return R.mipmap.icon_vol_medium;
        } else {
            return R.mipmap.icon_vol_high;
        }
    }
}
