package com.example.restaurantinspection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.InspectionManager;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RestaurantActivity extends AppCompatActivity {

    private static final String EXTRA_RESTAURANTNAME = "com.example.restaurantinspection.RestaurantActivity - the restaurantName";
    private static final String EXTRA_RESTAURANTADDR = "com.example.restaurantinspection.RestaurantActivity - the restaurantAddress";;
    private static final String EXTRA_RESTAURANTLAT = "com.example.restaurantinspection.RestaurantActivity - the restaurantLatitude";
    private static final String EXTRA_RESTAURANTLON = "com.example.restaurantinspection.RestaurantActivity - the restaurantLongitude";
    private static final String EXTRA_RESTAURANTTN = "com.example.restaurantinspection.RestaurantActivity - the restaurantTrackingNumber";

    //store extracted restaurant information
    private String restaurantName;
    private String restaurantAddr;
    private String restaurantLat;
    private String restaurantLon;
    private String restaurantTN;
    private InspectionManager inspectionManager = InspectionManager.getInstance();
    private List<RestaurantInspection> restaurantInspectionList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        extractDatafromIntent();
        updateTextView();

        //access relevant inspections for selected restaurant
        for (RestaurantInspection ri: inspectionManager){
            if (ri.getTrackingNumber().equals(restaurantTN)){
                restaurantInspectionList.add(ri);
            }
        }

        Collections.sort(restaurantInspectionList, new InspectionComparator());

        loadInspections();
        registerClickBack();

    }

    private void registerClickBack() {

            ListView listView = findViewById(R.id.inspectionList);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    RestaurantInspection restaurantInspection = restaurantInspectionList.get(position);
//                    Toast.makeText(RestaurantActivity.this, "You are inspecting report from " + restaurantInspection.getInspectionDate(), Toast.LENGTH_SHORT).show();
                    Log.d("MAKE",restaurantInspection.getViolations());
                    //run intent
                    Intent intent = SingleInspectionActivity.makeIntent(RestaurantActivity.this, restaurantInspection);
                    startActivity(intent);
                }
            });
    }


    private class CustomListAdapter extends ArrayAdapter<RestaurantInspection> {
        public CustomListAdapter(){
            super(RestaurantActivity.this, R.layout.restaurant_inspections_list, restaurantInspectionList);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurant_inspections_list, viewGroup, false);
            }

            RestaurantInspection restaurantInspection = restaurantInspectionList.get(position);

            //Textview
            TextView criticalText = itemView.findViewById(R.id.inspectionNumNonCritical);
            TextView nonCriticalText = itemView.findViewById(R.id.inspectionNumCritical);
            TextView timeText = itemView.findViewById(R.id.timeSinceInspection);
            Button hazardRating = itemView.findViewById(R.id.button);

            if (restaurantInspectionList.size() != 0) {

                //# critical issues found
                int numCritical = restaurantInspection.getNumCritical();

                //# non-critical issues found
                int numNonCritical = restaurantInspection.getNumNonCritical();

                //How long ago the inspection occurred
                DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
                try {
                    Date formatDate = format.parse(restaurantInspection.getInspectionDate());
                    String inspectionDate = formatDateInspection(formatDate);
                    timeText.setText("Inspection date: " + inspectionDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                criticalText.setText("Number of critical issues: " + numCritical);
                nonCriticalText.setText("Number of noncritical issues: " + numNonCritical);
                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating());
            }else{
                criticalText.setText("No inspections available");
                hazardRating.setVisibility(view.INVISIBLE);
            }

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
        restaurantTN = intent.getStringExtra(EXTRA_RESTAURANTTN);
    }


    private void updateTextView() {
        TextView restaurantNameView = findViewById(R.id.restaurantnameid);
        restaurantNameView.setText("Name: " + restaurantName);

        TextView restaurantAddrView = findViewById(R.id.restaurantaddrid);
        restaurantAddrView.setText("Address: " + restaurantAddr);

        TextView restaurantGPSView = findViewById(R.id.restaurantgpsid);
        restaurantGPSView.setText("GPS Coordinates: (" + restaurantLat + ", " + restaurantLon + ")");
    }


    public void loadInspections(){
        ArrayAdapter<RestaurantInspection> arrayAdapter = new RestaurantActivity.CustomListAdapter();
        ListView listView = findViewById(R.id.inspectionList);
        listView.setAdapter(arrayAdapter);
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
        } else if(dateDifference > 365){
//            result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH) - 1] + " " + calendar.get(Calendar.YEAR);
            Log.wtf("DATE:", dateDifference + " ");
            Log.wtf("DATE:", "  " + calendar.get(Calendar.MONTH) + " ");
        }

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(Button button, String hazardLevel){
        button.setPadding(25, 0, 25, 0);
        if(hazardLevel.equalsIgnoreCase("LOW")){
            button.setText("Low");
            button.setBackgroundColor(Color.rgb(75, 194, 54));
        } else if(hazardLevel.equalsIgnoreCase("MODERATE")){
            button.setText("Moderate");
            button.setBackgroundColor(Color.rgb(245, 158, 66));
        } else if(hazardLevel.equalsIgnoreCase("HIGH")){
            button.setText("High");
            button.setBackgroundColor(Color.rgb(245, 66, 66));
        }
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
}
