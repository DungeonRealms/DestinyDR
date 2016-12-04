package net.dungeonrealms.control.utils;

import java.text.DecimalFormat;

/**
 * Created by Evoltr on 12/4/2016.
 */
public class UtilMath {

    public static double trim(double d) {
        return trim(d, 1);
    }

    public static double trim(double d, int degree) {
        if (Double.isNaN(d) || Double.isInfinite(d))
            d = 0;
        String format = "#.#";
        for (int i = 1; i < degree; i++)
            format += "#";
        try {
            return Double.valueOf(new DecimalFormat(format).format(d));
        } catch (NumberFormatException exception) {
            return d;
        }
    }
}
