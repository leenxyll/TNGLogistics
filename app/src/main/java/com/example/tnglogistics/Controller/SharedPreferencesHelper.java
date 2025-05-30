package com.example.tnglogistics.Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";
    private static final String KEY_APP_PREF = "appPreferences";
    private static final String KEY_LAST_ACTIVITY = "lastScreen";
    private static final String KEY_LAST_FRAGMENT = "lastFragment";
    private static final String KEY_USER_SESSION = "UserSession";
//    private static final String KEY_TRUCK = "TruckReg";
    private static final String KEY_TRIP = "TripCode";
    private static final String KEY_MILE = "Mile";
    private static final String KEY_USER = "User";
    private static final String KEY_TAKEMILE = "TakeMile";
    private static final String KEY_LOCATION = "CurrentLocation";

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_USER_SESSION, false);  // Default to false
    }

    public static void setUserLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_USER_SESSION, isLoggedIn);
        editor.apply();
    }

    public static int getEmployee(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER, 0);  // Default to false
    }

    public static void setEmployee(Context context, int empCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER, empCode);
        editor.apply();
    }

    public static void saveLastActivity(Context context, String activityName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.putString(KEY_LAST_ACTIVITY, activityName);
        editor.apply();
    }

    public static String getLastActivity(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_LAST_ACTIVITY, "SplashActivity");
    }

    public static void saveLastFragment(Context context, String fragmentName){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.putString(KEY_LAST_FRAGMENT, fragmentName);
        editor.apply();
    }

    public static String getLastFragment(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_LAST_FRAGMENT, "");
    }


    public static void saveTrip(Context context, String tripCode){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TRIP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.putString(KEY_TRIP, tripCode);
        editor.apply();
    }

    public static String getTrip(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TRIP, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TRIP, "");
    }

    public static void saveMileType(Context context, int mileType){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_MILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_MILE, mileType);
        editor.apply();
    }

    public static int getMileType(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_MILE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_MILE, 0);
    }

    public static boolean isCameraMile(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TAKEMILE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_TAKEMILE, true);
    }

    public static void setCameraMile(Context context, boolean isCameraMile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TAKEMILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_TAKEMILE, isCameraMile);
        editor.apply();
    }

    public static void saveCurrentInvoiceLocation(Context context, int location) {
        SharedPreferences prefs = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LOCATION, location);
        editor.apply();
    }

    public static int getCurrentInvoiceLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_LOCATION, 0);
    }

}
