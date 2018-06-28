package com.kinetic.fit.ui.workout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.widget.WorkoutGraphView;
import com.kinetic.fit.util.ViewStyling;

import io.realm.RealmList;

/**
 * Created by Saxton on 6/23/16.
 */
public class WorkoutSelectionRecyclerAdapter
        extends RecyclerView.Adapter<WorkoutSelectionRecyclerAdapter.ViewHolder> {

//    WorkoutSelectionFragment.WorkoutSelectionFragmentListener mListener;
    private RealmList<Workout> mWorkouts;
    Context mContext;

    interface SelectionListener{
        void workoutSelected(Workout workout);
    }

    private SelectionListener mListener;

    public WorkoutSelectionRecyclerAdapter(RealmList<Workout> list){
        mWorkouts = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView workoutIF;
        TextView workoutTSS;
        TextView duration;
        TextView workoutPower;
        WorkoutGraphView graph;
        LinearLayout layout;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.workout_listitem_text_name);
            workoutIF = (TextView) view.findViewById(R.id.workout_list_item_if);
            workoutTSS = (TextView) view.findViewById(R.id.workout_list_item_tss);
            duration = (TextView) view.findViewById(R.id.workout_list_item_duration);
            graph = (WorkoutGraphView) view.findViewById(R.id.workout_overview_graph);
            layout = (LinearLayout) view.findViewById(R.id.workout_selection_layout);
            workoutPower = (TextView) view.findViewById(R.id.workout_list_item_workout_watts);
        }
    }

    @Override
    public WorkoutSelectionRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        mListener = (SelectionListener) parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View workoutView = layoutInflater.inflate(R.layout.list_item_workout, parent, false);
        ViewHolder viewHolder = new ViewHolder(workoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WorkoutSelectionRecyclerAdapter.ViewHolder holder, final int position) {
        final Workout workout = mWorkouts.get(position);
        holder.title.setText(workout.getName());
        holder.workoutIF.setText(FitProperty.PowerIntensityFactor
                .getStringValue(workout.getIntensityFactor(), "%1.2f"));
        holder.duration.setText(ViewStyling.timeToStringHMS(workout.getDuration(), false));
        holder.workoutTSS.setText(FitProperty.PowerTSS.getStringValue(workout.getTrainingStressScore(), "%.0f"));
        holder.workoutPower.setText(String.format("%.2f kJ", workout.getKilojoules(Profile.current())));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mListener.workoutSelected(workout);
            }
        });
        holder.graph.drawEntireWorkoutPower(workout);
    }

    @Override
    public int getItemCount() {
        return mWorkouts.size();
    }


    public void updateWorkouts(RealmList<Workout> list){
        mWorkouts = list;
    }


    public RealmList<Workout> getCurrentWorkoutList(){return mWorkouts;}
}

