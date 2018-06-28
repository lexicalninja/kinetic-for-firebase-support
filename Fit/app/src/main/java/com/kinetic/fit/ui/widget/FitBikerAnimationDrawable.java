package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 10/25/17.
 */

public class FitBikerAnimationDrawable extends FitAnimationDrawable {

    Context mContext;
    int mSpeed;

    public FitBikerAnimationDrawable(Context context, int speed) {
        super();
        this.mContext = context;
        this.mSpeed = speed;
        setUpBikeAnimation();
    }

    void setUpBikeAnimation() {
        Drawable frame;
        int[] ids = {R.drawable.fit_rider_animation0, R.drawable.fit_rider_animation1, R.drawable.fit_rider_animation2, R.drawable.fit_rider_animation3, R.drawable.fit_rider_animation4, R.drawable.fit_rider_animation5, R.drawable.fit_rider_animation6, R.drawable.fit_rider_animation7, R.drawable.fit_rider_animation8, R.drawable.fit_rider_animation9, R.drawable.fit_rider_animation10, R.drawable.fit_rider_animation11, R.drawable.fit_rider_animation12, R.drawable.fit_rider_animation13, R.drawable.fit_rider_animation14, R.drawable.fit_rider_animation15, R.drawable.fit_rider_animation16, R.drawable.fit_rider_animation17, R.drawable.fit_rider_animation18, R.drawable.fit_rider_animation19, R.drawable.fit_rider_animation20, R.drawable.fit_rider_animation21, R.drawable.fit_rider_animation22, R.drawable.fit_rider_animation23, R.drawable.fit_rider_animation24, R.drawable.fit_rider_animation25, R.drawable.fit_rider_animation26, R.drawable.fit_rider_animation27, R.drawable.fit_rider_animation28, R.drawable.fit_rider_animation29, R.drawable.fit_rider_animation30, R.drawable.fit_rider_animation31, R.drawable.fit_rider_animation32, R.drawable.fit_rider_animation33, R.drawable.fit_rider_animation34, R.drawable.fit_rider_animation35, R.drawable.fit_rider_animation36, R.drawable.fit_rider_animation37, R.drawable.fit_rider_animation38, R.drawable.fit_rider_animation39, R.drawable.fit_rider_animation40};
        for (int i : ids) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), i);
            frame = new BitmapDrawable(Resources.getSystem(), bitmap);
            addFrame(frame, mSpeed);
        }
        setOneShot(false);
    }
}
