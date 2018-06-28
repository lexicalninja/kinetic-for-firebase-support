package com.kinetic.fit.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.kinetic.fit.R;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

/**
 * Created by Saxton on 12/13/16.
 */

public class FitSystemNotifications {
    public FitSystemNotifications(Context context, String title, String longText, Class activityToOpen) {
        //        Build System Notification to persist instructions
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap())
                .setContentTitle("Kinetic Fit Notification (slide down for more)")
                .setContentText(title)
                .setDefaults(DEFAULT_ALL)
                .setPriority(PRIORITY_MAX)
                .setColor(ContextCompat.getColor(context, R.color.fit_light_green))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(longText));

        Intent resultIntent = new Intent(context, activityToOpen);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityToOpen);

//          Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//          notificationID allows you to update the notification later on.
        mNotificationManager.notify(1, notification.build());
    }
}
