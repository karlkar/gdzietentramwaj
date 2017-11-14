package com.kksionek.gdzietentramwaj.DataSource;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TramInterface {

    // https://api.um.warszawa.pl/api/action/busestrams_get/?resource_id=f2e5503e-927d-4ad3-9500-4ab9e55deb59&apikey=***REMOVED***&type=2

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
