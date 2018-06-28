package com.kinetic.fit.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Saxton on 5/9/17.
 */

public class JsonFromFile {
    private static final String TAG = "JSONFromFile";
    Context mContext;

    public JsonFromFile(Context context) throws JSONException{
        this.mContext = context;
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("country.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

//        try {
//            JSONObject obj = new JSONObject(loadJSONFromAsset(mContext));
//            JSONArray m_jArry = obj.getJSONArray("formules");
//            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
//            HashMap<String, String> m_li;
//
//            for (int i = 0; i < m_jArry.length(); i++) {
//                JSONObject jo_inside = m_jArry.getJSONObject(i);
//                Log.d("Details-->", jo_inside.getString("formule"));
//                String formula_value = jo_inside.getString("formule");
//                String url_value = jo_inside.getString("url");
//
//                //Add your values in your `ArrayList` as below:
//                m_li = new HashMap<String, String>();
//                m_li.put("formule", formula_value);
//                m_li.put("url", url_value);
//
//                formList.add(m_li);
//                return
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//            return  null;
//        }
//    }

}
