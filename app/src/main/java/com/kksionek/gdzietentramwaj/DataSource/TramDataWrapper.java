package com.kksionek.gdzietentramwaj.DataSource;

import java.util.List;

public class TramDataWrapper {
    public List<TramData> tramDataList;
    public Throwable throwable;

    public TramDataWrapper(List<TramData> tramDataList, Throwable throwable) {
        this.tramDataList = tramDataList;
        this.throwable = throwable;
    }
}
