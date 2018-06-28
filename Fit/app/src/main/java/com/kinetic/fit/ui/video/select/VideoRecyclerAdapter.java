package com.kinetic.fit.ui.video.select;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Video;
import com.kinetic.fit.data.realm_objects.YouTubeVideo;
import com.kinetic.fit.ui.video.VideoControllerItem;

/**
 * Created by Saxton on 8/2/17.
 * ll
 */

abstract class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.VideoViewHolder> {

    interface VideoSelectListener{
        void youTubeVideoSelected(YouTubeVideo video);
        void streamingVideoSelected(Video video);
        void localVideoSelected(VideoControllerItem vcv);
        void dropboxVideoSelected(VideoControllerItem vcv);
    }

    VideoSelectListener mListener;

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.list_item_video, parent, false);
        return new VideoViewHolder(v);
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView videoTitle;
        TextView authorName;
        TextView duration;
        ImageView videoThumb;
        LinearLayout layout;

        VideoViewHolder(View view) {
            super(view);
            videoTitle = (TextView) view.findViewById(R.id.videoTitle);
            authorName = (TextView) view.findViewById(R.id.authorName);
            duration = (TextView) view.findViewById(R.id.duration);
            videoThumb = (ImageView) view.findViewById(R.id.videoThumb);
            layout = (LinearLayout) view.findViewById(R.id.layout);
        }
    }

    void setVideoSelectListener(VideoSelectListener listener){
        this.mListener = listener;
    }
}
