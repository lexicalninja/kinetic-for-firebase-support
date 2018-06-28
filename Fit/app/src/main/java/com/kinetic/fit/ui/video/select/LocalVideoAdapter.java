package com.kinetic.fit.ui.video.select;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.kinetic.fit.R;
import com.kinetic.fit.ui.video.VideoControllerItem;
import com.kinetic.fit.util.ViewStyling;

import java.util.ArrayList;

/**
 * Created by Saxton on 8/7/17.
 * //
 */

class LocalVideoAdapter extends VideoRecyclerAdapter {
    private Cursor cursor;
    private ContentResolver contentResolver;
    private ArrayList<VideoControllerItem> mVideos = new ArrayList<>();

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        holder.videoTitle.setText(mVideos.get(position).title);
        holder.duration.setText(ViewStyling.timeToStringHMS((double) mVideos.get(position).duration, false));
        holder.authorName.setText(R.string.mine);
        holder.videoThumb.setImageBitmap(mVideos.get(position).localThumb);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.localVideoSelected(mVideos.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    void discoverVideos(ContentResolver cr) {
        mVideos.clear();
        this.contentResolver = cr;
        this.cursor = getVideoCursor(cr);
        getVideos();
    }

    private Bitmap getBitmap(int videoId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        return MediaStore.Video.Thumbnails.getThumbnail(contentResolver, videoId,
                MediaStore.Video.Thumbnails.MINI_KIND, options);
    }

    private Cursor getVideoCursor(ContentResolver cr) {
        String[] proj = {"*"};
        return cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
                null, null, null);
    }

    private void getVideos() {
        try {
            cursor.moveToFirst();
            do {
                VideoControllerItem vcv = new VideoControllerItem();
                vcv.uri = Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/"
                        + cursor.getInt(cursor.getColumnIndexOrThrow("_ID")));
                vcv.hidePopups = false;
                vcv.title = cursor.getString(cursor.getColumnIndexOrThrow("TITLE"));
                vcv.workoutSync = false;
                vcv.localThumb = getBitmap(cursor.getInt(cursor.getColumnIndexOrThrow("_ID")));
                vcv.duration = cursor.getLong(cursor.getColumnIndexOrThrow("DURATION"));
                mVideos.add(vcv);
            } while (cursor.moveToNext());
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
