package com.kinetic.fit.util;

/**
 * Created by andrew on 2/1/15.
 */
public class Conversions {

    public static double kj_to_kcal(double kj){
        return ((kj / 4.184) / 0.2145);
    }

    public static double kph_to_mph(double kph){
        return (kph * 0.621371);
    }

    public static double kph_to_mps(double kph){
        return (kph * (1000 / 3600.f));
    }

    public static double mph_to_kph(double mph){
        return (mph * 1.60934);
    }

    public static double watts_to_kcalmin(double watts){
        return (watts * 0.01434034416);
    }

    public static double kg_to_lbs(double kg){
        return (kg * 2.20462);
    }

    public static double lbs_to_kg(double lbs){
        return (lbs / 2.2046);
    }

    public static double cm_to_inches(double cms){
        return (cms * 0.39370078740157477);
    }

    public static double inches_to_cm(double inches){
        return (inches / 0.39370078740157477);
    }

    public static String weekdayFromInteger(int weekdayNum){
        switch (weekdayNum){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "No Day here";
        }
    }

}
