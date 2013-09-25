package util;

import java.text.DecimalFormat;

public class NumberFormatUtil {

    public static String formatScientific(double datum) {
        DecimalFormat df = new DecimalFormat ("0.###E0");
        return df.format(datum);
    }

    public static String format(double datum, String pattern) {
        DecimalFormat df = new DecimalFormat (pattern);
        return df.format(datum);
    }

}
