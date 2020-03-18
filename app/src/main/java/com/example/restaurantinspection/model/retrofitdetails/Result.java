package com.example.restaurantinspection.model.retrofitdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Result {
    @SerializedName("resources")
    @Expose
    private ArrayList<Resource> resources;

    public ArrayList<Resource> getResources() {
        return resources;
    }

    public void setResources(ArrayList<Resource> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "Result{" +
                "resources=" + resources +
                '}';
    }
}
