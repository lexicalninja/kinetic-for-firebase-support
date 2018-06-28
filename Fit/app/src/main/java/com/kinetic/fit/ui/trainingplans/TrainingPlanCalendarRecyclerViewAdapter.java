package com.kinetic.fit.ui.trainingplans;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.TrainingPlanDay;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.ui.widget.CalendarInformationUpdateView;
import com.kinetic.fit.ui.widget.WorkoutGraphView;
import com.kinetic.fit.ui.workout.OverviewActivity_;
import com.kinetic.fit.util.ViewStyling;

import java.util.ArrayList;

/**
 * Created by Saxton on 3/14/17.
 */

public class TrainingPlanCalendarRecyclerViewAdapter extends RecyclerView.Adapter<TrainingPlanCalendarRecyclerViewAdapter.CalendarViewHolder> {

    ArrayList<TrainingPlanDay> trainingPlanDays;
    int currentDay;
    private static final int WORKOUT_VIEW = 1;
    private static final int REST_DAY_VIEW = 2;
    private static final int NO_SCHEDULE_VIEW = 3;

    public TrainingPlanCalendarRecyclerViewAdapter(ArrayList<TrainingPlanDay> daysList, int currentDay) {
        this.trainingPlanDays = daysList;
        this.currentDay = currentDay;
    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder implements CalendarInformationUpdateView.CalendarInformationUpdateViewListener {
        TextView dayNumber;
        TextView dayTitle;
        TextView dayInfo;
        WorkoutGraphView graph;
        Context mContext;
        CalendarInformationUpdateView.SortGroups currentView;


        public CalendarViewHolder(View view) {
            super(view);
            mContext = view.getContext();
            dayNumber = (TextView) view.findViewById(R.id.calendar_day_number);
            dayTitle = (TextView) view.findViewById(R.id.calendar_day_title);
            dayInfo = (TextView) view.findViewById(R.id.calendar_info);
            graph = (WorkoutGraphView) view.findViewById(R.id.calendar_workout_graph);
        }

        @Override
        public void infoTypeSelected(CalendarInformationUpdateView.SortGroups infoType) {
            if (infoType == CalendarInformationUpdateView.SortGroups.DURATION) {
                double duration = trainingPlanDays.get(getAdapterPosition()).getWorkout().getDuration();
                int hours = (int) duration / 3600;
                int mins = (int) duration % 3600 / 60;
                dayInfo.setText(mContext.getString(R.string.calendar_duration_formatter, hours, mins));
            } else if (infoType == CalendarInformationUpdateView.SortGroups.IF) {
                dayInfo.setText(mContext.getString(R.string.calendar_if_formatter, trainingPlanDays.get(getAdapterPosition()).getWorkout().getIntensityFactor()));
            } else if (infoType == CalendarInformationUpdateView.SortGroups.TSS) {
                dayInfo.setText(mContext.getString(R.string.calendar_tss_formatter, (int) trainingPlanDays.get(getAdapterPosition()).getWorkout().getTrainingStressScore()));
            }
            currentView = infoType;
        }
    }

    @Override
    public int getItemCount() {
        return trainingPlanDays.size();
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View calendarView = inflater.inflate(R.layout.list_item_training_plan_calendar, parent, false);
        final CalendarViewHolder vh = new CalendarViewHolder(calendarView);
        switch (viewType) {
            case WORKOUT_VIEW: {
                calendarView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = vh.getAdapterPosition();
                        OverviewActivity_.intent(context)
                                .extra("workoutId", trainingPlanDays.get(position).getWorkout().getObjectId())
                                .start();
                    }
                });
                ((TrainingPlanCalendarActivity) context).calendarInformationUpdateView.setListener(vh);
                break;
            }
            case NO_SCHEDULE_VIEW: {
//                vh.dayTitle.setText(context.getString(R.string.training_plan_day_no_workout_text));
            }
            case REST_DAY_VIEW: {
                vh.dayInfo.setVisibility(View.INVISIBLE);
                vh.graph.setVisibility(View.INVISIBLE);
                break;
            }
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        if (trainingPlanDays.get(position) != null) {
            holder.dayTitle.setText(trainingPlanDays.get(position).getName());
            if (trainingPlanDays.get(position).getWorkout() != null) {
                Workout w = trainingPlanDays.get(position).getWorkout();
                Context c = holder.mContext;
                if (((TrainingPlanCalendarActivity) c).getCurrentInfo() == CalendarInformationUpdateView.SortGroups.DURATION) {
                    holder.dayInfo.setText(c.getString(R.string.calendar_duration_formatter,
                            (int) w.getDuration() / 3600,
                            (int) w.getDuration() % 3600 / 60));
                } else if (((TrainingPlanCalendarActivity) c).getCurrentInfo() == CalendarInformationUpdateView.SortGroups.IF) {
                    holder.dayInfo.setText(c.getString(R.string.calendar_if_formatter, w.getIntensityFactor()));
                } else {
                    holder.dayInfo.setText(c.getString(R.string.calendar_tss_formatter, (int) w.getTrainingStressScore()));
                }
                holder.graph.drawEntireWorkoutPower(w);
                holder.graph.setCurrentTimeLineVisibility(false);
                if(position + 1 >= currentDay) {
                    holder.graph.setGradientColorOutline(R.attr.colorFitPrimary);
                }
            }
        } else {
            holder.dayTitle.setText(holder.mContext.getString(R.string.training_plan_day_no_workout_text));
        }
        holder.dayNumber.setText(String.valueOf(position + 1));
        if(position + 1 < currentDay){
            holder.dayNumber.setTextColor(ViewStyling.getColor(R.attr.colorFitDisabled, holder.mContext));
        }
    }

    @Override
    public int getItemViewType(int position) {
        TrainingPlanDay tpd = trainingPlanDays.get(position);
        if (tpd != null) {
            if (tpd.getWorkout() != null) {
                return WORKOUT_VIEW;
            } else {
                return REST_DAY_VIEW;
            }
        } else {
            return NO_SCHEDULE_VIEW;
        }
    }

    public void setTrainingPlanDays(ArrayList<TrainingPlanDay> trainingPlanDays) {
        this.trainingPlanDays = trainingPlanDays;
    }

}

