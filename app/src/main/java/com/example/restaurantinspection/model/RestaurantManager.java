package com.example.restaurantinspection.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data model: Store a collection of restaurants.
 */

public class RestaurantManager implements Iterable<Restaurant> {
    private List<Restaurant> restaurantList = new ArrayList<>();
    private static RestaurantManager instance;

    //singleton model
    public static RestaurantManager getInstance(){
        if(instance == null){
            instance = new RestaurantManager();
        }
        return instance;
    }

    public List<Restaurant> getRestaurantList() {
        return restaurantList;
    }

    public void add(Restaurant restaurant){
        restaurantList.add(restaurant);
    }

    @NonNull
    @Override
    public Iterator<Restaurant> iterator() {
        return restaurantList.iterator();
    }
}
