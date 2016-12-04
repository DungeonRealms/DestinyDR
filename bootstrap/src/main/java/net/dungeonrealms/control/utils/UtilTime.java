package net.dungeonrealms.control.utils;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class UtilTime {

    public static String format(double seconds) {
        double minutes = seconds / 60D;
        double hours = minutes / 60D;
        double days = hours / 24D;
        if (minutes < 1)
            return UtilMath.trim(seconds) + " Seconds";
        else if (hours < 1)
            return UtilMath.trim(minutes % 60) + " Minutes";
        else if (days < 1)
            return UtilMath.trim(hours % 24) + " Hours";
        else
            return UtilMath.trim(days) + " Days";
    }
}
