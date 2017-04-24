package com.silikhin.geotask;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Route {

    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("legs")
    @Expose
    private List<Leg> legs = null;

    public String getSummary() {
        return summary;
    }

    public List<Leg> getLegs() {
        return legs;
    }

}