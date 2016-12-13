package com.kksionek.gdzietentramwaj;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TramInterface {

    public static final String ID = "c7238cfe-8b1f-4c38-bb4a-de386db7e776";
    public static final String APIKEY = "***REMOVED***";

    @GET("/api/action/wsstore_get/")
    Observable<TramList> getTrams(@Query("id") String id, @Query("apikey") String apikey);
}
