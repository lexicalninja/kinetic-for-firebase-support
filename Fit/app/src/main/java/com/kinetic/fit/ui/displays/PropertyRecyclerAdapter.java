package com.kinetic.fit.ui.displays;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.util.ViewStyling;

import java.util.ArrayList;

/**
 * Created by Saxton on 5/24/17.
 */

public class PropertyRecyclerAdapter extends android.support.v7.widget.RecyclerView.Adapter<PropertyRecyclerAdapter.ViewHolder> {


    private static final int HEADER_VIEW = 0;
    private static final int CAT_VIEW = 1;

    ArrayList<ModelHelper> listItems;
    private Context mContext;


    PropertySelectListener mListener;

    public PropertyRecyclerAdapter(Context context) {
        this.mContext = context;
        getListItems();
    }

    class ModelHelper {
        FitProperty property;
        boolean isHeader;
        String title;
        int imageRes;

        public ModelHelper(FitProperty property) {
            this.property = property;
            this.isHeader = false;
        }

        public ModelHelper(boolean isHeader, String title, int imageRes) {
            this.isHeader = isHeader;
            this.title = title;
            this.imageRes = imageRes;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public int getOrdinal(){
            return property.ordinal();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class HeaderViewHolder extends ViewHolder {

        ImageView propertyIcon;
        TextView propertyCategory;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            propertyIcon = (ImageView) itemView.findViewById(R.id.property_image);
            propertyCategory = (TextView) itemView.findViewById(R.id.property_category);
        }
    }

    class ItemViewHolder extends ViewHolder {
        FitProperty property;
        TextView propertyName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            propertyName = (TextView) itemView.findViewById(R.id.fit_proprty_text_view);
        }

        public void setProperty(FitProperty property) {
            this.property = property;
            propertyName.setText(property.title);
        }

        public void setSelected(){
            propertyName.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
            propertyName.setTextColor(ViewStyling.getColor(R.attr.colorFitBg0, mContext));
        }

        public void deselect(){
            propertyName.setBackgroundColor(ViewStyling.getColor(R.attr.colorFitBg0, mContext));
            propertyName.setTextColor(ViewStyling.getColor(R.attr.colorFitBody, mContext));
        }
    }

    @Override
    public PropertyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        final ViewHolder viewHolder;
        if (viewType == HEADER_VIEW) {
            View headerView = inflater.inflate(R.layout.recycler_element_property_header, parent, false);
            viewHolder = new HeaderViewHolder(headerView);
        } else {
            View propertyView = inflater.inflate(R.layout.list_item_fit_property, parent, false);
            viewHolder = new ItemViewHolder(propertyView);
            propertyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.selectProperty(viewHolder.getAdapterPosition(), v);
                }
            });
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).propertyIcon.setImageResource(listItems.get(position).imageRes);
            ((HeaderViewHolder) holder).propertyCategory.setText(listItems.get(position).title);
        } else {
            ((ItemViewHolder) holder).setProperty(listItems.get(position).property);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (listItems.get(position).isHeader()) {
            return HEADER_VIEW;
        } else {
            return CAT_VIEW;
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public interface PropertySelectListener {
        void selectProperty(int pos, View v);
    }

    public void resisterListener(PropertySelectListener listener) {
        this.mListener = listener;
    }

    private void getListItems() {
        listItems = new ArrayList<>();
        for (FitProperty.Category c : FitProperty.Category.values()) {
            if(c != FitProperty.Category.None) {
                listItems.add(new ModelHelper(true, c.name(), getImageRes(c)));
            }
            for (FitProperty p : FitProperty.values()) {
                if(p.category == c && p.category != FitProperty.Category.None){
                    listItems.add(new ModelHelper(p));
                }
            }
        }
        notifyDataSetChanged();
    }

    private int getImageRes(FitProperty.Category cat) {
        switch (cat) {
            case Power: {
                return FitProperty.Power.image;
            }
            case Heart: {
                return FitProperty.HeartRate.image;
            }
            case Speed: {
                return FitProperty.SpeedKPH.image;
            }
            case Time: {
                return FitProperty.LapTime.image;
            }
            case Cadence: {
                return FitProperty.Cadence.image;
            }
            case Distance: {
                return FitProperty.Distance.image;
            }
        }
        return FitProperty.Power.image;
    }


}
