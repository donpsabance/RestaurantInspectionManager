package com.example.restaurantinspection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://data.surrey.ca/";
    private static final String ID_RESTAURANTS = "restaurants";
    private static final String ID_INSPECTIONS = "fraser-health-restaurant-inspection-reports";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private static final int ACTIVITY_RESULT_FINISH = 101;
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private ArrayAdapter<Restaurant> arrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readRestaurantData();
        readInspectionData();

        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }
//        if(CompareTime())
//        {
//            ShowUpdateDialog();
//        }
//        startActivity(new Intent(this, MapsActivity.class));

        startActivity(new Intent(this, MapsActivity.class));

        loadRestaurants();
        registerClickFeedback();
        setUpMapButton();
    }
    public boolean CompareTime()
    {
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("user", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        String LastModifiedTime = LastModifiedTimeFile.getString("Last_Modified_time",DefaultTime);
        Log.d("Time",LastModifiedTime);
        String CurrentTime = "2020-03-18T08:18:00.000000";
        if(!LastModifiedTime.equals(CurrentTime))
        {
            SharedPreferences.Editor editor = LastModifiedTimeFile.edit();
            editor.putString("Last_Modified_time",CurrentTime);
            editor.apply();
            LastModifiedTime = LastModifiedTimeFile.getString("Last_Modified_time",DefaultTime);
            Log.d("Time",LastModifiedTime+" has changed");
            return true;
        }
        return false;

    }

    public static class UpdateDialog extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder UpdateDialog = new AlertDialog.Builder(getActivity());
            UpdateDialog.setMessage(R.string.Update).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Log.d("TAG", "onClick: update the information");
                }
            })
            .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("TAG", "onClick: did nothing");
                }
            });
            return UpdateDialog.create();
        }
    }

    public void ShowUpdateDialog()
    {
        DialogFragment showUpdateDialog = new UpdateDialog();
        showUpdateDialog.show(getSupportFragmentManager(),"update");
    }


    private void setUpMapButton() {

        //start MapActivity
        FloatingActionButton fab = findViewById(R.id.mapButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivityForResult(intent, ACTIVITY_RESULT_FINISH);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
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

                int icon = determineIcon(restaurant.getName());

                imageView.setImageResource(icon);
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

    private int determineIcon(String restaurantName) {

        if (restaurantName.toLowerCase().contains("pizza")) {
            return R.drawable.pizza;
        } else if (restaurantName.toLowerCase().contains("burger") ||
                restaurantName.toLowerCase().contains("a&w")) {
            return R.drawable.burger;
        } else if (restaurantName.toLowerCase().contains("sushi")) {
            return R.drawable.sushi;
        } else if (restaurantName.toLowerCase().contains("subway") ||
                restaurantName.toLowerCase().contains("sandwich")) {
            return R.drawable.sandwich;
        } else if (restaurantName.toLowerCase().contains("coffee") ||
                restaurantName.toLowerCase().contains("tim hortons") ||
                restaurantName.toLowerCase().contains("startbucks")) {
            return R.drawable.coffee;
        } else if (restaurantName.toLowerCase().contains("chicken")) {
            return R.drawable.chicken;
        } else if (restaurantName.toLowerCase().contains("seafood")) {
            return R.drawable.lobster;
        } else if (restaurantName.toLowerCase().contains("mcdonalds")) {
            return R.drawable.mcdonalds;
        } else if (restaurantName.toLowerCase().contains("taco")) {
            return R.drawable.taco;
        } else if (restaurantName.toLowerCase().contains("noodles") ||
                restaurantName.toLowerCase().contains("ramen") ||
                restaurantName.toLowerCase().contains("pho")) {
            return R.drawable.noodles;
        }

        //default
        return R.drawable.food;

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
        InputStream is = getResources().openRawResource(R.raw.new_inspections);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                // Split line by ','
                Log.d("TEST", line);

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
        arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter.notifyDataSetChanged();
//        restaurantManager = RestaurantManager.getInstance();;
        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }
    }
}
