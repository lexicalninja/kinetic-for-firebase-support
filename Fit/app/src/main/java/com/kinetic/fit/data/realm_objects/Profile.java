package com.kinetic.fit.data.realm_objects;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.kinetic.fit.data.KineticAPI;
import com.kinetic.fit.util.RealmUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/17/17.
 * fgsdfgsdfg
 */

public class Profile extends RealmObject implements KineticAPI.KineticObject {

    public static final String DATA_REFRESHED = "Profile.DATA_REFRESHED";
    public static final String LOGGED_OUT = "Profile.LOGGED_OUT";
    public static final String CLASS_NAME = "_User";

    @PrimaryKey
    private String objectId;
    @Ignore
    static ArrayList<Double> PowerZoneDefaultCeilings = new ArrayList<>(Arrays.asList(0.0, 0.55, 0.75, 0.90, 1.05, 1.20, 1.50));
    @Ignore
    static ArrayList<Double> HeartZoneDefaultCeilings = new ArrayList<>(Arrays.asList(0.0, 0.60, 0.70, 0.77, 0.88));
    @Ignore
    private ArrayList<Integer> powerZonesCache;
    private String powerZonesCacheString;
    @Ignore
    private ArrayList<Integer> heartZonesCache;
    private String heartZonesCacheSTring;
    @Ignore
    private ArrayList<Double> pzpCache;
    private String pzpCacheString;
    private RealmList<Workout> favoriteWorkoutsCache;
    private String uuid;
    private String name;
    private String username;
    private String email;
    private Date birthdate;
    private double weightKG;
    private double heightCM;
    private int powerFTP;
    private int heartMax;
    private int heartResting;
    private double totalDistance;
    private double totalKiloJoules;
    private double totalTime;
    private String customHuds;
    private Date updatedLast;
    private String favsString;
    private String sessionToken;
    private TrainingPlan currentPlan;
    private String freeCustom;


    public Profile(JsonObject jsonObject) {
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        if (jsonObject.get("objectId") != null) {
            this.setObjectId(jsonObject.get("objectId").getAsString());
        }
        this.setUsername(jsonObject.get("username").getAsString());
        this.setSessionToken(jsonObject.get("sessionToken").getAsString());
        this.setName(jsonObject.get("name").getAsString());
        if (jsonObject.get("uuid") != null) {
            this.setUuid(jsonObject.get("uuid").getAsString());
        } else {
            this.setUuid(UUID.randomUUID().toString());
        }
        this.setEmail(jsonObject.get("email").getAsString());
        JsonObject bday = jsonObject.getAsJsonObject("birthdate");
        if (bday != null) {
            this.setBirthdate(df.parseDateTime(bday.get("iso").getAsString()).toDate());
        } else {
            this.setBirthdate(new DateTime(1980, 1, 1, 0, 0).toDate());
        }
        if (jsonObject.get("weightKG") != null) {
            this.setWeightKG(jsonObject.get("weightKG").getAsDouble());
        } else {
            this.setWeightKG(80);
        }
        if (jsonObject.get("heightCM") != null) {
            this.setHeightCM(jsonObject.get("heightCM").getAsDouble());
        } else {
            this.setHeightCM(175);
        }
        if (jsonObject.get("powerFTP") != null) {
            this.setPowerFTP(jsonObject.get("powerFTP").getAsInt());
        } else {
            this.setPowerFTP(150);
        }
        if (jsonObject.get("heartMax") != null) {
            this.setHeartMax(jsonObject.get("heartMax").getAsInt());
        } else {
            this.setHeartMax(200);
        }
        if (jsonObject.get("heartResting") != null) {
            this.setHeartResting(jsonObject.get("heartResting").getAsInt());
        } else {
            this.setHeartResting(60);
        }
        if (jsonObject.get("totalDistanceKM") != null) {
            this.setTotalDistanceKM(jsonObject.get("totalDistanceKM").getAsDouble());
        } else {
            this.setTotalDistanceKM(0.0);
        }
        if (jsonObject.get("totalKilojoules") != null) {
            this.setTotalKilojoules(jsonObject.get("totalKilojoules").getAsDouble());
        } else {
            this.setTotalKilojoules(0);
        }
        if (jsonObject.get("totalTime") != null) {
            this.setTotalTime(jsonObject.get("totalTime").getAsDouble());
        } else {
            this.setTotalTime(0.0);
        }
        if (jsonObject.get("customHUDs") != null) {
            JsonArray array = jsonObject.get("customHUDs").getAsJsonArray();
            JSONArray keep = new JSONArray();
            for (JsonElement el : array) {
                String temp = el.getAsString();
                try {
                    keep.put(new JSONObject(temp).getJSONArray("props"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            this.customHuds = keep.toString();
        }
        this.setUpdatedLast(df.parseDateTime(jsonObject.get("updatedAt").getAsString()).toDate());
        if (jsonObject.get("powerZones") != null) {
            this.setPowerZonesCacheString(jsonObject.get("powerZones").toString());
        } else {
            this.autoCalculatePowerZones();
        }
        if (jsonObject.get("heartZones") != null) {
            this.setHeartZonesCacheSTring(jsonObject.get("heartZones").toString());
        } else {
            this.autoCalculateHeartZones();
        }
        if (jsonObject.get("favoriteWorkouts") != null) {
            this.setFavsString(jsonObject.get("favoriteWorkouts").toString());
        }

        if (jsonObject.get("freeCustom") != null) {
            String free = "";
            for (JsonElement j : jsonObject.getAsJsonArray("freeCustom")) {
                free += j.getAsString() + "\\|";
            }
            free = free.substring(0, free.length() - 2);
            this.setFreeCustom(free);
        }
    }

    public Profile(JsonObject jsonObject, Realm realm) {
        this(jsonObject);
        if (jsonObject.getAsJsonObject("currentPlan") != null) {
            String planId = jsonObject.getAsJsonObject("currentPlan").get("objectId").getAsString();
            TrainingPlan tp = realm.where(TrainingPlan.class).equalTo("objectId", planId).findFirst();
            if (tp != null) {
                this.setCurrentPlan(tp);
            }
        }
    }

    @Override
    public String getClassName() {
        return "Profile";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Profile getRealmFromJson(JsonObject jsonObject) {
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        Profile ro = new Profile();
        ro.setObjectId(jsonObject.get("objectId").getAsString());
        ro.setName(jsonObject.get("name").getAsString());
        ro.setUuid(jsonObject.get("uuid").getAsString());
        ro.setEmail(jsonObject.get("email").getAsString());
        JsonObject bday = jsonObject.getAsJsonObject("birthdate");
        ro.setBirthdate(df.parseDateTime(bday.get("iso").getAsString()).toDate());
        ro.setWeightKG(jsonObject.get("weightKG").getAsDouble());
        ro.setHeightCM(jsonObject.get("heightCM").getAsDouble());
        ro.setPowerFTP(jsonObject.get("powerFTP").getAsInt());
        ro.setHeartMax(jsonObject.get("heartMax").getAsInt());
        ro.setHeartResting(jsonObject.get("heartResting").getAsInt());
        ro.setTotalDistanceKM(jsonObject.get("totalDistanceKM").getAsDouble());
        ro.setTotalKilojoules(jsonObject.get("totalKilojoules").getAsDouble());
        ro.setTotalTime(jsonObject.get("totalTime").getAsDouble());
        if (jsonObject.get("customHUDs") != null) {
            ro.setCustomHuds(jsonObject.get("customHUDs").getAsString());
        }
        ro.setUpdatedLast(df.parseDateTime(jsonObject.get("updatedAt").getAsString()).toDate());
        Type listType = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        ArrayList<Integer> numbers = new Gson().fromJson(jsonObject.get("powerZones"), listType);
        ro.setPowerZonesCache(numbers);
        numbers = new Gson().fromJson(jsonObject.get("heartZones"), listType);
        ro.setHeartZonesCache(numbers);
        Type listType2 = new TypeToken<ArrayList<Double>>() {
        }.getType();
        ArrayList<Double> numbers2 = new Gson().fromJson(jsonObject.get("powerZones"), listType2);
        ro.setPzpCache(numbers2);
        return ro;
    }


    public static Profile current() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        realm.close();
        return p;
    }

    public static String profileId() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        String p = current.get(0).getObjectId();
        realm.close();
        return p;
    }

    public static String getUUID() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        String uuid = p.getUuid();
        realm.close();
        return uuid;
    }

    public static String getMainEmail() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        String email = p.getEmail();
        realm.close();
        return email;
    }

    public static String getCurrentSession() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        String token = p.getSessionToken();
        realm.close();
        return token;
    }

    public static String getCurrentUsername() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        String username = p.getUsername();
        realm.close();
        return username;
    }

    public static String getCurrentName() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        String name = p.getName();
        realm.close();
        return name;
    }

    public Profile() {
        super();
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public TrainingPlan getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(TrainingPlan currentPlan) {
        this.currentPlan = currentPlan;
    }

    public ArrayList<Integer> getPowerZonesCache() {
        return RealmUtils.getStringAsIntegerArray(getPowerZonesCacheString());
    }

    private void prepPowerZoneCache() {
        powerZonesCache = new ArrayList<>();
        RealmUtils.deSerializeIntegerArrayString(getPowerZonesCacheString(), powerZonesCache);
    }

    public void setPowerZonesCache(ArrayList<Integer> powerZonesCache) {
        this.powerZonesCache = powerZonesCache;
        this.powerZonesCacheString = powerZonesCache.toString();
    }

    public String getPowerZonesCacheString() {
        return powerZonesCacheString;
    }

    public void setPowerZonesCacheString(String powerZonesCacheString) {
        this.powerZonesCacheString = powerZonesCacheString;

    }

    public ArrayList<Integer> getHeartZonesCache() {
        return RealmUtils.getStringAsIntegerArray(getHeartZonesCacheSTring());
    }

    public void setHeartZonesCache(ArrayList<Integer> heartZonesCache) {
        this.heartZonesCache = heartZonesCache;
        this.heartZonesCacheSTring = heartZonesCache.toString();
    }

    public String getHeartZonesCacheSTring() {
        return heartZonesCacheSTring;
    }

    public void setHeartZonesCacheSTring(String heartZonesCacheSTring) {
        this.heartZonesCacheSTring = heartZonesCacheSTring;
    }

    public List<Double> getPzpCache() {
        return pzpCache;
    }

    public void setPzpCache(ArrayList<Double> pzpCache) {
        this.pzpCache = pzpCache;
        this.powerZonesCacheString = pzpCache.toString();
    }

    public String getPzpCacheString() {
        return pzpCacheString;
    }

    public void setPzpCacheString(String pzpCacheString) {
        this.pzpCacheString = pzpCacheString;
    }

    public void autoCalculatePowerZones() {
        float ftp = getPowerFTP();
        ArrayList<Integer> cache = new ArrayList<>();
//        prepPowerZoneCache();
        for (double ceiling : PowerZoneDefaultCeilings) {
            cache.add((int) Math.floor(ftp * ceiling));
        }
        setPowerZonesCache(cache);
    }

    public ArrayList<Double> powerZoneCeilingsP() {
        if (getPowerFTP() > 0) {
            ArrayList<Double> cache = new ArrayList<>();
            double ftp = this.getPowerFTP();
            ArrayList<Integer> powerZones = this.getPowerZonesCache();
            for (int i = 0; i < powerZones.size(); i++) {
                cache.add((double) powerZones.get(i) / ftp);
            }
            return cache;
        } else {
            return PowerZoneDefaultCeilings;
        }
    }

    public void autoCalculateHeartZones() {
        float hr = getHeartMax();
        ArrayList<Integer> cache = new ArrayList<>();
        for (double ceiling : HeartZoneDefaultCeilings) {
            cache.add((int) Math.floor(hr * ceiling));
        }
        setHeartZonesCache(cache);
    }

    public void autoCalculateHeartRest() {
        if (getHeartResting() == 0) {
            setHeartResting(60);
        }
    }

    public void autoCalculateHeartMax() {
        Date bd = getBirthdate();
        if (bd != null) {
            Date now = new Date();
            DateTime today = new DateTime(now);
            DateTime birthday = new DateTime(bd);
            int y1 = today.year().get();
            int y2 = birthday.year().get();
            int maxHR = (int) Math.round(205.8 - (0.685 * (y1 - y2)));
            setHeartMax(maxHR);
        }
        autoCalculateHeartRest();
    }

    public RealmList<Workout> getFavoriteWorkouts() {
        return favoriteWorkoutsCache;
    }

    public void setFavoriteWorkoutsCache(RealmList<Workout> favoriteWorkoutsCache) {
        this.favoriteWorkoutsCache = favoriteWorkoutsCache;
    }

    public void addFavorite(Workout workout) {
        if (favoriteWorkoutsCache == null) {
            favoriteWorkoutsCache = new RealmList<>();
        }
        favoriteWorkoutsCache.add(workout);
        ArrayList<String> array = new ArrayList<>();
        for (Workout w : favoriteWorkoutsCache) {
            array.add(w.getObjectId());
        }
        favsString = array.toString();

    }

    public void removeFavorite(Workout workout) {
        favoriteWorkoutsCache.remove(workout);
        if (favoriteWorkoutsCache.size() > 0) {
            ArrayList<String> array = new ArrayList<>();
            for (Workout w : favoriteWorkoutsCache) {
                array.add(w.getObjectId());
            }
            favsString = array.toString();
        } else {
            favsString = null;
        }
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public double getWeightKG() {
        return weightKG;
    }

    public void setWeightKG(double weightKG) {
        this.weightKG = weightKG;
    }

    public double getHeightCM() {
        return heightCM;
    }

    public void setHeightCM(double heightCM) {
        this.heightCM = heightCM;
    }

    public int getPowerFTP() {
        return powerFTP;
    }


    public void setPowerFTP(int powerFTP) {
        this.powerFTP = powerFTP;
    }

    public int getHeartMax() {
        return heartMax;
    }

    public void setHeartMax(int heartMax) {
        this.heartMax = heartMax;
    }

    public int getHeartResting() {
        return heartResting;
    }

    public void setHeartResting(int heartResting) {
        this.heartResting = heartResting;
    }

    public double getTotalDistanceKM() {
        return totalDistance;
    }

    public void setTotalDistanceKM(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalKilojoules() {
        return totalKiloJoules;
    }

    public void setTotalKilojoules(double totalKiloJoules) {
        this.totalKiloJoules = totalKiloJoules;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public JSONArray getCustomHuds() {
        JSONArray data = new JSONArray();
        if (customHuds != null) {
            try {
                data = new JSONArray(customHuds);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public String getFavsString() {
        return favsString;
    }

    public void setFavsString(String favsString) {
        this.favsString = favsString;
    }

    public void setCustomHuds(JSONArray customHuds) {
        this.customHuds = customHuds.toString();
    }

    public void setCustomHuds(String customHuds) {
        this.customHuds = customHuds;
    }

    public Date getUpdatedLast() {
        return updatedLast;
    }

    public void setUpdatedLast(Date updatedLast) {
        this.updatedLast = updatedLast;
    }

    public String getFreeCustom() {
        return freeCustom;
    }

    public void setFreeCustom(String freeCustom) {
        this.freeCustom = freeCustom;
    }

    static public int zoneForValue(int value, ArrayList<Integer> zones) {
        for (int i = zones.size(); i > 0; i--) {
            if (value > zones.get(i - 1)) {
                return i;
            }
        }
        return 0;
    }

    static public double percentOfFTP(int power, int ftp) {
        if (ftp > 0) {
            double percent = power / (double) ftp;
            return percent > 0 ? percent : 0;
        }
        return 0;
    }

    static public double percentOfReserve(int bpm, int resting, int max) {
        if (resting > 0 && max > resting) {
            int reserve = max - resting;
            int normalized = bpm - resting;
            double percent = normalized / (double) reserve;
            return percent > 0 ? percent : 0;
        }
        return 0;
    }

    static public double percentOfMax(int bpm, int max) {
        if (max > 0) {
            double percent = bpm / (double) max;
            return percent > 0 ? percent : 0;
        }
        return 0;
    }

    public static void logOut(final Context context) {
        broadcastLoggedOut(context);
    }

    private static void broadcastLoggedOut(final Context context) {
        context.sendBroadcast(new Intent(Profile.LOGGED_OUT));
    }


    public static void saveToParse() {

    }

    public void fetchWorkoutsFromRealm(Realm realm) {
        if (realm.isInTransaction()) {
            favoriteWorkoutsCache = new RealmList<>();
            ArrayList<String> favs = new ArrayList<>();
            String ss = favsString;
            RealmUtils.deSerializeStringArrayString(favsString, favs);
            Crashlytics.log("Favorites String : " + favsString);
            Workout result;
            for (String s : favs) {
                result = realm.where(Workout.class).equalTo("objectId", s).findFirst();
                if (result != null && !favoriteWorkoutsCache.contains(result)) {
                    Crashlytics.log("Favorite workout " + result.getObjectId());
                    favoriteWorkoutsCache.add(result);
                }
            }
        }
    }

    public static JsonObject serializeToJson(Profile profile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", profile.getName());
        jsonObject.addProperty("username", profile.getUsername());
        jsonObject.addProperty("email", profile.getEmail());
        jsonObject.addProperty("powerFTP", profile.getPowerFTP());
        jsonObject.add("birthdate", RealmUtils.serializeToJson(profile.getBirthdate()));
        jsonObject.add("heartZones", RealmUtils.serializeArrayToJson(profile.getHeartZonesCache()));
        jsonObject.addProperty("heartResting", profile.getHeartResting());
        jsonObject.addProperty("heartMax", profile.getHeartMax());
        jsonObject.addProperty("uuid", profile.getUuid());
        jsonObject.add("powerZones", RealmUtils.serializeArrayToJson(profile.getPowerZonesCache()));
        jsonObject.addProperty("weightKG", profile.getWeightKG());
        jsonObject.addProperty("totalTime", profile.getTotalTime());
        if (profile.getCustomHuds() != null) {
            JSONArray array = profile.getCustomHuds();
            JsonArray huds = new JsonArray();
            JsonParser parser = new JsonParser();
            for (int i = 0; i < array.length(); i++) {
                String hudId = "HUD" + i;
                JsonObject hud = new JsonObject();
                hud.addProperty("hudId", hudId);
                JsonArray trade = new JsonArray();
                try {
                    JsonElement tradeElement = parser.parse(array.get(i).toString());
                    trade = tradeElement.getAsJsonArray();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                hud.add("props", trade);
                huds.add(hud.toString());
            }
            jsonObject.add("customHUDs", huds);
        }
        if (profile.getFreeCustom() != null) {
            JsonArray free = new JsonArray();
            for (String id : profile.getFreeCustom().replace("[", "").replace("]", "").split("\\|")) {
                free.add(id);
            }
            jsonObject.add("freeCustom", free);
        }
        jsonObject.addProperty("totalKilojoules", profile.getTotalKilojoules());
        jsonObject.addProperty("totalDistanceKM", profile.getTotalDistanceKM());
        jsonObject.addProperty("heightCM", profile.getHeightCM());
        jsonObject.add("favoriteWorkouts", RealmUtils.serializeFavWorkouts(profile.getFavoriteWorkouts()));
//        TODO metric boolean?
//        TODO plan progress
        Log.d(CLASS_NAME, jsonObject.toString());
        return jsonObject;
    }

    public static String getUserName() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return "Kinetic User";
        }
        Profile p = current.get(0);
        String name = p.getName();
        realm.close();
        return name;
    }

    public static double getProfileWeightKG() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0.0;
        }
        Profile p = current.get(0);
        double w = p.getWeightKG();
        realm.close();
        return w;
    }

    public static int getProfileHeartMax() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        int h = p.getHeartMax();
        realm.close();
        return h;
    }

    public static int getProfileFTP() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        int f = p.getPowerFTP();
        realm.close();
        return f;
    }

    public static int getProfileRestingHR() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        int h = p.getHeartResting();
        realm.close();
        return h;
    }

    public static ArrayList<Integer> getProfileHRZones() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        ArrayList<Integer> list = p.getHeartZonesCache();
        realm.close();
        return list;
    }

    public static ArrayList<Integer> getProfilePowerZones() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return null;
        }
        Profile p = current.get(0);
        ArrayList<Integer> list = p.getPowerZonesCache();
        realm.close();
        return list;
    }

    public static boolean preferredRider(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return false;
        }
        Profile p = current.get(0);
        return p.getTotalDistanceKM() > 20;
    }

    public static double getProfileTotalTime(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        return p.getTotalTime();
    }

    public static double getProfileTotalDistanceKM(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        return p.getTotalDistanceKM();
    }

    public static double getProfileTotalKJ(){
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Profile> current = realm.where(Profile.class).findAll();
        if (current.isEmpty()) {
            realm.close();
            return 0;
        }
        Profile p = current.get(0);
        return p.getTotalKilojoules();
    }

    public static ArrayList<Integer> getDefaultPowerZones(){
        float ftp = Profile.getProfileFTP();
        ArrayList<Integer> cache = new ArrayList<>();
        for (double ceiling : PowerZoneDefaultCeilings) {
            cache.add((int) Math.floor(ftp * ceiling));
        }
        return cache;
    }

}
