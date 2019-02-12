package com.kksionek.gdzietentramwaj.Repository;

import com.kksionek.gdzietentramwaj.dataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.dataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.dataSource.TramData;

import java.util.HashSet;
import java.util.Map;

import io.reactivex.functions.Consumer;

public class FavoriteLinesConsumer implements Consumer<Map<String, TramData>> {
    private static final String TAG = "FavoriteLinesConsumer";

    private final TramDao mTramDao;
    private final HashSet<String> mSavedLines = new HashSet<>();

    public FavoriteLinesConsumer(TramDao tramDao) {
        mTramDao = tramDao;
    }

    @Override
    public void accept(Map<String, TramData> tramDataMap) throws Exception {
        String firstLine;
        for (TramData line : tramDataMap.values()) {
            firstLine = line.getFirstLine();
            if (!mSavedLines.contains(firstLine)) {
                mTramDao.save(new FavoriteTram(firstLine, false));
                mSavedLines.add(firstLine);
            }
        }
    }
}
