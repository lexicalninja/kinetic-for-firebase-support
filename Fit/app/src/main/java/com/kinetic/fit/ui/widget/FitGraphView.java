package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
import com.kinetic.fit.ui.settings.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by Saxton on 4/4/17.
 */

public class FitGraphView extends View {
    protected static final float LINE_WEIGHT_CURRENT_TIME = 1.0f;
    protected static final int LINE_COLOR_POWER = R.color.fit_dark_power;

    protected static final int LINE_COLOR_HEARTRATE = R.color.fit_dark_heart;
    protected static final float MAX_CADENCE = 120f;
    protected static final int LINE_COLOR_CADENCE = R.color.fit_dark_cadence;

    protected Path mPowerLinePath;
    protected Paint mPowerLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    protected Path mHeartRateLinePath;
    protected Paint mHeartRateLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    protected Path mCadenceLinePath;
    protected Paint mCadenceLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    protected float mMaxWatts;
    protected float mMaxHeartRate;
    protected float mPPS;
    protected float mPPW;
    protected float mFTP;
    protected float height = 0f;
    protected float width = 0f;

    private Session mSession;
    protected Profile mProfile;

    protected ArrayList<PointF> mPowerLineArray;
    protected ArrayList<PointF> mHeartRateLineArray;
    protected ArrayList<PointF> mCadenceLineArray;
    private SharedPreferences prefs;


    public FitGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSession(Session session) {
        this.mSession = session;
        init();
    }

    void init() {
        prefs = getContext().getSharedPreferences(SettingsActivity.getSettingsNamespace(), Context.MODE_PRIVATE);
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mPowerLinePath = new Path();
        mHeartRateLinePath = new Path();
        mCadenceLinePath = new Path();

        mPowerLinePen.setStrokeWidth(metrics.density * LINE_WEIGHT_CURRENT_TIME);
        mPowerLinePen.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPowerLinePen.setStyle(Paint.Style.STROKE);
        mPowerLinePen.setColor(getResources().getColor(LINE_COLOR_POWER));

        mHeartRateLinePen.setStrokeWidth(metrics.density * LINE_WEIGHT_CURRENT_TIME);
        mHeartRateLinePen.setFlags(Paint.ANTI_ALIAS_FLAG);
        mHeartRateLinePen.setStyle(Paint.Style.STROKE);
        mHeartRateLinePen.setColor(getResources().getColor(LINE_COLOR_HEARTRATE));

        mCadenceLinePen.setStrokeWidth(metrics.density * LINE_WEIGHT_CURRENT_TIME);
        mCadenceLinePen.setFlags(Paint.ANTI_ALIAS_FLAG);
        mCadenceLinePen.setStyle(Paint.Style.STROKE);
        mCadenceLinePen.setColor(getResources().getColor(LINE_COLOR_CADENCE));

        if (isInEditMode()) {
            mMaxWatts = 300;
            mFTP = 200;
            return;
        }

        mProfile = Profile.current();
        mMaxWatts = (float) mSession.getProfilePowerZones().get(mProfile.getPowerZonesCache().size() - 1) + 30;
        mMaxHeartRate = (float) mProfile.getHeartMax();
        mFTP = (float) mProfile.getPowerFTP();
        mPowerLineArray = new ArrayList<>();
        mHeartRateLineArray = new ArrayList<>();
        mCadenceLineArray = new ArrayList<>();

        for (SessionDataSlice slice : mSession.getDataSlices()) {
            double ts = slice.timestamp;
            mPowerLineArray.add(new PointF((float) (slice.timestamp / mSession.getDuration()), (float) slice.currentPower));
            mHeartRateLineArray.add(new PointF((float) (slice.timestamp / mSession.getDuration()), (float) slice.currentHeartRate));
            mCadenceLineArray.add(new PointF((float) (slice.timestamp / mSession.getDuration()), (float) slice.currentCadence));
        }
        invalidate();
    }

    public void makePath(ArrayList<PointF> list, Path path, Canvas canvas, Paint pen, float max) {
        float x, y;
        if (list != null) {
            path.reset();
            path.moveTo(-1, getHeight());
            for (PointF pt : list) {
                x = pt.x * getWidth();
                y = getHeight() - (pt.y / max * getHeight());
                path.lineTo(x, y);
            }
            canvas.drawPath(path, pen);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        float x;
//        float y1;
//        float y2 = 0;
//        float power;
        if (mSession != null) {
            mPPS = (float) (getWidth() / mSession.getDuration());
            mPPW = getHeight() / mMaxWatts;
            if (prefs.getBoolean(SettingsActivity.GRAPH_POWER + Profile.getUUID(), true)) {
                makePath(mPowerLineArray, mPowerLinePath, canvas, mPowerLinePen, mMaxWatts);
            }
            if (prefs.getBoolean(SettingsActivity.GRAPH_HEART + Profile.getUUID(), false)) {
                makePath(mHeartRateLineArray, mHeartRateLinePath, canvas, mHeartRateLinePen, mMaxHeartRate);
            }
            if (prefs.getBoolean(SettingsActivity.GRAPH_CADENCE + Profile.getUUID(), false)) {
                makePath(mCadenceLineArray, mCadenceLinePath, canvas, mCadenceLinePen, MAX_CADENCE);
            }
        }
    }
}
