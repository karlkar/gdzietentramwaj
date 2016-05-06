package com.kksionek.gdzietentramwaj;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefManager {
    public static final String PREFERENCES = "MyPrefs";
    private static final String PREF_FAVORITE_VIEW = "PREF_FAVORITE_VIEW";
    private static final String PREF_FAVORITE_TRAMS = "PREF_FAVORITE_TRAMS";

    private static SharedPreferences mPrefs = null;
    private static Set<String> favoriteTramData;

    private PrefManager() {}

    public static void init(Context ctx) {
        if (mPrefs == null)
            mPrefs = ctx.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static void setFavoriteViewOn(boolean favoriteViewOn) {
        mPrefs.edit().putBoolean(PREF_FAVORITE_VIEW, favoriteViewOn).apply();
    }

    public static boolean isFavoriteViewOn() {
        return mPrefs.getBoolean(PREF_FAVORITE_VIEW, false);
    }

    public static Set<String> getFavoriteTramData() {
        return mPrefs.getStringSet(PREF_FAVORITE_TRAMS, new HashSet<String>());
    }

    public static void setFavoriteTramData(Set<String> favoriteTramData) {
        mPrefs.edit().remove(PREF_FAVORITE_TRAMS).putStringSet(PREF_FAVORITE_TRAMS, favoriteTramData).apply();
    }
}
