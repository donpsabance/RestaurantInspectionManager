package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.example.restaurantinspection.model.InspectionManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import org.w3c.dom.Text;

public class RestaurantActivity extends AppCompatActivity {

    private static final String EXTRA_RESTAURANTINDEX = "com.example.restaurantinspection.RestaurantActivity - the restaurantIndex";
    private static final String EXTRA_RESTAURANTNAME = "com.example.restaurantinspection.RestaurantActivity - the restaurantName";
    private static final String EXTRA_RESTAURANTADDR = "com.example.restaurantinspection.RestaurantActivity - the restaurantAddress";;
    private static final String EXTRA_RESTAURANTLAT = "com.example.restaurantinspection.RestaurantActivity - the restaurantLatitude";
    private static final String EXTRA_RESTAURANTLON = "com.example.restaurantinspection.RestaurantActivity - the restaurantLongitude";

    //access restaurant information
    private RestaurantManager restaurantManager;
    private int restaurantIndex;
    private String restaurantName;
    private String restaurantAddr;
    private String restaurantLat;
    private String restaurantLon;

    //need a manager to keep track of list of restaurant inspections
    private RestaurantInspection restaurantInspection;
    private InspectionManager inspectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        extractDatafromIntent();
        updateTextView();
        displayInspectionList();
    }

    private void displayInspectionList() {
        //TODO: to-be implemented
    }

    //extract intent from RestaurantListActivity
    private void extractDatafromIntent() {
        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(EXTRA_RESTAURANTINDEX, 0);
        restaurantName = intent.getStringExtra(EXTRA_RESTAURANTNAME);
        restaurantAddr = intent.getStringExtra(EXTRA_RESTAURANTADDR);
        restaurantLat = intent.getStringExtra(EXTRA_RESTAURANTLAT);
        restaurantLon = intent.getStringExtra(EXTRA_RESTAURANTLON);
    }


    private void updateTextView() {
        TextView restaurantNameView = findViewById(R.id.restaurantnameid);
        restaurantNameView.setText(restaurantName);

        TextView restaurantAddrView = findViewById(R.id.restaurantaddrid);
        restaurantAddrView.setText(restaurantAddr);

        TextView restaurantGPSView = findViewById(R.id.restaurantgpsid);
        restaurantGPSView.setText(restaurantLat + ", " + restaurantLon);
    }

    //called by Main Activity
    public static Intent makeIntent(Context context, int restaurantIndex, Restaurant restaurant) {
        Intent intent = new Intent (context, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANTINDEX, restaurantIndex);
        intent.putExtra(EXTRA_RESTAURANTNAME, restaurant.getName());
        intent.putExtra(EXTRA_RESTAURANTADDR, restaurant.getAddress());
        intent.putExtra(EXTRA_RESTAURANTLAT, restaurant.getLatitude());
        intent.putExtra(EXTRA_RESTAURANTLON, restaurant.getLongitude());
        return intent;
    }





}
