package com.kinetic.fit.ui.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.kinetic.fit.R;
import com.kinetic.fit.util.ViewStyling;

/**
 * Created by Saxton on 7/17/17.
 */


public class SpinnerLoaderView extends View {
    private final Paint mPaint = new Paint();
    private final RectF mArcBounds = new RectF();

    private static final long ANIMATION_DURATION = 800L;
    private static final int ARC_MAX_DEGREES = 200;
    private static final int ARC_OFFSET_DEGREES = -90; //used to offset start pos. -90 is top of view
    private static final int DEGREES_IN_CIRCLE = 360;

    private ValueAnimator mAnimator;
    private DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    private float mArcStart;
    private float mArcSize;

    public SpinnerLoaderView(Context context) {
        super(context);
        init();
    }

    public SpinnerLoaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpinnerLoaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpinnerLoaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint.setColor(ViewStyling.getColor(R.attr.colorFitPrimary, getContext()));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final int smallestSide = Math.min(w, h);
        mPaint.setStrokeWidth(smallestSide * .15f);
        final float paintPadding = mPaint.getStrokeWidth() / 2f;
        mArcBounds.set(paintPadding + getPaddingLeft(), paintPadding + getPaddingTop(),
                w - paintPadding - getPaddingRight(), h - paintPadding - getPaddingBottom());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                mArcStart = calculateArcStart(progress);
                mArcSize = calculateArcSize(progress);
                invalidate();
            }
        });
        mAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimator.cancel();
        mAnimator.removeAllUpdateListeners();
        mAnimator = null;
    }

    private float calculateArcSize(float animationProgress){
        final double progress = Math.sin(animationProgress * Math.PI);
        return (float) progress * -ARC_MAX_DEGREES;
    }

    private float calculateArcStart(float animationProgress){
        final float progress = mDecelerateInterpolator.getInterpolation(animationProgress);
        return ARC_OFFSET_DEGREES + progress * DEGREES_IN_CIRCLE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mArcBounds, mArcStart, mArcSize, false, mPaint);
    }
}
