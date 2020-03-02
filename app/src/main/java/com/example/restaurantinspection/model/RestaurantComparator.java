package com.example.restaurantinspection.model;

import java.util.Comparator;

public class RestaurantComparator implements Comparator<Restaurant> {


    @Override
    public int compare(Restaurant restaurant, Restaurant otherRestaurant) {
        return restaurant.getName().compareTo(otherRestaurant.getName());
    }
}
