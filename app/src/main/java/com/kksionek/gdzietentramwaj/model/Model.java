package com.kksionek.gdzietentramwaj.model;

import com.kksionek.gdzietentramwaj.view.NaturalOrderComparator;

import java.util.SortedMap;
import java.util.TreeMap;

public class Model {

    private static final String TAG = "MODEL";
    private final TreeMap<String, Boolean> mFavoriteTramDatas = new TreeMap<>(new NaturalOrderComparator());
    private FavoriteManager mFavoriteManager = null;

    private Model() {
    }

    public static Model getInstance() {
        return Holder.instance;
    }

    public FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    public SortedMap<String, Boolean> getFavoriteTramData() {
        return mFavoriteTramDatas;
    }

    private static class Holder {
        static final Model instance = new Model();
    }
}
