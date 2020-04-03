package com.example.restaurantinspection.model;

import android.content.Context;

import androidx.preference.PreferenceManager;

//Followed The Big Nerd Ranch Guide (2017) Chapter 27 page 532
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SEARCH_QUERY, query).apply();
    }
}
