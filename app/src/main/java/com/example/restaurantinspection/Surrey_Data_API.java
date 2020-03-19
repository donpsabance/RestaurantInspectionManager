package com.example.restaurantinspection;

import com.example.restaurantinspection.model.retrofitdetails.Feed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Surrey_Data_API {

    @GET("api/3/action/package_show")
    Call<Feed> getData(@Query("id") String id);
}
