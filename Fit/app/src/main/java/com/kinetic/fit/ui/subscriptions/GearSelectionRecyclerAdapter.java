package com.kinetic.fit.ui.subscriptions;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.realm_objects.SubscriptionAddOn;
import com.kinetic.fit.util.FitImageRequestQueue;

import java.util.Locale;

import io.realm.RealmResults;

/**
 * Created by Saxton on 4/27/17.
 */

class GearSelectionRecyclerAdapter extends RecyclerView.Adapter<GearSelectionRecyclerAdapter.GearViewHolder> {
    private final static String TAG = "GearRecycler";
    RealmResults<SubscriptionAddOn> addOns;


    public interface GearSelectorToggleListener{
        void toggleGearSelection(int position);
    }
    Context mContext;
    GearSelectorToggleListener mListener;
    com.android.volley.toolbox.ImageLoader mImageLoader;


    @Override
    public GearSelectionRecyclerAdapter.GearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_gear, parent, false);
        mContext = parent.getContext();
        mImageLoader = FitImageRequestQueue.getInstance(mContext)
                .getImageLoader();
        return  new GearSelectionRecyclerAdapter.GearViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GearSelectionRecyclerAdapter.GearViewHolder holder, final int position) {

        SubscriptionAddOn addon = addOns.get(position);
        holder.gearName.setText(addon.getName());
        holder.gearPromo.setText(getLocalizedNumberString(getPenniesAsDollarDouble(addon.getPrice())));
        holder.gearRetail.setPaintFlags(holder.gearRetail.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        holder.gearRetail.setText(getLocalizedNumberString(getPenniesAsDollarDouble(addon.getRetailPrice())));
        mImageLoader.get(addon.getImageUrl(), com.android.volley.toolbox.ImageLoader.getImageListener(holder.gearImage, R.drawable.kinetic_logo, R.drawable.kinetic_logo));
//        ImageLoader.getInstance().cancelDisplayTask(holder.gearImage);
//        ImageLoader.getInstance().displayImage(addon.getImageUrl(), holder.gearImage);
        holder.gearImage.setAdjustViewBounds(true);
        holder.gearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "" + holder.getAdapterPosition());
                mListener.toggleGearSelection(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return addOns.size();
    }

    class GearViewHolder extends RecyclerView.ViewHolder {
        ImageView gearImage;
        TextView gearName;
        TextView gearRetail;
        TextView gearPromo;
        RelativeLayout gearLayout;

        public GearViewHolder(View itemView) {
            super(itemView);
            gearImage = (ImageView) itemView.findViewById(R.id.gear_image);
            gearName = (TextView) itemView.findViewById(R.id.gear_name);
            gearRetail = (TextView) itemView.findViewById(R.id.gear_retail_price);
            gearPromo = (TextView) itemView.findViewById(R.id.gear_promo_price);
            gearLayout = (RelativeLayout) itemView.findViewById(R.id.gear_layout);
        }
    }

    public void registerListener(GearSelectorToggleListener listener){
        mListener = listener;
    }

    public void unregisterListener(){
        mListener = null;
    }

    public RealmResults<SubscriptionAddOn> getAddOns() {
        return addOns;
    }

    public void setAddOns(RealmResults<SubscriptionAddOn> addOns) {
        this.addOns = addOns;
    }

    public String getLocalizedNumberString(double value){
        return "US$" + String.format(Locale.getDefault(), "%,.2f", value) ;
    }

    double getPenniesAsDollarDouble(int pennies){
        return ((double) pennies / 100.00);
    }

}

