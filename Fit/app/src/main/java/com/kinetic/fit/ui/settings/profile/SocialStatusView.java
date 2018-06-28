package com.kinetic.fit.ui.settings.profile;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.kinetic.fit.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import info.hoang8f.android.segmented.SegmentedGroup;

@EViewGroup
public class SocialStatusView extends LinearLayout {

    public interface SocialStatusViewListener {
        void toggleConnection(SocialStatusView statusView);

        void toggleAutoShare(SocialStatusView statusView);

        void changeVisibility(SocialStatusView statusView, int visIndex);
    }

    private SocialStatusViewListener mListener;

    public SocialStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setListener(SocialStatusViewListener listener) {
        mListener = listener;
    }

    @ViewById
    TextView title;

    @ViewById
    ImageView socialIcon;

    @ViewById(R.id.visibility1)
    RadioButton friendsShare;

    @ViewById(R.id.visibility2)
    RadioButton publicShare;

    @ViewById(R.id.visibility0)
    RadioButton privateShare;

    @ViewById
    ImageView connectButton;

    @ViewById
    ImageView autoShare;

    @ViewById
    SegmentedGroup visibility;

    @Click(R.id.connectButton)
    void connectButtonHandler() {
        if (mListener != null) {
            mListener.toggleConnection(this);
        }
    }

    @Click(R.id.autoShare)
    void autoShareButtonHandler() {
        if (mListener != null) {
            mListener.toggleAutoShare(this);
        }
    }

    @Click(R.id.visibility0)
    void visibility0ButtonHandler() {
        if (mListener != null) {
            mListener.changeVisibility(this, 0);
        }
    }

    @Click(R.id.visibility1)
    void visibility1ButtonHandler() {
        if (mListener != null) {
            mListener.changeVisibility(this, 1);
        }
    }

    @Click(R.id.visibility2)
    void visibility2ButtonHandler() {
        if (mListener != null) {
            mListener.changeVisibility(this, 2);
        }
    }

    void setAutoShare(boolean sharing) {
        if (sharing) {
            autoShare.setImageResource(R.mipmap.button_toggle_green_checked);
        } else {
            autoShare.setImageResource(R.mipmap.button_toggle_blue_unchecked);
        }
    }

    void setConnected(boolean connected) {
        if (connected) {
            connectButton.setImageResource(R.mipmap.button_connected);

            socialIcon.setColorFilter(getResources().getColor(R.color.fit_dark_green), PorterDuff.Mode.SRC_ATOP);

            visibility.setTintColor(getResources().getColor(R.color.fit_dark_green), getResources().getColor(R.color.fit_dark_bg0));
            visibility.setClickable(true);
            visibility.setEnabled(true);

            autoShare.setClickable(true);
        } else {
            connectButton.setImageResource(R.mipmap.button_disconnected);

            socialIcon.setColorFilter(getResources().getColor(R.color.fit_dark_gray), PorterDuff.Mode.SRC_ATOP);

            visibility.setTintColor(getResources().getColor(R.color.fit_dark_gray));
            visibility.setEnabled(false);
            visibility.setClickable(false);
            visibility.clearCheck();

            autoShare.setClickable(false);
            autoShare.setImageResource(R.mipmap.button_toggle_blue_unchecked);
        }
    }

    void setSocialVisibility(int visIndex) {
        if (visIndex == 0) {
            visibility.check(R.id.visibility0);
        } else if (visIndex == 1) {
            visibility.check(R.id.visibility1);
        } else if (visIndex == 2)
            visibility.check(R.id.visibility2);
    }

    public void removeFriendShare(){
        visibility.removeView(friendsShare);
        visibility.updateBackground();
    }

    public void removePublicShare(){
        visibility.removeView(publicShare);
        visibility.updateBackground();
    }

    public void removePrivateShare(){
        visibility.removeView(privateShare);
        visibility.updateBackground();
    }

    public void removeAutoShare(){
        autoShare.setVisibility(INVISIBLE);
    }


}
