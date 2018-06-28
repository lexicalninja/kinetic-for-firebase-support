package com.kinetic.fit.ui.root;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.ui.analysis.AnalysisActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

import io.realm.RealmResults;

/**
 * Created by Saxton on 6/16/16.
 */
public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.ViewHolder> {

    public static final int TSS_VIEW = 0;
    public static final int TP_VIEW = 1;
    public static final int SESSION_VIEW = 3;
    public static final int HISTORY_HEAD_VIEW = 4;

    private RealmResults<Session> mSessionsROs;
    Context context;


    public HistoryRecyclerAdapter(RealmResults<Session> sessions) {
        mSessionsROs = sessions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (viewType == SESSION_VIEW) {
            View sessionView = layoutInflater.inflate(R.layout.list_item_session, parent, false);
            return new SessionViewHolder(sessionView);

        } else if (viewType == TSS_VIEW) {
            View tssView = layoutInflater.inflate(R.layout.recycler_element_root_tss_history, parent, false);
            TSSViewHolder vh = new TSSViewHolder(tssView);
            ((RootActivity) context).setTSSCardListener(vh);
            return vh;
        } else if (viewType == TP_VIEW) {
            View trainingView = layoutInflater.inflate(R.layout.recycler_view_element_root_training_plan, parent, false);
            TPViewHolder vh = new TPViewHolder(trainingView);
            ((RootActivity) context).setTrainingPlanCardListener(vh);
            return vh;
        } else {
            View headerView = layoutInflater.inflate(R.layout.recycler_view_history_header, parent, false);
            return new HistoryHeader(headerView);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position > 2) {
            final Session session = mSessionsROs.get(position - 3);
            ((SessionViewHolder) holder).uuid = session.getUuid();
            ((SessionViewHolder) holder).workoutName.setText(session.getWorkoutName());
            String shortDate = new DateTime(session.getWorkoutDate()).toString(DateTimeFormat.mediumDate());
            ((SessionViewHolder) holder).workoutDate.setText(shortDate.toUpperCase());
            if (session.getIntensityFactor() >= 0) {
                ((SessionViewHolder) holder).workoutIF.setText(String.format(Locale.getDefault(), "%1$.2f", session.getIntensityFactor()));
            } else {
                ((SessionViewHolder) holder).workoutIF.setText("---");
            }
            if (session.getTrainingStressScore() >= 0) {
                ((SessionViewHolder) holder).workoutTSS.setText(String.format(Locale.getDefault(), "%.0f", session.getTrainingStressScore()));
            } else {
                ((SessionViewHolder) holder).workoutTSS.setText("---");
            }
            ((SessionViewHolder) holder).duration.setText(ViewStyling.timeToStringHMS(session.getDuration(), false));
            if (session.getDistanceKM() >= 0) {
                if (SharedPreferencesInterface.isMetric()) {
                    ((SessionViewHolder) holder).distance.setText(FitProperty.Distance.getStringValue(session.getDistanceKM(), "%.2f km"));
                } else {
                    ((SessionViewHolder) holder).distance.setText(FitProperty.Distance.getStringValue(Conversions.kph_to_mph(session.getDistanceKM()), "%.2f mi"));
                }
            } else {
                ((SessionViewHolder) holder).distance.setText("---");
            }
            if (session.getKilojoules() >= 0) {
                ((SessionViewHolder) holder).watts.setText(String.format(Locale.getDefault(), "%.0f kJ", session.getKilojoules()));
            } else {
                ((SessionViewHolder) holder).watts.setText("---");
            }
            if (session.getAvgHeartRate() >= 0) {
                ((SessionViewHolder) holder).bpm.setText(String.format(Locale.getDefault(), "%.0f", session.getAvgHeartRate()));
            } else {
                ((SessionViewHolder) holder).bpm.setText("---");
            }
            ((SessionViewHolder) holder).layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RootActivity context = ((RootActivity) v.getContext());
                    Intent i = new Intent(context, AnalysisActivity.class).putExtra(AnalysisActivity.EXTRA_SESSION_UUID,((SessionViewHolder) holder).uuid);
                    context.startActivity(i);
                }
            });
        } else if (position == 0) {
//            todo tss info gets put here
        } else if (position == 1) {
//            todo fetch tpprogress and git bizzy
        }
    }

    @Override
    public int getItemCount() {
        return mSessionsROs.size() + 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 2) {
            return SESSION_VIEW;
        } else if (position == 0) {
            return TSS_VIEW;
        } else if (position == 1) {
            return TP_VIEW;
        } else {
            return HISTORY_HEAD_VIEW;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

    class SessionViewHolder extends ViewHolder {
        TextView workoutName;
        TextView workoutDate;
        TextView workoutIF;
        TextView workoutTSS;
        TextView duration;
        TextView distance;
        TextView watts;
        TextView bpm;
        LinearLayout layout;
        String uuid;

        SessionViewHolder(View view) {
            super(view);
            workoutName = (TextView) view.findViewById(R.id.list_item_session_name);
            workoutDate = (TextView) view.findViewById(R.id.list_item_session_date);
            workoutIF = (TextView) view.findViewById(R.id.list_item_session_if);
            workoutTSS = (TextView) view.findViewById(R.id.list_item_session_tss);
            duration = (TextView) view.findViewById(R.id.list_item_session_duration);
            distance = (TextView) view.findViewById(R.id.list_item_session_distance);
            watts = (TextView) view.findViewById(R.id.list_item_session_watts);
            bpm = (TextView) view.findViewById(R.id.list_item_session_bpm);
            layout = (LinearLayout) view.findViewById(R.id.history_linear_layout);
        }
    }

    class TSSViewHolder extends ViewHolder implements RootActivity.TSSCardListener {
        TextView thisWeekTSS;
        TextView lastWeekTSS;
        TextView thisMonthTSS;
        TextView lastMonthTSS;

        TSSViewHolder(View view) {
            super(view);
            thisWeekTSS = (TextView) view.findViewById(R.id.this_week_value);
            lastWeekTSS = (TextView) view.findViewById(R.id.last_week_value);
            thisMonthTSS = (TextView) view.findViewById(R.id.this_month_value);
            lastMonthTSS = (TextView) view.findViewById(R.id.last_month_value);
        }

        @Override
        public void setTSSValues(HashMap<String, Integer> map) {
            Formatter formatter = new Formatter();
            thisWeekTSS.setText(String.valueOf(map.get("thisWeek")));
            lastWeekTSS.setText(String.valueOf(map.get("lastWeek")));
            thisMonthTSS.setText(String.valueOf(map.get("thisMonth")));
            lastMonthTSS.setText(String.valueOf(map.get("lastMonth")));
        }
    }

    class TPViewHolder extends ViewHolder implements RootActivity.TrainingPlanCardListener {
        TextView title;
        TextView todaysMessage;
        FitButton tpButton;
        ProgressBar planProgress;
        TextView headline;

        TPViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.training_plan_title);
            todaysMessage = (TextView) view.findViewById(R.id.training_plan_day_message);
            tpButton = (FitButton) view.findViewById(R.id.training_plan_button);
            planProgress = (ProgressBar) view.findViewById(R.id.training_plan_progress_bar);
            headline = (TextView) view.findViewById(R.id.training_plan_headline);
        }

        @Override
        public void setMessageTitle(String text) {
            this.title.setText(text);
        }

        @Override
        public void setTodaysMessage(String text) {
            this.todaysMessage.setText(text);
        }

        @Override
        public void setButtonText(String text) {
            if (text.equals(context.getString(R.string.ride))) {
                tpButton.setFitButtonStyle("default");
            } else {
                tpButton.setFitButtonStyle("basic");
            }
            tpButton.setText(text);
        }

        @Override
        public void setOnClickListener(View.OnClickListener listener) {
            tpButton.setOnClickListener(listener);
        }

        @Override
        public void setPlanProgressBar(int progress) {
            planProgress.setProgress(progress);
        }

        @Override
        public void hideButton() {
            tpButton.setVisibility(View.GONE);
        }

        @Override
        public void setHeadline(String text) {
            if (text.equals("")) {
                headline.setVisibility(View.GONE);
            } else {
                headline.setVisibility(View.VISIBLE);
                headline.setText(text);
            }
        }
    }

    class HistoryHeader extends ViewHolder {
        HistoryHeader(View view) {
            super(view);
        }
    }


    void setSessionList(RealmResults<Session> sessionList) {
        mSessionsROs = sessionList;
        notifyDataSetChanged();
    }

}



