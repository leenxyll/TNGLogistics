package com.example.tnglogistics.Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String TAG = "SharedPreferencesHelper";
    private static final String KEY_APP_PREF = "appPreferences";
    private static final String KEY_LAST_ACTIVITY = "lastScreen";
    private static final String KEY_LAST_FRAGMENT = "lastFragment";
    private static final String KEY_USER_SESSION = "UserSession";
    private static final String KEY_TRUCK = "TruckReg";
    private static final String KEY_TRIP = "TripCode";
    private static final String KEY_MILEIN = "MileIn";
    private static final String KEY_USER = "User";

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

    public static void setEmployee(Context context, int EmpCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_APP_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER, EmpCode);
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
        return sharedPreferences.getString(KEY_LAST_FRAGMENT, "LoginDriverFragment");
    }

    public static void saveTruck(Context context, String truckReg){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TRUCK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.putString(KEY_TRUCK, truckReg);
        editor.apply();
    }

    public static String getTruck(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_TRUCK, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TRUCK, "");
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

    public static void saveMileIn(Context context, boolean isMileIn){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_MILEIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();;
        editor.putBoolean(KEY_MILEIN, isMileIn);
        editor.apply();
    }

    public static boolean getMileIn(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY_MILEIN, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_MILEIN, false);
    }




}
