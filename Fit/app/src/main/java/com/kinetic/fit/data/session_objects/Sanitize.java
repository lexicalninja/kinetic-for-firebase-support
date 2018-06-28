package com.kinetic.fit.data.session_objects;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Sanitize {

    public static ArrayList<Double> toDoubleArray(JSONArray array) {
        ArrayList<Double> primitives = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    primitives.add(array.getDouble(i));
                } catch (JSONException e) {
                    // this better not happen...
                }
            }
        }
        return primitives;
    }

    public static ArrayList<Integer> toIntArray(JSONArray array) {
        ArrayList<Integer> primitives = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    primitives.add(array.getInt(i));
                } catch (JSONException e) {
                    // this better not happen...
                }
            }
        }
        return primitives;
    }

    public static ArrayList<String> toStringArray(JSONArray array){
        ArrayList<String> primatives = new ArrayList<>();
        if(array != null){
            for(int i = 0; i < array.length(); i++){
                try{
                    primatives.add(array.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                    // watch, I bet this happens at some point :P
                }
            }
        }

        return primatives;
    }
}
