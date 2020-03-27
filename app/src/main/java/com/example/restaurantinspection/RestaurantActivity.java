package com.example.restaurantinspection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    }

    private void registerClickGPS() {

        //Start MapActivity upon clicking gps textview
        TextView textView = findViewById(R.id.restaurantgpsid);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MapsActivity.makeIntent(RestaurantActivity.this, restaurantIndex);
                startActivity(intent);
            }
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = SingleInspectionActivity.makeIntent(RestaurantActivity.this, restaurantIndex, position);
                startActivity(intent);
            }
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
        restaurantAddrView.setText(restaurant.getAddress() + ", " + restaurant.getCity());

        TextView restaurantGPSView = findViewById(R.id.restaurantgpsid);
        restaurantGPSView.setText("(" + restaurant.getLatitude() + ", " + restaurant.getLongitude() + ")");
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

    //called by Main Activity
    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        return intent;
    }
}
