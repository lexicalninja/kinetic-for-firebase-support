package com.kinetic.fit.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.crashlytics.android.Crashlytics;
import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.settings.SettingsActivity;

import java.util.Locale;

/**
 * Created by Saxton on 1/5/17.
 */


public class    FITAudio implements SoundPool.OnLoadCompleteListener {

    private static final String TAG = "FITAudio";
    private static final int MAX_SOUND_STREAMS = 3;
    private static final int SOUND_QUALITY = 0;

    private Context applicationContext;
    private SoundPool soundPool;
    private TextToSpeech tts;
    private int startWorkoutSound;
    private int endWorkoutSound;
    private int pauseWorkoutSound;
    private int resumeWorkoutSound;
    private int cueSelectSound;
    private int zoneStartSound;
    private int zoneUpSound;
    private int zoneDownSound;

    public enum SoundId {
        StartWorkout,
        PauseWorkout,
        ResumeWorkout,
        EndWorkout,
        CueSelect,
        ZoneStart,
        ZoneUp,
        ZoneDown
    }

    public FITAudio(final Context applicationContext) {
        this.applicationContext = applicationContext;
        SharedPreferences sharedPref = applicationContext.getSharedPreferences(SettingsActivity.getSettingsNamespace(), Context.MODE_PRIVATE);
        boolean useTTS = sharedPref.getBoolean(SettingsActivity.TTS_ENABLED + Profile.getUUID(), true);
        if (useTTS) {
            tts = new TextToSpeech(applicationContext, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Crashlytics.log("locale: " + Locale.getDefault().toString());
                    if (status == TextToSpeech.SUCCESS) {
                        if (tts.isLanguageAvailable(Locale.getDefault()) != -2) {
                            Crashlytics.log("Is lang avail int: " + tts.isLanguageAvailable(Locale.getDefault()) );
                            Locale locale = Locale.getDefault() != null ? Locale.getDefault() : Locale.US;
                            tts.setLanguage(locale);
                        }
                    } else {
                        SharedPreferences sharedPref = applicationContext.getSharedPreferences(SettingsActivity.getSettingsNamespace(), Context.MODE_PRIVATE);                        tts = null;
                        sharedPref.edit().putBoolean(SettingsActivity.TTS_ENABLED, false).apply();
                    }
                }
            });
        }
        soundPool = new SoundPool(MAX_SOUND_STREAMS, AudioManager.STREAM_MUSIC, SOUND_QUALITY);
        soundPool.setOnLoadCompleteListener(this);
        startWorkoutSound = soundPool.load(applicationContext, R.raw.start_of_workout_sound, 1);
        endWorkoutSound = soundPool.load(applicationContext, R.raw.end_of_workout_sound, 1);
        cueSelectSound = soundPool.load(applicationContext, R.raw.cue_select_sound, 1);
        zoneStartSound = soundPool.load(applicationContext, R.raw.zone_start_sound, 1);
        pauseWorkoutSound = soundPool.load(applicationContext, R.raw.end_of_workout_sound, 1);
        resumeWorkoutSound = soundPool.load(applicationContext, R.raw.start_of_workout_sound, 1);
        zoneDownSound = soundPool.load(applicationContext, R.raw.zone_down_sound, 1);
        zoneUpSound = soundPool.load(applicationContext, R.raw.zone_up_sound, 1);
    }

    public void playFITSound(SoundId soundId) {
        switch (soundId) {
            case StartWorkout: {
                soundPool.play(startWorkoutSound, 1, 1, 0, 0, 1);
                break;
            }
            case PauseWorkout: {
                soundPool.play(pauseWorkoutSound, 1, 1, 0, 0, 1);
                break;
            }
            case ResumeWorkout: {
                soundPool.play(resumeWorkoutSound, 1, 1, 0, 0, 1);
                break;
            }
            case EndWorkout: {
                soundPool.play(endWorkoutSound, 1, 1, 0, 0, 1);
                break;
            }
            case CueSelect: {
                soundPool.play(cueSelectSound, 1, 1, 0, 0, 1);
                break;
            }
            case ZoneStart: {
                soundPool.play(zoneStartSound, 1, 1, 0, 0, 1);
                break;
            }
            case ZoneUp: {
                soundPool.play(zoneUpSound, 1, 1, 0, 0, 1);
                break;
            }
            case ZoneDown: {
                soundPool.play(zoneDownSound, 1, 1, 0, 0, 1);
                break;
            }
        }
    }

    public void playVoiceOver(String textToSpeak) {
        if (tts != null) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

    }

    public void releaseSoundPool(){
        soundPool.release();
    }
}
