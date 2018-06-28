package com.kinetic.fit.ui.video.select;

import android.view.View;

import com.kinetic.fit.data.realm_objects.YouTubeVideo;
import com.nostra13.universalimageloader.core.ImageLoader;

import io.realm.RealmList;

/**
 * Created by Saxton on 8/2/17.
 */

public class YouTubeVideoAdapter extends VideoRecyclerAdapter {
    RealmList<YouTubeVideo> mVideos;

    public YouTubeVideoAdapter(RealmList<YouTubeVideo> data) {
        super();
        this.mVideos = data;
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        holder.videoTitle.setText(mVideos.get(position).getTitle());
        holder.duration.setText("");
        holder.authorName.setText(mVideos.get(position).getAuthor());
        ImageLoader.getInstance().cancelDisplayTask(holder.videoThumb);
        holder.videoThumb.setImageBitmap(null);
        ImageLoader.getInstance().displayImage(mVideos.get(position).getThumbUrl(), holder.videoThumb);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.youTubeVideoSelected(mVideos.get(holder.getAdapterPosition()));
            }
        });
    }

}
