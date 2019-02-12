package com.kksionek.gdzietentramwaj.DataSource;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class TramList {

    @SerializedName("result")
    private ArrayList<TramData> mList;

    public List<TramData> getList() {
        return mList;
    }
}
