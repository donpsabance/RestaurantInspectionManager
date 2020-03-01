package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    private List<Restaurant> restaurantSamples= new ArrayList<>();
    private List<RestaurantInspection> inspections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readRestaurantData();
        readInspectionData();
    }

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
                Log.d(MAIN_ACTIVITY_TAG, "Line is: " + line);
                // Split line by ','
                String [] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0],tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5],tokens[6]);

                restaurantManager.add(sample);
                Log.d(MAIN_ACTIVITY_TAG, "Just created: " + sample);
            }
        }catch (IOException e){
            Log.wtf(MAIN_ACTIVITY_TAG,"Error reading data file on line" + line, e);
        }

    }
    private void readInspectionData() {
        InputStream is = getResources().openRawResource(R.raw.inspections);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while( (line = reader.readLine()) != null){
                Log.d(MAIN_ACTIVITY_TAG, "Line is: " + line);
                // Split line by ','
                String [] tokens = line.split(",");
                String var_token6;
                if(tokens.length >= 7 && tokens[6].length() > 0){
                    var_token6 = tokens[6];
                }else{
                    var_token6 = "No violations";
                }

                RestaurantInspection sample = new RestaurantInspection(tokens[0],tokens[1],
                                            tokens[2], tokens[3], tokens[4],
                                            tokens[5],var_token6);

                //inspections.add(sample);
                for(Restaurant restaurant : restaurantManager){
                    if(sample.getTrackingNumber() == restaurant.getTrackingNumber()){
                        restaurant.setInspection(sample);
                    }
                }
                Log.d(MAIN_ACTIVITY_TAG, "Just created: " + sample);
            }
        }catch (IOException e){
            Log.wtf(MAIN_ACTIVITY_TAG,"Error reading data file on line" + line, e);
        }
    }
}
