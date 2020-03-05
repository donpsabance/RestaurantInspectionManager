package com.example.restaurantinspection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.restaurantinspection.model.InspectionManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;

import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {

    private static final String EXTRA_RESTAURANTNAME = "com.example.restaurantinspection.RestaurantActivity - the restaurantName";
    private static final String EXTRA_RESTAURANTADDR = "com.example.restaurantinspection.RestaurantActivity - the restaurantAddress";;
    private static final String EXTRA_RESTAURANTLAT = "com.example.restaurantinspection.RestaurantActivity - the restaurantLatitude";
    private static final String EXTRA_RESTAURANTLON = "com.example.restaurantinspection.RestaurantActivity - the restaurantLongitude";
    private static final String EXTRA_RESTAURANTTN = "com.example.restaurantinspection.RestaurantActivity - the restaurantTrackingNumber";

    //access restaurant information
    private String restaurantName;
    private String restaurantAddr;
    private String restaurantLat;
    private String restaurantLon;
    private String restaurantTrackingNumber;
    private InspectionManager inspectionManager = InspectionManager.getInstance();

    private List<RestaurantInspection> restaurantInspectionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

//        displayInspections();
        extractDatafromIntent();
        updateTextView();

        loadInspections();

    }

//    private void displayInspections() {
//        for (RestaurantInspection RI: inspectionManager){
//            if (RI.getTrackingNumber() == restaurantTrackingNumber){
//                restaurantInspectionList.add(RI);
//            }
//        }
//    }

    private class CustomListAdapter extends ArrayAdapter<RestaurantInspection> {
        public CustomListAdapter(){
            super(RestaurantActivity.this, R.layout.restaurant_inspections_list, inspectionManager.getInspectionList());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurant_inspections_list, viewGroup, false);
            }

            TextView criticalText = itemView.findViewById(R.id.inspectionNumNonCritical);
            TextView nonCriticalText = itemView.findViewById(R.id.inspectionNumCritical);

            criticalText.setText("Number of critical issues: 1");
            nonCriticalText.setText("2");

            return itemView;
        }

    }

    //extract intent from RestaurantListActivity
    private void extractDatafromIntent() {
        Intent intent = getIntent();
        restaurantName = intent.getStringExtra(EXTRA_RESTAURANTNAME);
        restaurantAddr = intent.getStringExtra(EXTRA_RESTAURANTADDR);
        restaurantLat = intent.getStringExtra(EXTRA_RESTAURANTLAT);
        restaurantLon = intent.getStringExtra(EXTRA_RESTAURANTLON);
        restaurantTrackingNumber = intent.getStringExtra((EXTRA_RESTAURANTTN));
    }


    private void updateTextView() {
        TextView restaurantNameView = findViewById(R.id.restaurantnameid);
        restaurantNameView.setText("Name: " + restaurantName);

        TextView restaurantAddrView = findViewById(R.id.restaurantaddrid);
        restaurantAddrView.setText("Address: " + restaurantAddr);

        TextView restaurantGPSView = findViewById(R.id.restaurantgpsid);
        restaurantGPSView.setText("GPS Coordinates: (" + restaurantLat + ", " + restaurantLon + ")");
    }

    //called by Main Activity
    public static Intent makeIntent(Context context, Restaurant restaurant) {
        Intent intent = new Intent (context, RestaurantActivity.class);
        intent.putExtra(EXTRA_RESTAURANTTN, restaurant.getTrackingNumber());
        intent.putExtra(EXTRA_RESTAURANTNAME, restaurant.getName());
        intent.putExtra(EXTRA_RESTAURANTADDR, restaurant.getAddress());
        intent.putExtra(EXTRA_RESTAURANTLAT, restaurant.getLatitude());
        intent.putExtra(EXTRA_RESTAURANTLON, restaurant.getLongitude());
        return intent;
    }

    public void loadInspections(){
        ArrayAdapter<RestaurantInspection> arrayAdapter = new RestaurantActivity.CustomListAdapter();
        ListView listView = findViewById(R.id.inspectionList);
        listView.setAdapter(arrayAdapter);
    }
}
