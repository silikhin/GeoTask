package com.silikhin.geotask;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RouteApiResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error_message")
    @Expose
    private String errorMessage;
    @SerializedName("routes")
    @Expose
    private List<Route> routes = null;

    public String getStatus() {
        return status;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}