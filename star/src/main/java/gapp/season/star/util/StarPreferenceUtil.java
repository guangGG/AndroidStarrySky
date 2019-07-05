package gapp.season.star.util;

import android.content.Context;
import android.content.SharedPreferences;

public class StarPreferenceUtil {
    private static final String SP_NAME = "sky_star";
    private static final String LOCAL_LONGITUDE = "local_longitude";
    private static final String LOCAL_LATITUDE = "local_latitude";

    public static void saveLocation(Context context, double longitude, double latitude) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit().putFloat(LOCAL_LONGITUDE, (float) longitude)
                .putFloat(LOCAL_LATITUDE, (float) latitude)
                .apply();
    }

    public static float getLocation(Context context, boolean latitude) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        //深圳经纬度:(114.06,22.54)
        if (latitude) {
            return sp.getFloat(LOCAL_LATITUDE, 22.5f);
        } else {
            return sp.getFloat(LOCAL_LONGITUDE, 114f);
        }
    }
}
