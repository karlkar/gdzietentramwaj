package com.kksionek.gdzietentramwaj.data;

import com.google.gson.annotations.SerializedName;
import com.kksionek.gdzietentramwaj.data.TramData;

import java.util.ArrayList;
import java.util.List;

public class TramList {

    @SerializedName("result")
    private ArrayList<TramData> mList;

    public List<TramData> getList() {
        return mList;
    }
}
