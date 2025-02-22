package com.example.tnglogistics.Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";
    private static final String KEY_APP_PREF = "appPreferences";
    private static final String KEY_USER_SESSION = "UserSession";

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_USER_SESSION, true);  // Default to false
    }

    public static void setUserLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_SESSION, isLoggedIn);
        editor.apply();
    }
}
