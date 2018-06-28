package com.kinetic.fit.controllers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Saxton on 4/7/16.
 */
public class WorkoutTextAndTime implements Parcelable{
    String text;
    int powerStart = 0;
    int powerEnd = 0;
    double timeStart = 0;

    public String getText() {
        return text;
    }

    public int getPowerStart() {
        return powerStart;
    }

    public int getPowerEnd() {
        return powerEnd;
    }

    public double getTimeStart() {
        return timeStart;
    }

    public double getTimeEnd() {
        return timeEnd;
    }

    public double getCountdown() {
        return countdown;
    }

    public String getDurationText() {
        return durationText;
    }

    double timeEnd = 0;
    double countdown = 0;
    String durationText = "";

//        func attributedString(FTP:Int) -> NSAttributedString {
//            if powerStart == powerEnd {
//                return attributedStringForPower(powerStart, FTP:FTP, small:false)
//            } else {
//                let powerString = attributedStringForPower(powerStart, FTP:FTP, small:true)
//                let dashString = NSMutableAttributedString(string: "  ➞  ", attributes:[
//                NSFontAttributeName: UIFont.systemFontOfSize(20, weight: UIFontWeightLight),
//                NSForegroundColorAttributeName: KineticStyle.text
//                ])
//                powerString.appendAttributedString(dashString)
//                powerString.appendAttributedString(attributedStringForPower(powerEnd, FTP:FTP, small:true))
//                return powerString
//            }
//        }
//
//        private func attributedStringForPower(power:Int, FTP:Int, small:Bool) -> NSMutableAttributedString {
//            let powerColor = KineticStyle.colorForPropertyCategory(.Power)
//            if power < 0 {
//
//                let zoneAttString = NSMutableAttributedString(string: "zone ", attributes:[
//                NSFontAttributeName: UIFont.systemFontOfSize((small ? 26 : 32), weight: UIFontWeightLight),
//                NSForegroundColorAttributeName: KineticStyle.text
//                ])
//                let numString = String(format:"%d", abs(power))
//                let numAttString = NSMutableAttributedString(string: numString, attributes:[
//                NSFontAttributeName: UIFont.systemFontOfSize((small ? 40 : 48), weight: UIFontWeightLight),
//                NSForegroundColorAttributeName: powerColor
//                ])
//                zoneAttString.appendAttributedString(numAttString)
//                return zoneAttString
//            } else {
//                let targetPower = (Float(power) / 100) * Float(FTP)
//                let numString = String(format:"%.0f", targetPower)
//                let numAttString = NSMutableAttributedString(string: numString, attributes:[
//                NSFontAttributeName: UIFont.systemFontOfSize((small ? 36 : 48), weight: UIFontWeightLight),
//                NSForegroundColorAttributeName: powerColor
//                ])
//                let zoneAttString = NSMutableAttributedString(string: " watts", attributes:[
//                NSFontAttributeName: UIFont.systemFontOfSize((small ? 26 : 32), weight: UIFontWeightLight),
//                NSForegroundColorAttributeName: KineticStyle.text
//                ])
//                numAttString.appendAttributedString(zoneAttString)
//                return numAttString
//            }
//        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeInt(this.powerStart);
        dest.writeInt(this.powerEnd);
        dest.writeDouble(this.timeStart);
        dest.writeDouble(this.timeEnd);
        dest.writeDouble(this.countdown);
        dest.writeString(this.durationText);
    }

    public WorkoutTextAndTime() {
    }

    protected WorkoutTextAndTime(Parcel in) {
        this.text = in.readString();
        this.powerStart = in.readInt();
        this.powerEnd = in.readInt();
        this.timeStart = in.readDouble();
        this.timeEnd = in.readDouble();
        this.countdown = in.readDouble();
        this.durationText = in.readString();
    }

    public static final Creator<WorkoutTextAndTime> CREATOR = new Creator<WorkoutTextAndTime>() {
        @Override
        public WorkoutTextAndTime createFromParcel(Parcel source) {
            return new WorkoutTextAndTime(source);
        }

        @Override
        public WorkoutTextAndTime[] newArray(int size) {
            return new WorkoutTextAndTime[size];
        }
    };
}
