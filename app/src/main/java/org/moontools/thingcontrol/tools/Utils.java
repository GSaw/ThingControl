package org.moontools.thingcontrol.tools;

/**
 * Created by georg on 12.11.16.
 */

public class Utils {

    public static String getFirstNChars(String str, int n) {
        if(str == null) {
            return null;
        }
        if(str.length() > n) {
            return str.substring(0, n);
        }
        return str;
    }

}
