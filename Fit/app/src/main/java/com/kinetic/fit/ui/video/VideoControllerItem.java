package com.kinetic.fit.ui.video;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VideoControllerItem {
    private static final String TAG = "VideoControllerItem";

    public String youTubeId = null;
    public String dropboxUrl;
    public String title;
    public boolean hidePopups = false;
    public boolean workoutSync = true;
    public Bitmap localThumb;
    public long duration;

    public VideoControllerItem() {

    }

    public Uri uri = null;
    public String streamingURL;
    public Map<String, String> cookies;

    // must be called from the background
    public void fetchStreamingCookies() {
        if (uri != null) {
            String url = uri.toString();
            try {
                // get JSON headers as VideoView does not follow redirects nor include Headers. Thanks Obama.
                Response<JSONObject> response = Webb.create().get(url).ensureSuccess().asJsonObject();
                String streaming = response.getBody().getString("url");
                String cookie = response.getBody().getString("cookie");

                streamingURL = streaming;
                cookies = new HashMap<>();
                cookies.put("Cookie", cookie);
            } catch (WebbException we) {
                Log.d(TAG, we.getLocalizedMessage());
            } catch (JSONException je) {
                Log.d(TAG, je.getLocalizedMessage());
            }
        }
    }

    public String getKPITitle(){
        if(streamingURL != null){
            return "Streaming: " + title;
        } else if(youTubeId != null){
            return "YouTube: " + title;
        } else {
            return "?";
        }
    }
}
