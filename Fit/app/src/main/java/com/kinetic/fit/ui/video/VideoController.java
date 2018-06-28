package com.kinetic.fit.ui.video;


import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.support.v7.app.MediaRouteChooserDialogFragment;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.kinetic.fit.cast.FitCastService;
import com.kinetic.fit.cast.FitCastService_;
import com.kinetic.fit.ui.FitActivity;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean(scope = EBean.Scope.Singleton)
public class VideoController {

    private static final String TAG = "VideoController";


//    enum VideoState {
//        case Paused
//        case Playing
//    }

//    struct Video {
//        let url: NSURL
//        let title: String
//        var suggestedStream: Bool = false
//        var hidePopups: Bool = false
//        var workoutSync: Bool = true
//        let player: AVPlayer
//
//        init(url: NSURL, title: String) {
//            self.url = url
//            self.title = title
//
//            player = AVPlayer(playerItem: AVPlayerItem(URL: url))
//            player.allowsExternalPlayback = false
//            player.actionAtItemEnd = .None
////            player.prerollAtRate(1) { finished in
////
////            }
//        }
//    }


    VideoControllerItem mVideo;

    private WifiManager.WifiLock mWiiLock;

    public void setVideo(VideoControllerItem video) {
        mVideo = video;
        if (mVideo != null && mVideo.uri != null) {
            // TODO: popup -> "Validating Subscription Status..."
            fetchStreamingCookies();
        }
    }

    @Background
    protected void fetchStreamingCookies() {
        if (mVideo != null) {
            mVideo.fetchStreamingCookies();
        }
    }

    public VideoControllerItem getVideo() {
        return mVideo;
    }

    public boolean videoIsNull() {
        return (mVideo == null);
    }

    @RootContext
    Context context;

    @AfterInject
    void afterInjection() {
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    private static final String CAST_APP_ID = "5F963664";
    protected MediaRouter mMediaRouter;
    protected MediaRouteSelector mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(CAST_APP_ID)).build();
    protected final MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback() {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteSelected");

            wifiLock();

            CastDevice castDevice = CastDevice.getFromBundle(info.getExtras());
            if (castDevice != null) {
                startCastService(castDevice);
            }
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected");

            FitCastService.stopService();
        }
    };

    public void selectChromeCast(FitActivity activity) {
        int apiAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (apiAvailability != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(activity, apiAvailability, 0);
            return;
        }

        if (FitCastService.getInstance() == null) {
            MediaRouteChooserDialogFragment fragment = MediaRouteDialogFactory.getDefault().onCreateChooserDialogFragment();
            fragment.setRouteSelector(mMediaRouteSelector);
            fragment.show(activity.getSupportFragmentManager(), "android.support.v7.mediarouter:MediaRouteChooserDialogFragment");
        } else {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            String displayName = null;
            try {
                displayName = CastDevice.getFromBundle(mMediaRouter.getSelectedRoute().getExtras()).getFriendlyName();
            } catch (NullPointerException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            if (displayName != null) {
                builder.setTitle("Disconnect from " + displayName + "?");
            } else {
                builder.setTitle("Disconnect from Chromecast?");
            }
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FitCastService.stopService();
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {

                }
            });
            builder.show();
        }
    }

    private void startCastService(CastDevice castDevice) {
        Notification notification = new Notification.Builder(context).setContentInfo("Chromecast").build();
        FitCastService.NotificationSettings settings = new FitCastService.NotificationSettings.Builder().setNotification(notification).build();

        FitCastService_.startService(context, FitCastService_.class, CAST_APP_ID, castDevice, settings, new CastRemoteDisplayLocalService.Callbacks() {
            @Override
            public void onServiceCreated(CastRemoteDisplayLocalService service) {
                Log.d(TAG, "onServiceCreated");
            }

            @Override
            public void onRemoteDisplaySessionStarted(CastRemoteDisplayLocalService service) {
                Log.d(TAG, "onServiceStarted");
            }

            @Override
            public void onRemoteDisplaySessionError(Status errorReason) {
                int code = errorReason.getStatusCode();
                Log.d(TAG, "onServiceError: " + errorReason.getStatusCode());

                // TODO: Service take care of itself???
                FitCastService.stopService();
//                mCastDevice = null;
                //CastRemoteDisplayActivity.this.finish();
            }

            @Override
            public void onRemoteDisplaySessionEnded(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

            }
        });
    }

    private void wifiLock() {
        Log.d(TAG, "wifiLock");
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWiiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock");
        mWiiLock.acquire();
    }

    public void wifiLockRelease() {
        Log.d(TAG, "wifiLockRelease");
        if (mWiiLock != null && mWiiLock.isHeld()) {
            mWiiLock.release();
        }
    }

    public String getVideoTitle(){
        if(mVideo == null){
            return "None";
        } else {
            return mVideo.getKPITitle();
        }
    }
}
