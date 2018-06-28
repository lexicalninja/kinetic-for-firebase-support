package com.kinetic.fit.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saxton on 1/4/17.
 */

public class FITZoneMonitor {
    private final static String TAG = "FITZoneMonitor";

    private static final double ZONE_DURATION_REQ = 2.5;
    Context context;

    private int instantPowerZone = 0;
    private int potentialPowerZone = -1;
    private int potentialHeartZone = -1;
    private int currentPowerZone = -1;
    private int currentHeartZone = -1;

    private double potentialPowerZoneTime = 0;
    private double potentialHeartZoneTime = 0;
    private boolean zoneSFX;
    private boolean voiceOver;
    private FITAudio fitAudio;
    private boolean autoLap;
    private List<AutoLapObserver> observers;
    public interface AutoLapObserver{
        void autoLap();
    }

    public FITZoneMonitor(Context context) {
        this.context = context;
        fitAudio = new FITAudio(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingsActivity.getSettingsNamespace(), Context.MODE_PRIVATE);
        zoneSFX = sharedPreferences.getBoolean(SettingsActivity.ZONE_CUES_ON + Profile.getUUID(), false);
        voiceOver = sharedPreferences.getBoolean(SettingsActivity.VOICE_OVERS_ON + Profile.getUUID(), false);
        autoLap = sharedPreferences.getBoolean(SettingsActivity.AUTO_LAP_INDICATORS + Profile.getUUID(), false);
        observers = new ArrayList<>();
    }

    public void setCurrentPowerZone(int powerZone) {
        if (instantPowerZone != powerZone) {
            instantPowerZone = powerZone;
            onPowerZoneChanged(powerZone);
        }
    }

    public void setCurrentHeartZone(int currentHeartZone) {
        if (this.currentHeartZone != currentHeartZone) {
            onHeartZoneChanged(currentHeartZone);
        }
    }

    public void addTime(double timeDelta, int powerZone, int heartZone) {
        if (potentialPowerZone != -1) {
            potentialPowerZoneTime += timeDelta;
//            Log.d(TAG, "addTime: " + potentialPowerZone + " (" + potentialPowerZoneTime + ")");
            if (potentialPowerZoneTime >= ZONE_DURATION_REQ) {
                changePowerZone(potentialPowerZone);
            }
        } else {
            if (currentPowerZone == -1) {
                changePowerZone(powerZone);
            }
        }
        if (potentialHeartZone != -1) {
            potentialHeartZoneTime += timeDelta;
            if (potentialHeartZoneTime >= ZONE_DURATION_REQ) {
                changeHeartZone(potentialHeartZone);
            }
        } else {
            if (currentHeartZone == -1) {
                changeHeartZone(heartZone);
            }
        }
    }

    private void onPowerZoneChanged(int newPowerZone) {
        if (currentPowerZone == newPowerZone) {
            potentialPowerZone = -1;
        } else {
            potentialPowerZone = newPowerZone;
        }
        potentialPowerZoneTime = 0;

//        Log.d(TAG, "onPowerZoneChanged: " + newPowerZone);
    }

    private void onHeartZoneChanged(int newHeartZone) {
        if (currentHeartZone == newHeartZone) {
            potentialHeartZone = -1;
        } else {
            potentialHeartZone = newHeartZone;
        }
        potentialHeartZoneTime = 0;
    }

    private void changePowerZone(int powerZone) {
        int oldValue = currentPowerZone;
        currentPowerZone = powerZone;
//        Log.d(TAG, "changePowerZone: " + powerZone + " Old: " + oldValue);
        if (oldValue != -1) {
            if (currentPowerZone > 0) {
                if(voiceOver) {
//                TODO Voice Over?
                    fitAudio.playVoiceOver(context.getString(R.string.fit_audio_voice_over_string_power_zone, currentPowerZone));
                }
                if (zoneSFX) {
                    if (oldValue < currentPowerZone) {
//                        TODO play up sound
                        fitAudio.playFITSound(FITAudio.SoundId.ZoneUp);
                    } else {
//                        TODO play down sound
                        fitAudio.playFITSound(FITAudio.SoundId.ZoneDown);
                    }
                }
                if(autoLap){
                    markLap();
                }
            }
        }
        potentialPowerZone = -1;
        potentialPowerZoneTime = 0;
    }

    private void changeHeartZone(int heartZone) {
        int oldValue = currentHeartZone;
        currentHeartZone = heartZone;
        if (oldValue != -1) {
            if (currentHeartZone > 0) {
               if(voiceOver) {
//                TODO Voice Over?
                   fitAudio.playVoiceOver(context.getString(R.string.fit_audio_voice_over_string_heart_rate_zone, currentHeartZone));
               }
                if (zoneSFX) {
                    if (oldValue < currentHeartZone) {
//                        TODO play up sound
                        fitAudio.playFITSound(FITAudio.SoundId.ZoneUp);
                    } else {
//                        TODO play down sound
                        fitAudio.playFITSound(FITAudio.SoundId.ZoneDown);
                    }
                }
            }
        }
    }

    public void addObserver(AutoLapObserver observer){
        observers.add(observer);
    }

    public void removeObserver(AutoLapObserver observer){
        observers.remove(observer);
    }

    public void markLap(){
        for(AutoLapObserver observer: observers){
            observer.autoLap();
        }
    }
}
