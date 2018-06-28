package com.kinetic.fit.ui.video.select;

import android.view.View;

import com.kinetic.fit.data.realm_objects.Video;
import com.kinetic.fit.util.ViewStyling;
import com.nostra13.universalimageloader.core.ImageLoader;

import io.realm.RealmList;

/**
 * Created by Saxton on 8/7/17.
 */

public class StreamingVideoAdapter extends VideoRecyclerAdapter {
    RealmList<Video> mVideos;

    public StreamingVideoAdapter(RealmList<Video> videos){
        super();
        this.mVideos = videos;
    }
    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        holder.videoTitle.setText(mVideos.get(position).getName());
        holder.authorName.setText(mVideos.get(position).getAuthor());
        holder.duration.setText(ViewStyling.timeToStringMS(mVideos.get(position).getDuration()));
        ImageLoader.getInstance().cancelDisplayTask(holder.videoThumb);
        holder.videoThumb.setImageBitmap(null);
        ImageLoader.getInstance().displayImage(mVideos.get(position).getThumbUrl(), holder.videoThumb);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.streamingVideoSelected(mVideos.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }
}
