package com.kksionek.gdzietentramwaj.Repository;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class FavoriteLinesConsumer implements Consumer<Map<String, TramData>> {
    private static final String TAG = "FavoriteLinesConsumer";

    private final TramDao mTramDao;
    private final HashSet<String> mSavedLines = new HashSet<>();

    public FavoriteLinesConsumer(TramDao tramDao) {
        mTramDao = tramDao;
    }

    @Override
    public void accept(Map<String, TramData> tramDataMap) throws Exception {
        for (String line : tramDataMap.keySet()) {
            if (!mSavedLines.contains(line)) {
                mTramDao.save(new FavoriteTram(line, false));
                mSavedLines.add(line);
            }
        }
    }
}
