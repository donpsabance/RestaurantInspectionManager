package com.example.restaurantinspection.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InspectionManager implements Iterable<RestaurantInspection>{

    private List<RestaurantInspection> inspectionList = new ArrayList<>();

    public List<RestaurantInspection> getInspectionList() {
        return inspectionList;
    }

    public void add(RestaurantInspection inspection){inspectionList.add(inspection);}

    @NonNull
    @Override
    public Iterator<RestaurantInspection> iterator() {
        return inspectionList.iterator();
    }
}
