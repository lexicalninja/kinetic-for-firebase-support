package com.kinetic.fit.util;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kinetic.fit.R;

public class ViewStyling {

    public static int getColor(int attr, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static String timeToStringMS(double totalSeconds) {
        int mins = (int) Math.floor(totalSeconds / 60.0);
        totalSeconds -= mins * 60;
        int secs = (int) Math.floor(totalSeconds);
        return String.format("%02d:%02d", mins, secs);
    }

    public static String timeToStringMSF(double totalSeconds) {
        int mins = (int) Math.floor(totalSeconds / 60.0);
        totalSeconds -= mins * 60;
        int secs = (int) Math.floor(totalSeconds);
        totalSeconds -= secs;
        int fraction = (int) (totalSeconds * 10.0);
        return String.format("%02d:%02d.%01d", mins, secs, fraction);

    }

    public static String timeToStringHM(double totalSeconds) {
        int hours = (int) Math.floor(totalSeconds / 3600);
        totalSeconds -= hours * 3600;
        int mins = (int) Math.floor(totalSeconds / 60.0);
        return String.format("%02d:%02d", hours, mins);
    }

    public static String timeToStringHMS(double totalSeconds, boolean trimHours) {
        int hours = (int) Math.floor(totalSeconds / 3600);
        if (hours == 0 && trimHours) {
            return timeToStringMS(totalSeconds);
        }
        totalSeconds -= hours * 3600;
        int mins = (int) Math.floor(totalSeconds / 60.0);
        totalSeconds -= mins * 60;
        int secs = (int) Math.floor(totalSeconds);
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    public static String timeToStringHMSF(double totalSeconds) {
        int hours = (int) Math.floor(totalSeconds / 3600);
        totalSeconds -= hours * 3600;
        int mins = (int) Math.floor(totalSeconds / 60.0);
        totalSeconds -= mins * 60;
        int secs = (int) Math.floor(totalSeconds);
        totalSeconds -= secs;
        int fraction = (int) (totalSeconds * 10.0);
        return String.format("%02d:%02d:%02d.%01d", hours, mins, secs, fraction);
    }

    public static Toast getCustomToast(Context context, LayoutInflater layoutInflater, String toastMessage){
        ViewGroup rootGroup = (ViewGroup) ((Activity)context).getWindow().getDecorView().findViewById(R.id.toast_custom_layout_container);
        View toastLayout = layoutInflater.inflate(R.layout.toast_custom_layout, rootGroup);
        TextView textView = (TextView) toastLayout.findViewById(R.id.toast_custom_layout_text);
        textView.setText(toastMessage);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        return toast;
    }


}
