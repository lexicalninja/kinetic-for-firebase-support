package com.kinetic.fit.ui.settings;

import android.content.Intent;
import android.view.WindowManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitAlertDialog;
import com.kinetic.fit.ui.widget.SpinnerLoaderView;
import com.kinetic.fit.util.ViewStyling;
import com.kinetic.fit.util.ViewSwapper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import static com.kinetic.fit.connectivity.SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT;

/**
 * Created by Saxton on 7/17/17.
 */

@EActivity(R.layout.activity_sensor_update)
public class SensorUpdateActivity extends FitActivity {

    @ViewById(R.id.circle1)
    ImageView circle1;
    @ViewById(R.id.circle2)
    ImageView circle2;
    @ViewById(R.id.circle3)
    ImageView circle3;
    @ViewById(R.id.line1)
    LinearLayout line1;
    @ViewById(R.id.line2)
    LinearLayout line2;
    @ViewById(R.id.current_progress)
    TextView progressText;

    SpinnerLoaderView spinner;
    float scale;

    @AfterViews
    public void afterViews() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        scale = getResources().getDisplayMetrics().density;
        spinner = getSpinner(1);
        ViewSwapper.replaceView(circle1, spinner);
    }

    private void onProgressChange(int progress) {
        switch (progress) {
            case 30: {
                line1.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                ViewSwapper.replaceView(spinner, getCompletedImage(1));
                spinner = getSpinner(2);
                ViewSwapper.replaceView(circle2, spinner);
                progressText.setText(R.string.sensor_update_installing_firmware);
                break;
            }
            case 75: {
                line2.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                ViewSwapper.replaceView(spinner, getCompletedImage(2));
                spinner = getSpinner(3);
                ViewSwapper.replaceView(circle3, spinner);
                progressText.setText(R.string.sensor_update_validating_install);
                break;
            }
//            case 100: {
//                ViewSwapper.replaceView(spinner, getCompletedImage(3));
//                progressText.setText(R.string.sensor_update_complete);
//                break;
//            }
        }
    }

    private void showFirmwareUpdateComplete(){
        ViewSwapper.replaceView(spinner, getCompletedImage(3));
        progressText.setText(R.string.sensor_update_complete);
    }

    @UiThread
    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS)
    protected void onFirmwareUpdateProgress(Intent intent) {
        int progress = intent.getIntExtra(SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT, 0);
        onProgressChange(progress);
    }

    @UiThread
    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_COMPLETE)
    protected void onFirmwareUpdateComplete(Intent intent){
        showFirmwareUpdateComplete();
    }

    private int getPixelsFromDP(int dp) {
        return (int) (dp * scale + .5f);
    }

    private SpinnerLoaderView getSpinner(int position) {
        SpinnerLoaderView s = new SpinnerLoaderView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixelsFromDP(36), getPixelsFromDP(36));
        if (position == 1) {
            params.setMarginStart(getPixelsFromDP(32));
        } else if (position == 2) {

        } else {
            params.setMarginEnd(getPixelsFromDP(32));
        }
        s.setLayoutParams(params);
        return s;
    }

    private ImageView getCompletedImage(int position) {
        ImageView newView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixelsFromDP(36), getPixelsFromDP(36));
        if (position == 1) {
            params.setMarginStart(getPixelsFromDP(32));
        } else if (position == 2) {

        } else {
            params.setMarginEnd(getPixelsFromDP(32));
        }
        newView.setLayoutParams(params);
        newView.setImageResource(R.drawable.material_icon_check_mark);
        newView.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
        return newView;
    }

    @Override
    public void onBackPressed() {
       getBackAlert();
    }

    public void getBackAlert() {
        FitAlertDialog.show(this, getString(R.string.important),
                getString(R.string.sensor_update_please_stay_on_screen),
                getString(R.string.stay),
                getString(R.string.leave),
                null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                },
                true
        );
    }

}
