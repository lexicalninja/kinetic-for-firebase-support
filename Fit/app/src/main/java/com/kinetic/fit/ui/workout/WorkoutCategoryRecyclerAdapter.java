package com.kinetic.fit.ui.workout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.Category;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.util.ViewStyling;
import com.nostra13.universalimageloader.core.ImageLoader;

import io.realm.RealmResults;

public class WorkoutCategoryRecyclerAdapter
        extends RecyclerView.Adapter<WorkoutCategoryRecyclerAdapter.ViewHolder> {

    RealmResults<Category> mCategories;
    Context mContext;

    public WorkoutCategoryRecyclerAdapter(RealmResults<Category> data) {
        mCategories = data;
    }

    @Override
    public void onBindViewHolder(WorkoutCategoryRecyclerAdapter.ViewHolder holder, final int position) {
        if (position > 1) {
            Category category = mCategories.get(position - 2);

            holder.categoryName.setText(category.getName());
            holder.categoryDescription.setText(category.getShortDescription());
            holder.categoryCount.setText(String.valueOf(category.getCountedWorkouts()));
            ImageLoader.getInstance().cancelDisplayTask(holder.categoryThumb);
            holder.categoryThumb.setColorFilter(null);
            holder.categoryThumb.setImageBitmap(null);
            if (category.getImageUrl() != null) {
                ImageLoader.getInstance().displayImage(category.getImageUrl(), holder.categoryThumb);
            }
        } else if (position == 0) {
            holder.categoryName.setText("Free Ride");
            holder.categoryDescription.setText("No laps, no power or cadence targets, no limits.");
            holder.categoryThumb.setImageResource(R.mipmap.category_freeride);
            holder.categoryCount.setText("---");
        } else {
            holder.categoryName.setText("Favorites");
            holder.categoryDescription.setText("Recent, starred, and imported workouts. All in one place!");
            holder.categoryThumb.setImageResource(R.drawable.material_icon_star);
            holder.categoryThumb.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
            holder.categoryCount.setText(String.valueOf(Profile.current().getFavoriteWorkouts().size()));
        }

        holder.categoryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0) {
                   OverviewActivity_.intent(mContext)
                            .start();
                } else if (position == 1) {
                    SelectionActivity_.intent(mContext)
                            .extra(SelectionActivity.FAVORITES_BOOLEAN_EXTRA, true)
                            .start();
                } else {
                    SelectionActivity_.intent(mContext)
                            .extra(SelectionActivity.CATEGORY_NAME, mCategories.get(position -2).getName())
                            .start();
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View categoryView = layoutInflater.inflate(R.layout.list_item_category, parent, false);
        ViewHolder viewHolder = new ViewHolder(categoryView);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mCategories.size() + 2;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryCount;
        TextView categoryName;
        TextView categoryDescription;
        ImageView categoryThumb;
        LinearLayout categoryLayout;

        public ViewHolder(View view) {
            super(view);
            categoryCount = (TextView) view.findViewById(R.id.categoryCount);
            categoryName = (TextView) view.findViewById(R.id.categoryName);
            categoryDescription = (TextView) view.findViewById(R.id.categoryDescription);
            categoryThumb = (ImageView) view.findViewById(R.id.categoryThumb);
            categoryLayout = (LinearLayout) view.findViewById(R.id.workout_category_layout);
        }

    }
}
