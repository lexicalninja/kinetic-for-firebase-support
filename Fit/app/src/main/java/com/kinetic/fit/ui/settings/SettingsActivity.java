package com.kinetic.fit.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.util.FITAudio;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 12/22/16.
 */


@EActivity(R.layout.activity_settings)
public class SettingsActivity extends FitActivity {
    private static final String TAG = "SettingActivity";
    public static final String HIDE_POPUPS = "hidePopups";
    public static final String AUTO_LAP_INDICATORS = "autoLapsOn";
    public static final String EVENT_CUES_ON = "eventCuesOn";
    public static final String VOICE_OVERS_ON = "voiceOversOn";
    public static final String ZONE_CUES_ON = "zoneCuesOn";
    public static final String LIGHT_THEME_ON = "lightThemeOn";
    public static final String DIFFICULTY_PCT = "difficultyPercent";
    public static final String GRAPH_POWER = "graphPower";
    public static final String GRAPH_HEART = "graphHeart";
    public static final String GRAPH_CADENCE = "graphCadence";


    public static final String PREV_SPEED_SENSOR = "prevSpeedSensor";
    public static final String PREV_POWER_SENSOR = "prevPowerSensor";
    public static final String PREV_HEART_SENSOR = "prevHeartSensor";
    public static final String PREV_CADENCE_SENSOR = "prevCadenceSensor";

    public static final String TTS_ENABLED = "ttsEnabled";


    public static final boolean AutoLapEnabled = false;


    @ViewById(R.id.pop_ups_switch)
    SwitchCompat popupsSwitch;
    @ViewById(R.id.autolap_switch)
    SwitchCompat autolapSwitch;
    @ViewById(R.id.event_cues_switch)
    SwitchCompat eventCuesSwitch;
    @ViewById(R.id.voice_over_switch)
    SwitchCompat voiceOverSwitch;
    @ViewById(R.id.zone_cues_switch)
    SwitchCompat zoneCuesSwitch;
    @ViewById(R.id.light_theme_switch)
    SwitchCompat lightThemeSwitch;
    @ViewById(R.id.difficulty_seek_bar)
    SeekBar difficultySeekBar;
    @ViewById(R.id.difficulty_zero_button)
    ImageButton difficultyZeroButton;
    @ViewById(R.id.difficulty_percent_text)
    TextView difficultyPercentText;
    @ViewById(R.id.voice_over_help_text)
    TextView voiceOverHelpText;
    @ViewById(R.id.autolap_text)
    TextView autolapText;
    @ViewById(R.id.autolap_hint)
    TextView autolapHint;
    @ViewById(R.id.power_switch)
    TextView powerSwitch;
    @ViewById(R.id.heart_switch)
    TextView heartSwitch;
    @ViewById(R.id.cadence_switch)
    TextView cadenceSwitch;

    SharedPreferences sharedPreferences;
    String userUuid;
    boolean initializing = true;
    FITAudio fitAudio;
    boolean themeChange = false;
    TextToSpeech tts;

    @CheckedChange(R.id.pop_ups_switch)
    void setHidePopups(CompoundButton option, boolean isChecked) {
        sharedPreferences.edit().putBoolean(HIDE_POPUPS + userUuid, isChecked).apply();
    }

    @CheckedChange(R.id.autolap_switch)
    void setAutoLapIndicators(CompoundButton option, boolean isChecked) {
        sharedPreferences.edit().putBoolean(AUTO_LAP_INDICATORS + userUuid, isChecked).apply();
    }

    @CheckedChange(R.id.event_cues_switch)
    void setEventCuesOn(CompoundButton option, boolean isChecked) {
        if (!initializing) {
            sharedPreferences.edit().putBoolean(EVENT_CUES_ON + userUuid, isChecked).apply();
            if (isChecked) {
                fitAudio.playFITSound(FITAudio.SoundId.CueSelect);
            }
        }
    }

    @CheckedChange(R.id.voice_over_switch)
    void setVoiceOversOn(CompoundButton option, boolean isChecked) {
        if (!initializing) {
            sharedPreferences.edit().putBoolean(VOICE_OVERS_ON + userUuid, isChecked).apply();
            if (isChecked) {
                fitAudio.playVoiceOver(getString(R.string.settings_activity_voice_overs_on_string));
            }
        }
    }

    @CheckedChange(R.id.zone_cues_switch)
    void setZoneCuesOn(CompoundButton option, boolean isChecked) {
        if (!initializing) {
            sharedPreferences.edit().putBoolean(ZONE_CUES_ON + userUuid, isChecked).apply();
            if (isChecked) {
                fitAudio.playFITSound(FITAudio.SoundId.CueSelect);
            }
        }
    }

    @CheckedChange(R.id.light_theme_switch)
    void setLightThemeOn(CompoundButton option, boolean isChecked) {
        if (!initializing) {
            sharedPreferences.edit().putBoolean(LIGHT_THEME_ON + userUuid, isChecked).apply();
            themeChange = !themeChange;
        }
    }

    @SeekBarProgressChange(R.id.difficulty_seek_bar)
    void onDifficultySeekBarProgressChange(SeekBar seekBar, int progress) {
        difficultyPercentText.setText(getString(R.string.setting_activity_dificulty_percent_text, progress - 50));
    }

    @SeekBarTouchStop(R.id.difficulty_seek_bar)
    void onDifficultySeekBarStop(SeekBar seekBar) {
        sharedPreferences.edit().putInt(DIFFICULTY_PCT + userUuid, seekBar.getProgress() - 50).apply();
    }

    @Click(R.id.difficulty_zero_button)
    void resetDifficulty() {
        difficultySeekBar.setProgress(50);
        sharedPreferences.edit().putInt(DIFFICULTY_PCT + userUuid, difficultySeekBar.getProgress() - 50).apply();
    }

    @Click(R.id.power_switch)
    void togglePowerGraph() {
        if (sharedPreferences.getBoolean(GRAPH_POWER + userUuid, true)) {
            powerSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            sharedPreferences.edit().putBoolean(GRAPH_POWER + userUuid, false).apply();
        } else {
            powerSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            sharedPreferences.edit().putBoolean(GRAPH_POWER + userUuid, true).apply();
        }
    }

    @Click(R.id.heart_switch)
    void toggleHeartGraph() {
        if (sharedPreferences.getBoolean(GRAPH_HEART + userUuid, false)) {
            heartSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            sharedPreferences.edit().putBoolean(GRAPH_HEART + userUuid, false).apply();
        } else {
            heartSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            sharedPreferences.edit().putBoolean(GRAPH_HEART + userUuid, true).apply();
        }
    }

    @Click(R.id.cadence_switch)
    void toggleCadenceGraph() {
        if (sharedPreferences.getBoolean(GRAPH_CADENCE + userUuid, false)) {
            cadenceSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
            sharedPreferences.edit().putBoolean(GRAPH_CADENCE + userUuid, false).apply();
        } else {
            cadenceSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
            sharedPreferences.edit().putBoolean(GRAPH_CADENCE + userUuid, true).apply();
        }
    }


    @AfterViews
    void afterViews() {
        sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
        userUuid = Profile.getUUID();
        popupsSwitch.setChecked(sharedPreferences.getBoolean(HIDE_POPUPS + userUuid, false));
        autolapSwitch.setChecked((sharedPreferences.getBoolean(AUTO_LAP_INDICATORS + userUuid, false)));
        eventCuesSwitch.setChecked(sharedPreferences.getBoolean(EVENT_CUES_ON + userUuid, false));
        checkTTS();
        if (sharedPreferences.getBoolean(TTS_ENABLED + userUuid, true)) {
            voiceOverSwitch.setChecked(sharedPreferences.getBoolean(VOICE_OVERS_ON + userUuid, false));
        } else {
            voiceOverSwitch.setVisibility(View.GONE);
            voiceOverHelpText.setText(getString(R.string.setting_activity_no_tts_installed));
        }
        zoneCuesSwitch.setChecked(sharedPreferences.getBoolean(ZONE_CUES_ON + userUuid, false));
        lightThemeSwitch.setChecked(sharedPreferences.getBoolean(LIGHT_THEME_ON + userUuid, false));
        difficultySeekBar.setProgress(sharedPreferences.getInt(DIFFICULTY_PCT + userUuid, 0) + 50);
        difficultyPercentText.setText(getString(R.string.setting_activity_dificulty_percent_text, sharedPreferences.getInt(DIFFICULTY_PCT + userUuid, 0)));
        initializing = false;

        if (!AutoLapEnabled) {
            autolapHint.setVisibility(View.GONE);
            autolapSwitch.setVisibility(View.GONE);
            autolapText.setVisibility(View.GONE);
        }

        if (sharedPreferences.getBoolean(GRAPH_POWER + userUuid, true)) {
            powerSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        } else {
            powerSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
        }
        if (sharedPreferences.getBoolean(GRAPH_HEART + userUuid, false)) {
            heartSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        } else {
            heartSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
        }
        if (sharedPreferences.getBoolean(GRAPH_CADENCE + userUuid, false)) {
            cadenceSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        } else {
            cadenceSwitch.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, this));
        }
    }

    public static String getSettingsNamespace() {
        return TAG;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fitAudio = new FITAudio(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("recreate", themeChange);
        setResult(RESULT_OK, result);
        finish();
    }

    void checkTTS() {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    sharedPreferences.edit().putBoolean(TTS_ENABLED + userUuid, true).apply();
                } else {
                    sharedPreferences.edit().putBoolean(TTS_ENABLED + userUuid, false).apply();
                }
                if(tts != null) {
                    tts.shutdown();
                }
            }
        });

    }

    void redrawActivity() {
        recreate();
    }

    @Override
    protected void onDestroy() {
        if(tts != null){
            tts.shutdown();
        }
        fitAudio.releaseSoundPool();
        super.onDestroy();
    }
}
