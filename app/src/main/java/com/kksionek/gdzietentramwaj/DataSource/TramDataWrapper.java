package com.kksionek.gdzietentramwaj.DataSource;

import java.util.Map;

public class TramDataWrapper {
    public Map<String, TramData> tramDataHashMap;
    public Throwable throwable;

    public TramDataWrapper(Map<String, TramData> tramDataHashMap, Throwable throwable) {
        this.tramDataHashMap = tramDataHashMap;
        this.throwable = throwable;
    }
}
