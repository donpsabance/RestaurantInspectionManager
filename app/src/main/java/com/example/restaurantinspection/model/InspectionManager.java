package com.example.restaurantinspection.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InspectionManager implements Iterable<RestaurantInspection> {
    private List<RestaurantInspection> inspectionList = new ArrayList<>();
    private static InspectionManager instance;

    //singleton model
    public static InspectionManager getInstance(){
        if(instance == null){
            instance = new InspectionManager();
        }
        return instance;
    }

    public List<RestaurantInspection> getInspectionList() {
        return inspectionList;
    }

    public void add(RestaurantInspection restaurantInspection){
        inspectionList.add(restaurantInspection);
    }

    @NonNull
    @Override
    public Iterator<RestaurantInspection> iterator() {
        return inspectionList.iterator();
    }
}
