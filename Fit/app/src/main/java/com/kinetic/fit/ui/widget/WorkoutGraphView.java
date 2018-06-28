package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.data.objects.WorkoutInterval;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.util.ViewStyling;

import java.util.ArrayList;

public class WorkoutGraphView extends View {

    public static final String TAG = "WorkoutGraphView";
    static final String NAME_SPACE = "http://schemas.android.com/apk/res/com.kinetic.fit";
    static final String PROGRESS_LINE_ATTRIBUTE = "showProgressLine";


    private static final float LINE_WEIGHT_CURRENT_TIME = 1.0f;
    private static final int LINE_COLOR_BASE = R.attr.colorFitBody;
    private static final int LINE_COLOR_POWER = R.color.fit_dark_power;

    private static final int LINE_COLOR_HEARTRATE = R.color.fit_dark_heart;
    private static final float MAX_CADENCE = 120f;
    private static final int LINE_COLOR_CADENCE = R.color.fit_dark_cadence;

    private float mMaxWatts;
    private float mMaxHeartRate;
    private float mPPS;
    private float mPPW;
    private float mFTP;
    private LinearGradient mGradient;
    private int gradientColorBase = R.attr.colorFitBg0;
    private int gradientColorOutline = R.attr.colorFitHeadline;

    private float[] mCurrentTimeLineCoordinates = new float[4];
    private Paint mCurrentTimeLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private Path mWorkoutOutlinePath;
    private Paint mWorkoutOutlinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private Path mPowerLinePath;
    private Paint mPowerLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    private Path mHeartRateLinePath;
    private Paint mHeartRateLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    private Path mCadenceLinePath;
    private Paint mCadenceLinePen = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.EMBEDDED_BITMAP_TEXT_FLAG);

    private boolean mCurrentTimeLineVisibility = true;

    float height = 0f;
    float width = 0f;

    private Workout mWorkout;
    private Profile mProfile;

    private ArrayList<PointF> mPowerLineArray;
    private ArrayList<PointF> mHeartRateLineArray;
    private ArrayList<PointF> mCadenceLineArray;

//    public WorkoutGraphView(Context context) {
//        super(context);
//        init();
//    }

    public WorkoutGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCurrentTimeLineVisibility = attrs.getAttributeBooleanValue(NAME_SPACE, PROGRESS_LINE_ATTRIBUTE, true);
        init();
    }

    public void setCurrentTimeLineVisibility(boolean visible) {
        mCurrentTimeLineVisibility = visible;
    }

    public void drawEntireWorkoutPower(Workout workout) {
        mWorkout = workout;
        invalidate();
    }

    private void init() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mCurrentTimeLinePen.setStrokeWidth(metrics.density * LINE_WEIGHT_CURRENT_TIME);
        mCurrentTimeLinePen.setFlags(Paint.ANTI_ALIAS_FLAG);
        mCurrentTimeLinePen.setStrokeCap(Paint.Cap.ROUND);
        mCurrentTimeLinePen.setStrokeJoin(Paint.Join.ROUND);
        mCurrentTimeLinePen.setStyle(Paint.Style.FILL_AND_STROKE);
        mCurrentTimeLinePen.setColor(ViewStyling.getColor(LINE_COLOR_BASE, getContext()));

        mWorkoutOutlinePen.setStrokeWidth(metrics.density * LINE_WEIGHT_CURRENT_TIME);
        mWorkoutOutlinePen.setStyle(Paint.Style.FILL_AND_STROKE);
        mWorkoutOutlinePen.setColor(getGradientColorOutline());

        mWorkoutOutlinePath = new Path();
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
        mMaxWatts = (float) mProfile.getPowerZonesCache().get(mProfile.getPowerZonesCache().size() - 1) + 30;
        mMaxHeartRate = (float) mProfile.getHeartMax();
        mFTP = (float) mProfile.getPowerFTP();

        mPowerLineArray = new ArrayList<>();
        mHeartRateLineArray = new ArrayList<>();
        mCadenceLineArray = new ArrayList<>();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        height = getHeight();
        width = getWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        //int minh = MeasureSpec.getSize(w) - 20 + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w) - 20, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);

        mCurrentTimeLineCoordinates[0] = 0;
        mCurrentTimeLineCoordinates[1] = getPaddingTop();
        mCurrentTimeLineCoordinates[2] = 0;
        mCurrentTimeLineCoordinates[3] = h - getPaddingBottom();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mWorkoutOutlinePen.setColor(getGradientColorOutline());
        mGradient = new LinearGradient(0, (int) (height * 0.1), 0, height,
                getGradientColorOutline(),
                ViewStyling.getColor(gradientColorBase, getContext()),
                Shader.TileMode.CLAMP);
        super.onDraw(canvas);

        float x;
        float y1;
        float y2 = 0;
        float power;

        if (mWorkout != null && mWorkout.getIntervals().size() > 0) {
            mPPS = (float) (getWidth() / mWorkout.getDuration());
            mPPW = getHeight() / mMaxWatts;

            x = 0;

            mWorkoutOutlinePath.reset();
            mWorkoutOutlinePath.moveTo(x - 10, getHeight() + 5);
            mWorkoutOutlinePath.lineTo(x - 10, getHeight() - ((float) (mWorkout.getIntervals().get(0).startPower(mProfile)) / 100F * mFTP) * mPPW);

            for (WorkoutInterval interval : mWorkout.getIntervals()) {

                y1 = getHeight() - ((float) (interval.startPower(mProfile)) / 100F * mFTP) * mPPW;
                y2 = getHeight() - ((float) (interval.endPower(mProfile)) / 100F * mFTP) * mPPW;

                mWorkoutOutlinePath.lineTo(x, y1);

                mWorkoutOutlinePath.lineTo(x + (float) interval.duration * mPPS, y2);

                x += (float) interval.duration * mPPS;
            }

            mWorkoutOutlinePath.lineTo(width + 10, y2);
            mWorkoutOutlinePath.lineTo(width + 10, getHeight() + 5);
            mWorkoutOutlinePath.close();

            mWorkoutOutlinePen.setStyle(Paint.Style.FILL);
            mWorkoutOutlinePen.setShader(mGradient);
            canvas.drawPath(mWorkoutOutlinePath, mWorkoutOutlinePen);

            mWorkoutOutlinePen.setStyle(Paint.Style.STROKE);
            mWorkoutOutlinePen.setShader(null);
            canvas.drawPath(mWorkoutOutlinePath, mWorkoutOutlinePen);

            makePath(mPowerLineArray, mPowerLinePath, canvas, mPowerLinePen, mMaxWatts);
            makePath(mHeartRateLineArray, mHeartRateLinePath, canvas, mHeartRateLinePen, mMaxHeartRate);
            makePath(mCadenceLineArray, mCadenceLinePath, canvas, mCadenceLinePen, MAX_CADENCE);

            if (mCurrentTimeLineVisibility) {
                canvas.drawLines(mCurrentTimeLineCoordinates, mCurrentTimeLinePen);
            }
        }
    }

    public void updateScroller(float pctDone) {
        mCurrentTimeLineCoordinates[0] = pctDone * getWidth();
        mCurrentTimeLineCoordinates[2] = pctDone * getWidth();
    }

    public void updatePowerLineArray(ArrayList<PointF> list) {
        mPowerLineArray = list;
    }

    public void updateHeartRateLineArray(ArrayList<PointF> list) {
        mHeartRateLineArray = list;
    }

    public void updateCadenceLineArray(ArrayList<PointF> list) {
        mCadenceLineArray = list;
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

    public void setGradient(int outline, int base) {
        gradientColorBase = base;
        gradientColorOutline = outline;
        mWorkoutOutlinePen.setColor(ViewStyling.getColor(outline, getContext()));
        invalidate();
    }

    public void setGradientColorOutline(int gradientColorOutline) {
        this.gradientColorOutline = gradientColorOutline;
        invalidate();
    }

    public int getGradientColorBase() {
        return ViewStyling.getColor(gradientColorBase, getContext());
    }

    public void setGradientColorBase(int gradientColorBase) {
        this.gradientColorBase = gradientColorBase;
        invalidate();
    }

    public int getGradientColorOutline() {
        return ViewStyling.getColor(gradientColorOutline, getContext());
    }
}
