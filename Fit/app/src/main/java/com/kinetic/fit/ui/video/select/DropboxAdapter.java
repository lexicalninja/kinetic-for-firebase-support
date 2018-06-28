package com.kinetic.fit.ui.video.select;

import android.view.View;

import com.kinetic.fit.R;
import com.kinetic.fit.ui.video.VideoControllerItem;
import com.kinetic.fit.util.ViewStyling;

import java.util.ArrayList;

/**
 * Created by Saxton on 8/7/17.
 * /
 */

class DropboxAdapter extends VideoRecyclerAdapter {

    private ArrayList<VideoControllerItem> mVideos = new ArrayList<>();

    DropboxAdapter(ArrayList<VideoControllerItem> mVideos) {
        this.mVideos = mVideos;
    }

    @Override
    public void onBindViewHolder(final VideoViewHolder holder, int position) {
        holder.videoTitle.setText(mVideos.get(position).title);
        holder.duration.setText(ViewStyling.timeToStringHMS((double) mVideos.get(position).duration, false));
        holder.authorName.setText(R.string.mine);
        holder.videoThumb.setImageResource(R.drawable.ic_dropbox);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.localVideoSelected(mVideos.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

}


