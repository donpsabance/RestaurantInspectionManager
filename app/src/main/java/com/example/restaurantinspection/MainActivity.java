package com.example.restaurantinspection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.restaurantinspection.model.InspectionManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private InspectionManager inspectionManager = InspectionManager.getInstance();

    private class CustomListAdapter extends ArrayAdapter<Restaurant> {
        public CustomListAdapter(){
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup){

            View itemView = view;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);

            ImageView imageView = itemView.findViewById(R.id.restaurantIcon);
            TextView addressText = itemView.findViewById(R.id.restaurantLocation);
            TextView descriptionText =  itemView.findViewById(R.id.restaurantDescription);
            TextView reportText = itemView.findViewById(R.id.restaurantRecentReport);
            ProgressBar hazardRating = itemView.findViewById(R.id.hazardRatingBar);

            imageView.setImageResource(R.drawable.food);
            addressText.setText(restaurant.getAddress());
            descriptionText.setText(restaurant.getName());

            //make sure they have an inspection report available
            if(restaurant.getInspection() != null){

                RestaurantInspection restaurantInspection = restaurant.getInspection();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date inspectionDate = null;

                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating());

                try {
                    inspectionDate = simpleDateFormat.parse(restaurantInspection.getInspectionDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int issuesFound = restaurantInspection.getNumCritical() + restaurantInspection.getNumNonCritical();

                String formattedInspectionDate = formatDateInspection(inspectionDate);
                String reportMsg = "Most Recent Report: " + formattedInspectionDate + "\n";
                reportMsg += issuesFound + " issues found";

                imageView.setImageResource(R.drawable.food);
                descriptionText.setText(restaurant.getName());
                reportText.setText(reportMsg);


            } else {

                reportText.setText("No available reports");
                hazardRating.setVisibility(View.INVISIBLE);

            }

            return itemView;

        }
    }


    private String formatDateInspection(Date inspectionDate){

        String result = "";

        Date dateToday = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inspectionDate);

        long dateDifference = TimeUnit.DAYS.convert(dateToday.getTime() - inspectionDate.getTime(), TimeUnit.MILLISECONDS);

        if(dateDifference < 30){
            result = Long.toString(dateDifference);
        } else if (dateDifference > 30 && dateDifference < 365){
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH) - 1] + " " + calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH) - 1] + " " + calendar.get(Calendar.YEAR);
        }

        return result;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel){

        if(hazardLevel.equalsIgnoreCase("LOW")){
            progressBar.setProgress(30);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));

        } else if(hazardLevel.equalsIgnoreCase("MODERATE")){

            progressBar.setProgress(60);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));

        } else if(hazardLevel.equalsIgnoreCase("HIGH")){

            progressBar.setProgress(90);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 66, 66)));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readRestaurantData();
        readInspectionData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        }


        loadRestaurants();
        registerClickFeedback();

    }

    private void registerClickFeedback(){

        ListView listView = findViewById(R.id.restaurantListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Restaurant restaurant = restaurantManager.getRestaurantList().get(position);
                Toast.makeText(MainActivity.this, "You clicked " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                //run intent
                Intent intent = RestaurantActivity.makeIntent(MainActivity.this, restaurant);
                startActivity(intent);
            }
        });
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
                // Split line by ','
                String [] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0],tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5],tokens[6]);

                restaurantManager.add(sample);
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

                inspectionManager.add(sample);
                for(Restaurant restaurant : restaurantManager){
                    if(sample.getTrackingNumber().equalsIgnoreCase(restaurant.getTrackingNumber())){

//                        Log.wtf("TEST", "FOUND MATCHING TRACKING");
                        restaurant.setInspection(sample);
                    }
                }
//                Log.d(MAIN_ACTIVITY_TAG, "Just created: " + sample);
            }
        }catch (IOException e){
            Log.wtf(MAIN_ACTIVITY_TAG,"Error reading data file on line" + line, e);
        }
    }

    public void loadRestaurants(){

        ArrayAdapter<Restaurant> arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }
}
