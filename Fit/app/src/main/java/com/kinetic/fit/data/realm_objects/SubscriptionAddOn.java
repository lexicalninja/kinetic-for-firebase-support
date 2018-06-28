package com.kinetic.fit.data.realm_objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.json.Json;
import com.google.gson.JsonObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 5/5/17.
 */

public class SubscriptionAddOn extends RealmObject implements Parcelable{
    String name;
    @PrimaryKey
    String sku;
    int price;
    int retailPrice;
    String imageUrl;
    String color;

    public SubscriptionAddOn() {
    }

    public SubscriptionAddOn(JsonObject item){
        name = item.get("name").getAsString();
        sku  = item.get("sku").getAsString();
        imageUrl  = item.get("image").getAsString();
        if(!item.getAsJsonObject("attributes").isJsonNull()){
            if(item.getAsJsonObject("attributes").get("color") != null) {
                color = item.getAsJsonObject("attributes").get("color").getAsString();
            }
        }
        price = item.get("price").getAsInt();
        retailPrice = Integer.parseInt(item.get("retailPrice").getAsString());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(int retailPrice) {
        this.retailPrice = retailPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.sku);
        dest.writeInt(this.price);
        dest.writeInt(this.retailPrice);
        dest.writeString(this.imageUrl);
        dest.writeString(this.color);
    }

    protected SubscriptionAddOn(Parcel in) {
        this.name = in.readString();
        this.sku = in.readString();
        this.price = in.readInt();
        this.retailPrice = in.readInt();
        this.imageUrl = in.readString();
        this.color = in.readString();
    }

    public static final Creator<SubscriptionAddOn> CREATOR = new Creator<SubscriptionAddOn>() {
        @Override
        public SubscriptionAddOn createFromParcel(Parcel source) {
            return new SubscriptionAddOn(source);
        }

        @Override
        public SubscriptionAddOn[] newArray(int size) {
            return new SubscriptionAddOn[size];
        }
    };
}
