package com.example.restaurantinspection.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Reader {

    public static void readRestaurantData(RestaurantManager restaurantManager,InputStream is){
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );

        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                // Split line by ','
                String[] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6]);

                restaurantManager.add(sample);
            }
        } catch (IOException e) {
            Log.wtf("READER", "Error reading data file on line" + line, e);
        }
    }

    public static void readInspectionData(RestaurantManager restaurantManager,InputStream is){
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        HashMap<String,Restaurant> hmap = new HashMap<>();
        for(Restaurant r : restaurantManager){
            hmap.put(r.getTrackingNumber(),r);
        }

        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                // Split line by ','
                //Log.d("TEST", line);

                String[] tokens = line.split(",");
                String var_token5;
                if (tokens.length >= 7 && tokens[5].length() > 0) {
                    var_token5 = tokens[5];
                } else {
                    var_token5 = "No violations";
                }

                RestaurantInspection sample = new RestaurantInspection(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        var_token5, tokens[6]);
                //Log.d("MY_ACTIVITY", sample.getTrackingNumber() + " " + sample.getInspectionDate());
                if(hmap.containsKey(sample.getTrackingNumber())){

                    hmap.get(sample.getTrackingNumber()).getRestaurantInspectionList().add(sample);
                }
            }
        } catch (IOException e) {
            Log.wtf("READER", "Error reading data file on line" + line, e);
        }
    }

}
