package com.conlistech.sportsclubbookingengine.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedPrefManager {

    private static final String SPORT_PREF = "sports_pref";

    public static SharedPreferences sport_pref;

    private SharedPrefManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        sport_pref = context.getSharedPreferences(SPORT_PREF, Context.MODE_PRIVATE);
        return sport_pref;
    }

    public static String getSportName(Context context, String sportId) {
        return getSharedPreferences(context).getString(sportId , null);
    }

    public static void saveValues(Context context, String sportId, String sportName) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(sportId , sportName);
        editor.commit();
    }
}
