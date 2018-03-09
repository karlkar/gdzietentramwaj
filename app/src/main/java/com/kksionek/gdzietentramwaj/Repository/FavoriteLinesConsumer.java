package com.kksionek.gdzietentramwaj.Repository;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import java.util.HashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class FavoriteLinesConsumer implements Function<List<TramData>, ObservableSource<List<TramData>>> {
    private static final String TAG = "FavoriteLinesConsumer";

    private final TramDao mTramDao;
    private final HashSet<String> mSavedLines = new HashSet<>();

    public FavoriteLinesConsumer(TramDao tramDao) {
        mTramDao = tramDao;
    }

    @Override
    public ObservableSource<List<TramData>> apply(List<TramData> list) throws Exception {
        return Observable.fromIterable(list)
                .doOnNext(tram -> {
                    if (!mSavedLines.contains(tram.getFirstLine())) {
                        mTramDao.save(new FavoriteTram(tram.getFirstLine(), false));
                        mSavedLines.add(tram.getFirstLine());
                    }
                })
                .toList()
                .toObservable();
    }
}
