package com.example.restaurantinspection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    private class CustomListAdapter extends ArrayAdapter<Restaurant> {
        public CustomListAdapter(){
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){

            View itemView = view;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);


            String reportMsg = "Most Recent Report: ";

            ImageView imageView = itemView.findViewById(R.id.restaurantIcon);
            imageView.setImageResource(R.drawable.food);

            TextView textView =  itemView.findViewById(R.id.restaurantDescription);
            textView.setText(restaurant.getName());

            TextView report = itemView.findViewById(R.id.restaurantRecentReport);
            report.setText(reportMsg);


            return itemView;

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readRestaurantData();
        readInspectionData();

        restaurantManager.getRestaurantList().sort(new RestaurantComparator());


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

                RestaurantInspection inspection = new RestaurantInspection(tokens[0],tokens[1],
                                            tokens[2], tokens[3], tokens[4],
                                            tokens[5],var_token6);

                //inspections.add(inspection);
                for(Restaurant restaurant : restaurantManager){
                    if(inspection.getTrackingNumber() == restaurant.getTrackingNumber()){
                        restaurant.getInspectionManager().add(inspection);
                    }
                }
                Log.d(MAIN_ACTIVITY_TAG, "Just created: " + inspection);
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
