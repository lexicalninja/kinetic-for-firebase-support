package com.kinetic.fit.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kinetic.fit.R;

/**
 * Created by Saxton on 3/9/17.
 */

public class FitAlertDialog extends AlertDialog {

    public FitAlertDialog(Context context) {
        super(context);
    }

    public static FitAlertDialog show(Context context, CharSequence title, CharSequence message, @Nullable CharSequence negativeButtonText, CharSequence positiveButtonText, @Nullable View.OnClickListener negativeClickListener, View.OnClickListener positiveClickListener, boolean hasDismissButton) {
        View dialogView = ((Activity) context).getLayoutInflater().inflate(R.layout.fit_alert_dialog, null);
        final FitAlertDialog fitAlertDialog = new FitAlertDialog(context);
        fitAlertDialog.setView(dialogView);
        TextView textView = (TextView) dialogView.findViewById(R.id.alert_title);
        textView.setText(title);
        textView = (TextView) dialogView.findViewById(R.id.alert_message);
        textView.setText(message);
        TextView negativeButton = (TextView) dialogView.findViewById(R.id.alert_negative_button);
        TextView positiveButton = (TextView) dialogView.findViewById(R.id.alert_positive_button);
        if (negativeButtonText != null && !hasDismissButton) {
            negativeButton.setText(negativeButtonText);
            negativeButton.setOnClickListener(negativeClickListener);
        } else if(hasDismissButton && negativeButtonText != null){
            negativeButton.setText(negativeButtonText);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fitAlertDialog.dismiss();
                }
            });
        }
        else {
            negativeButton.setVisibility(View.GONE);
        }
        positiveButton.setText(positiveButtonText);
        positiveButton.setOnClickListener(positiveClickListener);
        fitAlertDialog.show();
        return fitAlertDialog;
    }

    private View.OnClickListener dismissListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FitAlertDialog.this.dismiss();
        }
    };

    public View.OnClickListener getDismissListener() {
        return dismissListener;
    }
}
