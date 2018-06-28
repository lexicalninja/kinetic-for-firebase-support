package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 5/4/17.
 */

public class FitBikerAnimationScene extends RelativeLayout {

    private static final int GRASS_ANIMATION_BASELINE = 75;
    private static final int HILLS_ANIMATION_BASELINE = 2500;
    private static final int TREES_ANIMATION_BASELINE = 3500;
    private static final int MOUNTAINS_ANIMATION_BASELINE = 5500;
    private static final int RIDER_ANIMATION_BASELINE = 25;

    TranslateAnimation grassAnimation;
    TranslateAnimation hillsAnimation;
    TranslateAnimation treesAnimation;
    TranslateAnimation mountainsAnimation;
    FitAnimationDrawable bikeAnimation;

    View grass;
    View hills;
    View trees;
    View mountains;
    ImageView biker;

    Drawable grassDrawable;
    Drawable treesDrawable;
    Drawable hillsDrawable;
    Drawable mountainsDrawable;


    public FitBikerAnimationScene(Context context) {
        super(context);
        init();
    }

    public FitBikerAnimationScene(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FitBikerAnimationScene(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FitBikerAnimationScene(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        View.inflate(getContext(), R.layout.widget_rider_animation, this);
        grass = findViewById(R.id.grass);
        hills = findViewById(R.id.hills);
        trees = findViewById(R.id.trees);
        mountains = findViewById(R.id.mountains);
        biker = findViewById(R.id.biker);
        grassDrawable = ContextCompat.getDrawable(getContext(), R.drawable.onboarding_scene_grass);
        hillsDrawable = ContextCompat.getDrawable(getContext(), R.drawable.onboarding_scene_hills);
        treesDrawable = ContextCompat.getDrawable(getContext(), R.drawable.onboarding_scene_trees);
        mountainsDrawable = ContextCompat.getDrawable(getContext(), R.drawable.onboarding_scene_mountains);
        setNewAnimation(grass, grassDrawable, grassAnimation, GRASS_ANIMATION_BASELINE);
        setNewAnimation(hills, hillsDrawable, hillsAnimation, HILLS_ANIMATION_BASELINE);
        setNewAnimation(trees, treesDrawable, treesAnimation, TREES_ANIMATION_BASELINE);
        setNewAnimation(mountains, mountainsDrawable, mountainsAnimation, MOUNTAINS_ANIMATION_BASELINE);
        bikeAnimation = new FitBikerAnimationDrawable(getContext(), RIDER_ANIMATION_BASELINE);
        biker.setBackground(bikeAnimation);
        bikeAnimation.start();
    }

    void setNewAnimation(View view, Drawable image, TranslateAnimation animation, int duration) {
        final int imageWidth = image.getIntrinsicWidth();
        final int animatedViewWidth = getResourceWidth(imageWidth);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = animatedViewWidth;
        view.setLayoutParams(layoutParams);
        animation = new TranslateAnimation(0, -imageWidth, 0, 0);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(duration);
        view.startAnimation(animation);
    }

    int getResourceWidth(final int imageWidth) {
        final int screenWidth = getScreenDimensions(getContext()).x;
        int animatedViewWidth = 0;
        while (animatedViewWidth < screenWidth) {
            animatedViewWidth += imageWidth;
        }
        animatedViewWidth += 2 * imageWidth;
        return animatedViewWidth;
    }

    public static Point getScreenDimensions(Context context) {
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        return new Point(width, height);
    }

    public void setSpeedKPH(double speedKPH) {
        adjustAnimationSpeed(speedKPH);
    }

    private void adjustAnimationSpeed(double speedKPH) {
        double offset;
        speedKPH = Math.min(speedKPH, 40.0);
        if (speedKPH == 0) {
            bikeAnimation.stop();
            offset = 0.0;
        } else {
            if (!bikeAnimation.isRunning()) {
                bikeAnimation.start();
            }
            offset = Math.max(Math.abs(((speedKPH - 40.0) / 40.0)) * 5, 1);
            bikeAnimation.setDuration((int) (offset * (double) RIDER_ANIMATION_BASELINE));
        }
        grass.getAnimation().setDuration((long) (offset * (double) GRASS_ANIMATION_BASELINE));
        hills.getAnimation().setDuration((long) (offset * (double) HILLS_ANIMATION_BASELINE));
        trees.getAnimation().setDuration((long) (offset * (double) TREES_ANIMATION_BASELINE));
        mountains.getAnimation().setDuration((long) (offset * (double) MOUNTAINS_ANIMATION_BASELINE));
    }
}