package com.kksionek.gdzietentramwaj.DataSource;

import java.util.Map;

import io.reactivex.annotations.Nullable;

public class TramDataWrapper {
    public Map<String, TramData> tramDataHashMap;
    public Throwable throwable;
    public boolean loading;

    public TramDataWrapper(
            @Nullable Map<String, TramData> tramDataHashMap,
            @Nullable Throwable throwable,
            boolean loading) {
        this.tramDataHashMap = tramDataHashMap;
        this.throwable = throwable;
        this.loading = loading;
    }
}
