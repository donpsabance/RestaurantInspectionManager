package com.example.restaurantinspection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RestaurantActivity extends AppCompatActivity {

    public static final String RESTAURANT_INDEX = "com.example.restaurantinspection - restaurant index";
    private int restaurantIndex;
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        List<String> favourite_list = readFavouriteList();
        extractDatafromIntent();
        updateTextView();

        //set back button
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadInspections();
        checkEmptyInspections();
        registerClickBack();
        registerClickGPS();
        compareRestaurant(favourite_list);
        initView(favourite_list);
    }



    private void compareRestaurant(List<String> list){
        for (String TrackingNum : list){
            if(TrackingNum.contains(restaurant.getTrackingNumber())){
                restaurant.setFavourite(true);
                //TODO save in copy as well
                for(Restaurant restaurant : restaurantManager.getFullRestaurantListCopy()){
                    if(TrackingNum.contains(restaurant.getTrackingNumber())){
                        restaurant.setFavourite(true);
                    }
                }
            }
        }

    }

    private void registerClickGPS() {

        //Start MapActivity upon clicking gps textview
        TextView textView = findViewById(R.id.restaurantgpsid);
        textView.setOnClickListener(v -> {
            Intent intent = MapsActivity.makeIntent(RestaurantActivity.this, restaurantIndex);
            startActivity(intent);
        });
    }

    private void checkEmptyInspections() {
        if (restaurant.getRestaurantInspectionList().size() == 0) {
            TextView noInspectionsText = findViewById(R.id.noinspectionsview);
            noInspectionsText.setText(R.string.noinspections);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    private void registerClickBack() {

        ListView listView = findViewById(R.id.inspectionList);
        listView.setOnItemClickListener((parent, view, position, id) -> {

            Intent intent = SingleInspectionActivity.makeIntent(RestaurantActivity.this, restaurantIndex, position);
            startActivity(intent);
        });
    }


    private class CustomListAdapter extends ArrayAdapter<RestaurantInspection> {
        public CustomListAdapter() {
            super(RestaurantActivity.this, R.layout.restaurant_inspections_list, restaurant.getRestaurantInspectionList());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurant_inspections_list, viewGroup, false);
            }

            RestaurantInspection restaurantInspection = restaurant.getRestaurantInspectionList().get(position);

            //Set all textview
            TextView criticalText = itemView.findViewById(R.id.numNonCritical);
            TextView nonCriticalText = itemView.findViewById(R.id.numCritical);
            TextView timeText = itemView.findViewById(R.id.inspectionDate);
            Button hazardRating = itemView.findViewById(R.id.button);

            if (restaurant.getRestaurantInspectionList().size() != 0) {

                //# critical issues found
                int numCritical = restaurantInspection.getNumCritical();
                String numCriticalstr = Integer.toString(numCritical);

                //# non-critical issues found
                int numNonCritical = restaurantInspection.getNumNonCritical();
                String numNonCriticalstr = Integer.toString(numNonCritical);

                //How long ago the inspection occurred
                DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
                try {
                    Date formatDate = format.parse(restaurantInspection.getInspectionDate());
                    String inspectionDate = DateManager.formatDateInspection(formatDate);
                    timeText.setText(inspectionDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                criticalText.setText(numCriticalstr);
                nonCriticalText.setText(numNonCriticalstr);
                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating());
            }
            restaurant = restaurantManager.getRestaurantList().get(restaurantIndex);
            return itemView;
        }

    }

    //extract intent from Main Activity
    private void extractDatafromIntent() {
        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(RESTAURANT_INDEX, 0);
        restaurant = restaurantManager.getRestaurantList().get(restaurantIndex);
    }

    private void updateTextView() {
        TextView restaurantNameView = findViewById(R.id.restaurantnameid);
        restaurantNameView.setText(restaurant.getName());

        TextView restaurantAddrView = findViewById(R.id.restaurantaddrid);
        restaurantAddrView.setText(String.format("%s, %s", restaurant.getAddress(), restaurant.getCity()));

        TextView restaurantGPSView = findViewById(R.id.restaurantgpsid);
        restaurantGPSView.setText(String.format("(%s, %s)", restaurant.getLatitude(), restaurant.getLongitude()));
    }


    public void loadInspections() {
        ArrayAdapter<RestaurantInspection> arrayAdapter = new RestaurantActivity.CustomListAdapter();
        ListView listView = findViewById(R.id.inspectionList);
        listView.setAdapter(arrayAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(Button button, String hazardLevel) {
        button.setPadding(25, 0, 25, 0);
        if (hazardLevel.equalsIgnoreCase("LOW")) {
            button.setText(R.string.low);
            button.setBackgroundColor(Color.rgb(75, 194, 54));
        } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {
            button.setText(R.string.moderate);
            button.setBackgroundColor(Color.rgb(245, 158, 66));
        } else if (hazardLevel.equalsIgnoreCase("HIGH")) {
            button.setText(R.string.high);
            button.setBackgroundColor(Color.rgb(245, 66, 66));
        }
    }

    private void initView(List<String> favourite_list){
        String TrackingNumber = restaurant.getTrackingNumber();
        String Date = "";
        if(restaurant.getRestaurantInspectionList().size() != 0){
            Date = restaurant.getRestaurantInspectionList().get(0).getInspectionDate();
        }
        String Last_Date = Date;
        ToggleButton favourite_Toggle = findViewById(R.id.favorite_toggle);
        favourite_Toggle.setChecked(restaurant.getFavourite());
        favourite_Toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                favourite_list.add(TrackingNumber + "+" + Last_Date);
                saveList(favourite_list);
                restaurant.setFavourite(true);
                // TODO set favourite as copy as well
                for(Restaurant restaurant_copy : restaurantManager.getFullRestaurantListCopy()){
                    if(TrackingNumber.contains(restaurant_copy.getTrackingNumber())){
                        restaurant_copy.setFavourite(true);
                    }
                }
                Log.d("favourite","add favourite");
                Log.d("List",favourite_list.toString());
            }else{
                favourite_list.remove(TrackingNumber + "+" + Last_Date);
                saveList(favourite_list);
                restaurant.setFavourite(false);
                // TODO set favourite for copy as well
                for(Restaurant restaurant_copy : restaurantManager.getFullRestaurantListCopy()){
                    if(TrackingNumber.contains(restaurant_copy.getTrackingNumber())){
                        restaurant_copy.setFavourite(false);
                    }
                }
                Log.d("favourite","Remove favourite");
                Log.d("List",favourite_list.toString());
            }
        });
    }



    //called by Main Activity
    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        return intent;
    }



    public void saveList(List<String> favourite_list){
        SharedPreferences sp = this.getSharedPreferences("favourite_list", Context.MODE_PRIVATE);
        Gson user_gson = new Gson();
        String favourite_jsonStr = user_gson.toJson(favourite_list);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Favourite_list",favourite_jsonStr);
        editor.apply();
    }

    public List<String> readFavouriteList() {
        List<String> list = new ArrayList<>();
        SharedPreferences sp1 = getSharedPreferences("favourite_list", Context.MODE_PRIVATE);
        String favourite_jsonStr = sp1.getString("Favourite_list","");
        if(!favourite_jsonStr.equals("")){
            Gson gson = new Gson();
            list = gson.fromJson(favourite_jsonStr,new TypeToken<List<String>>(){}.getType());
        }
        return list;
    }

    private List<String> getRestaurant(List<String> oldList){
        List<String> list = new ArrayList<>();
        for(String a : oldList){
            String[] arr = a.split("\\+");
            list.add(arr[0]);
        }

        return list;
    }
}
