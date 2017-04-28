package com.kksionek.gdzietentramwaj.data;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TramInterface {

    String ID = "f2e5503e-927d-4ad3-9500-4ab9e55deb59";
    int TYPE_BUS = 1;
    int TYPE_TRAM = 2;
    String APIKEY = "***REMOVED***";

    @GET("/api/action/busestrams_get/")
    Observable<TramList> getTrams(
            @Query("resource_id") String id,
            @Query("apikey") String apikey,
            @Query("type") int type);
}
