package com.silikhin.geotask;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Leg {

    @SerializedName("steps")
    @Expose
    private List<Step> steps = null;

    public List<Step> getSteps() {
        return steps;
    }

}