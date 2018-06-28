package com.kinetic.fit.util;

import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.TrainingPlan;
import com.kinetic.fit.data.realm_objects.Workout;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Date;

import io.realm.RealmList;

/**
 * Created by Saxton on 1/19/17.
 */

public class RealmUtils {
    private static final String TAG = "RealmUtils";
    private static final String CLASS_NAME = "RealmUtils";

    public enum PrimitiveType {
        Int,
        Double,
        String;
    }

    public static void deSerializeIntegerArrayString(String arrayString, ArrayList<Integer> targetArray) {
        String[] items = arrayString.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        targetArray.clear();
        for (int i = 0; i < items.length; i++) {
            try {
                targetArray.add((int) Double.parseDouble(items[i]));
            } catch (NumberFormatException nfe) {
                Log.d(CLASS_NAME, nfe.getLocalizedMessage());
            }
        }
    }

    public static void deSerializeDoubleArrayString(String arrayString, ArrayList<Double> targetArray) {
        String[] items = arrayString.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        targetArray.clear();
        for (int i = 0; i < items.length; i++) {
            try {
                targetArray.add(Double.parseDouble(items[i]));
            } catch (NumberFormatException nfe) {
                Log.d(CLASS_NAME, nfe.getLocalizedMessage());
            }
        }
    }

    public static ArrayList<String> deSerializeStringArrayString(@Nullable String arrayString, ArrayList<String> targetArray) {
        if(arrayString != null && arrayString.length() > 2) {
            String[] items = arrayString.replaceAll("\\[", "")
                    .replaceAll("\\]", "")
                    .replaceAll("\\s", "")
                    .replaceAll("\"", "")
                    .split(",");
            targetArray.clear();
            for (int i = 0; i < items.length; i++) {
                try {
                    targetArray.add(items[i]);
                } catch (NumberFormatException nfe) {
                    Log.d(CLASS_NAME, nfe.getLocalizedMessage());
                }
            }
            return targetArray;
        } else {
            return null;
        }
    }

    public static ArrayList<Integer> getStringAsIntegerArray(String arrayString) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        if (arrayString != null && !arrayString.isEmpty()) {
            String[] items = arrayString.replaceAll("\\[", "")
                    .replaceAll("\\]", "")
                    .replaceAll("\\s", "")
                    .split(",");
            for (String s : items) {
                arrayList.add(Integer.parseInt(s));
            }
            if(arrayList.size() < 1){
                arrayList = Profile.getDefaultPowerZones();
            }
        }
        return arrayList;
    }

    public static ArrayList<Double> getStringAsDoubleArray(String arrayString) {
        ArrayList<Double> arrayList = new ArrayList<>();
        if (arrayString != null) {
            String[] items = arrayString.replaceAll("\\[", "")
                    .replaceAll("\\]", "")
                    .replaceAll("\\s", "")
                    .split(",");

            for (String s : items) {
                if(!s.equals("")) {
                    arrayList.add(Double.parseDouble(s));
                }
            }
        }
        return arrayList;
    }

    public static JsonObject serializeToJson(Date date) {
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        DateTime dt = new DateTime(date).withZone(DateTimeZone.UTC);
//        System.out.println(df.print(dt));
        JsonObject j = new JsonObject();
        j.addProperty("__type", "Date");
        j.addProperty("iso", df.print(dt));
        return j;
    }

    public static long deserializeDateFromJson(JsonObject dateObject){
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        if (dateObject != null) {
            return df.parseDateTime(dateObject.get("iso").getAsString()).getMillis();
        } else {
           return new DateTime().minusDays(1).getMillis();
        }

    }

    public static JsonElement serializeArrayToJson(ArrayList<? extends Number> array) {
        return new Gson().toJsonTree(array);
    }

    public static JsonElement serializeFavWorkouts(RealmList<Workout> list) {
        if(list.size() > 0) {
            ArrayList<String> favs = new ArrayList<>();
            for (Workout w : list) {
                favs.add(w.getObjectId());
            }
            return new Gson().toJsonTree(favs);
        } else {
            return null;
        }
    }

    public static JsonObject serializeToJsonByte64(byte[] bytes) {
        JsonObject j = new JsonObject();
        j.addProperty("__type", "Bytes");
        j.addProperty("base64", Base64.encodeToString(bytes, Base64.NO_WRAP));
        return j;
    }

    public static JsonObject serializeProfilePointerToJson() {
        JsonObject j = new JsonObject();
        j.addProperty("__type", "Pointer");
        j.addProperty("className", "_User");
        j.addProperty("objectId", Profile.current().getObjectId());
        return j;
    }

    public static JsonObject serializeTraininglanPointerToJson(TrainingPlan tp){
        JsonObject j = new JsonObject();
        j.addProperty("__type", "Pointer");
        j.addProperty("className", "TrainingPlan");
        j.addProperty("objectId", tp.getObjectId());
        return j;
    }

    public static JsonObject getACLs(){
        JsonObject j = new JsonObject();
        j.addProperty("read", true);
        j.addProperty("write", true);
        JsonObject acl = new JsonObject();
        acl.add(Profile.current().getObjectId(), j);
        return acl;
    }

}
