package com.kinetic.fit.ui.trainingplans;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.TrainingPlan;

import java.util.List;

/**
 * Created by Saxton on 10/20/16.
 */

public class TrainingPlanRecyclerAdapter extends android.support.v7.widget.RecyclerView.Adapter<TrainingPlanRecyclerAdapter.ViewHolder> {

    private static final int HEADER_VIEW = 0;
    private static final int PLAN_VIEW = 1;


    private List<TrainingPlan> mTrainingPlans;
    private Context mContext;

    public TrainingPlanRecyclerAdapter(List<TrainingPlan> list) {
        mTrainingPlans = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

    private class TrainingPlanViewHolder extends ViewHolder {
        TextView trainingPlanTitle;
        TextView trainingPlanAuthor;
        TextView trainingPlanDuration;
        ImageView trainingPlanCategoryImage;
        ImageView trainingPlanExperienceLevelImage;
        ImageView trainingPlanVolumeImage;

        TrainingPlanViewHolder(View view) {
            super(view);
            trainingPlanTitle = (TextView) view.findViewById(R.id.training_plan_card_overview_plan_name);
            trainingPlanAuthor = (TextView) view.findViewById(R.id.training_plan_overview_creator_name);
            trainingPlanDuration = (TextView) view.findViewById(R.id.training_plan_overview_duration);
            trainingPlanCategoryImage = (ImageView) view.findViewById(R.id.training_plan_overview_category_icon);
            trainingPlanExperienceLevelImage = (ImageView) view.findViewById(R.id.training_plan_overview_difficulty_icon);
            trainingPlanVolumeImage = (ImageView) view.findViewById(R.id.training_plan_overview_volume_icon);
        }
    }

    private class HeaderViewHolder extends ViewHolder {
        ImageView categoryImage;
        TextView categoryName;

        HeaderViewHolder(View view) {
            super(view);
            categoryImage = (ImageView) view.findViewById(R.id.training_plan_header_image);
            categoryName = (TextView) view.findViewById(R.id.training_plan_header_category_name);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (viewType == PLAN_VIEW) {
            View trainingPlanView = layoutInflater.inflate(R.layout.card_view_training_plan_overview, parent, false);
            final ViewHolder viewHolder = new TrainingPlanViewHolder(trainingPlanView);
            trainingPlanView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = viewHolder.getAdapterPosition();
                    Intent intent = new Intent(mContext, TrainingPlanOverViewActivity_.class);
                    intent.putExtra("planId", mTrainingPlans.get(position).getObjectId());
                    mContext.startActivity(intent);
                }
            });
            return viewHolder;
        } else {
            View headerView = layoutInflater.inflate(R.layout.recyler_view_header_training_plan_category, parent, false);
            ViewHolder header = new HeaderViewHolder(headerView);
            return header;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrainingPlan tp = mTrainingPlans.get(position);
        if (!tp.getIsHeader()) {
            ((TrainingPlanViewHolder) holder).trainingPlanTitle.setText(tp.getPlanName());
            ((TrainingPlanViewHolder) holder).trainingPlanTitle.setSelected(true);
            ((TrainingPlanViewHolder) holder).trainingPlanAuthor.setText(tp.getAuthor());
            ((TrainingPlanViewHolder) holder).trainingPlanDuration
                    .setText(mContext.getString(R.string.training_plan_duration_string_formatter,
                            tp.getPlanLengthInWeeks()));
            ((TrainingPlanViewHolder) holder).trainingPlanCategoryImage.setImageResource(tp.getCategoryIconResourceId());
            ((TrainingPlanViewHolder) holder).trainingPlanVolumeImage.setImageResource(tp.getPlanVolumeIconId());
            ((TrainingPlanViewHolder) holder).trainingPlanExperienceLevelImage.setImageResource(tp.getExperienceLevelIconId());
        } else {
            if (tp.getImageResourceId() != 0) {
                ((HeaderViewHolder) holder).categoryImage.setImageResource(tp.getImageResourceId());
                ((HeaderViewHolder) holder).categoryName.setText(tp.getCategoryName().toUpperCase());
            } else {
                ((HeaderViewHolder) holder).categoryImage.setVisibility(View.GONE);
                ((HeaderViewHolder) holder).categoryName.setText(tp.getCategoryName().toUpperCase());
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mTrainingPlans.get(position).getIsHeader()) {
            return HEADER_VIEW;
        } else {
            return PLAN_VIEW;
        }
    }

    @Override
    public int getItemCount() {
        return mTrainingPlans.size();
    }

    public void setTrainingPlans(List<TrainingPlan> mTrainingPlans) {
        this.mTrainingPlans = mTrainingPlans;
    }

}
