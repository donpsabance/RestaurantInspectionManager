package com.example.restaurantinspection;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///////////////////////////////////
        // TODO: testing work on using retrofit
        // PRE: nothing    POST: i want to extract the url from

        ///////////////////////////////////
        readRestaurantData();
        readInspectionData();

        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }
        //TODO not really working right now for me
        //startActivity(new Intent(this, MapsActivity.class));

        loadRestaurants();
        registerClickFeedback();
    }

    private class CustomListAdapter extends ArrayAdapter<Restaurant> {
        public CustomListAdapter() {
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);

            ImageView imageView = itemView.findViewById(R.id.restaurantIcon);
            TextView addressText = itemView.findViewById(R.id.restaurantLocation);
            TextView descriptionText = itemView.findViewById(R.id.restaurantDescription);
            TextView reportText = itemView.findViewById(R.id.restaurantRecentReport);
            ProgressBar hazardRating = itemView.findViewById(R.id.hazardRatingBar);

            imageView.setImageResource(R.drawable.food);
            addressText.setText(restaurant.getAddress());
            descriptionText.setText(restaurant.getName());

            //make sure they have an inspection report available
            if (restaurant.getRestaurantInspectionList().size() > 0) {

                RestaurantInspection restaurantInspection = restaurant.getRestaurantInspectionList().get(0);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date inspectionDate = null;

                try {
                    inspectionDate = simpleDateFormat.parse(restaurantInspection.getInspectionDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int issuesFound = restaurantInspection.getNumCritical() + restaurantInspection.getNumNonCritical();

                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating(), issuesFound);

                String formattedInspectionDate = DateManager.formatDateInspection(inspectionDate);
                String reportMsg = "Most Recent Report: " + formattedInspectionDate + "\n";
                reportMsg += issuesFound + " issues found";

                imageView.setImageResource(R.drawable.food);
                descriptionText.setText(restaurant.getName());
                reportText.setText(reportMsg);

            } else {

                reportText.setText(R.string.noavailablereports);
                hazardRating.setVisibility(View.INVISIBLE);
            }
            return itemView;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel, int totalViolations) {

        if (hazardLevel.equalsIgnoreCase("LOW")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));

        } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));

        } else if (hazardLevel.equalsIgnoreCase("HIGH")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 66, 66)));
        }
        progressBar.setMax(100);
        progressBar.setProgress(5 + 10 * totalViolations);
    }

    private void registerClickFeedback() {

        ListView listView = findViewById(R.id.restaurantListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Restaurant restaurant = restaurantManager.getRestaurantList().get(position);
                Toast.makeText(MainActivity.this, "You clicked " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                //start restaurant activity
                Intent intent = RestaurantActivity.makeIntent(MainActivity.this, position);
                startActivity(intent);
            }
        });
    }

    private void readRestaurantData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants);
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
            Log.wtf(MAIN_ACTIVITY_TAG, "Error reading data file on line" + line, e);
        }
    }

    private void readInspectionData() {
        InputStream is = getResources().openRawResource(R.raw.inspectionsdata);
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
                String var_token6;
                if (tokens.length >= 7 && tokens[6].length() > 0) {
                    var_token6 = tokens[6];
                } else {
                    var_token6 = "No violations";
                }

                RestaurantInspection sample = new RestaurantInspection(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], var_token6);
                Log.d("MY_ACTIVITY", sample.getTrackingNumber() + " " + sample.getInspectionDate());
                for (Restaurant restaurant : restaurantManager) {
                    if (sample.getTrackingNumber().equalsIgnoreCase(restaurant.getTrackingNumber())) {
                        restaurant.getRestaurantInspectionList().add(sample);
                    }
                }
            }
        } catch (IOException e) {
            Log.wtf(MAIN_ACTIVITY_TAG, "Error reading data file on line" + line, e);
        }
    }

    public void loadRestaurants() {

        ArrayAdapter<Restaurant> arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }
}
