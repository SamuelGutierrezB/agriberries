package com.agriberriesmx.agriberries.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String MY_PREFERENCES = "myPrefs";
    private static final String NAME = "name";
    private static final String CATEGORY = "category";
    private static SharedPreferencesManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) instance = new SharedPreferencesManager(context);

        return instance;
    }

    public String getNameFromPreferences() {
        return sharedPreferences.getString(NAME, "");
    }

    public void saveNameFromPreferences(String name) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME, name);
        editor.apply();
    }

    public void removeNameFromPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(NAME);
        editor.apply();
    }

    public int getCategoryFromPreferences() {
        return sharedPreferences.getInt(CATEGORY, 1);
    }

    public void saveCategoryFromPreferences(int category) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CATEGORY, category);
        editor.apply();
    }

    public void removeCategoryFromPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(CATEGORY);
        editor.apply();
    }

}
