package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class SingleInspectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);
        readRestaurantData();
    }

    private List<Restaurant> restaurantSamples= new ArrayList<>();
    private void readRestaurantData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while( (line = reader.readLine()) != null){
                //Log.d("MyActivity", "Line is: " + line);
                // Split line by ','
                String [] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0],tokens[1],
                                                    tokens[2], tokens[3], tokens[4],
                                                    tokens[5],tokens[6]);

                restaurantSamples.add(sample);
                Log.d("MyActivity", "Just created: " + sample);
            }
        }catch (IOException e){
            Log.wtf("MyActivity","Error reading data file on line" + line, e);
        }

    }

    //called by Restaurant Activity
    public static Intent makeIntent(Context context, RestaurantInspection restaurantInspection) {
        Intent intent = new Intent (context, SingleInspectionActivity.class);
        return intent;
    }

}
