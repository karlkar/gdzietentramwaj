package com.kksionek.gdzietentramwaj.Repository;

import android.util.Log;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.reactivex.functions.Consumer;

public class FavoriteLinesConsumer implements Consumer<List<TramData>> {
    private static final String TAG = "FavoriteLinesConsumer";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());
    private final TramDao mTramDao;
    private final HashSet<String> mSavedLines = new HashSet<>();

    public FavoriteLinesConsumer(TramDao tramDao) {
        mTramDao = tramDao;
    }

    @Override
    public void accept(List<TramData> tramData) throws Exception {
        long currentTime = System.currentTimeMillis();
        for (TramData tram : tramData) {
            if (!mSavedLines.contains(tram.getFirstLine())) {
                try {
                    if (currentTime - mDateFormat.parse(tram.getTime()).getTime() > 60000) {
                        tram.setTooOld();
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "TramRepository: ", e);
                    continue;
                }
                mTramDao.save(new FavoriteTram(tram.getFirstLine(), false));
                mSavedLines.add(tram.getFirstLine());
            }
        }
    }
}
