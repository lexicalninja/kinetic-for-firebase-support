package com.kinetic.fit.data.objects;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Saxton on 5/8/17.
 */

public class SmartSubscription implements Parcelable {

    private String name;
    private int priceUSPennies;

    public SmartSubscription() {
        this.priceUSPennies = 5999;
        this.name = "12-month smart subscription";
    }

    public String getName() {
        return name;
    }

    public int getPriceUSPennies() {
        return priceUSPennies;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeInt(this.priceUSPennies);
    }

    protected SmartSubscription(Parcel in) {
        this.name = in.readString();
        this.priceUSPennies = in.readInt();
    }

    public static final Creator<SmartSubscription> CREATOR = new Creator<SmartSubscription>() {
        @Override
        public SmartSubscription createFromParcel(Parcel source) {
            return new SmartSubscription(source);
        }

        @Override
        public SmartSubscription[] newArray(int size) {
            return new SmartSubscription[size];
        }
    };
}
