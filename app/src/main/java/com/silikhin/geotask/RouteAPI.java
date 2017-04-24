package com.silikhin.geotask;

import android.location.Location;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by B_Silikhin on 023 23.04.17.
 */

public interface RouteAPI {
    String ENDPOINT = "https://maps.googleapis.com";

    @GET("/maps/api/directions/json")
    Call<RouteApiResponse> getRoute(@Query("origin") String origin,
                        @Query("destination") String destination,
                        @Query("key") String key
    );
}
