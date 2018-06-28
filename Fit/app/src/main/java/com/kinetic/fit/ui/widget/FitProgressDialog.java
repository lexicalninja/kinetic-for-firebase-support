package com.kinetic.fit.ui.widget;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Saxton on 12/19/16.
 */

public class FitProgressDialog extends ProgressDialog {
    public FitProgressDialog(Context context) {
        super(context);
    }

    public static FitProgressDialog show(Context context, CharSequence title, CharSequence message) {
        FitProgressDialog fitProgressDialog = new FitProgressDialog(context);
        fitProgressDialog.setTitle(title);
        fitProgressDialog.setMessage(message);
        fitProgressDialog.setIndeterminate(true);
        fitProgressDialog.setIndeterminateDrawable(new FitBikerAnimationDrawable(context, 25));
        fitProgressDialog.show();
        return fitProgressDialog;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
