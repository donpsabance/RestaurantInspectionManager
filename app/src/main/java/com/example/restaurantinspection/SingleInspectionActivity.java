package com.example.restaurantinspection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Violation;


import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SingleInspectionActivity extends AppCompatActivity {

    public static final String EXTRA_RESTAURANT_INDEX = "com.example.restaurantinspection - restaurant index";
    public static final String EXTRA_INSPECTION_INDEX = "com.example.restaurantinspection - inspection index";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private RestaurantInspection restaurantInspection;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);
        getFromExtra();
        updateTextUI();
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadViolations();
        checkEmptyViolations();
        registerClickCallback();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }


    private void getFromExtra() {
        Intent intent = getIntent();
        int restaurant_index = intent.getIntExtra(EXTRA_RESTAURANT_INDEX, 0);
        int inspection_index = intent.getIntExtra(EXTRA_INSPECTION_INDEX, 0);

        restaurantInspection = restaurantManager.getRestaurantList()
                .get(restaurant_index)
                .getRestaurantInspectionList().get(inspection_index);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateTextUI() {
        String message;

        TextView inspectionTypeView = findViewById(R.id.inspectionType);
        message = getString(R.string.inspection_type) + " " + restaurantInspection.getInspectionType();
        inspectionTypeView.setText(message);

        TextView inspectionDateView = findViewById(R.id.inspectionDate);
        message = getString(R.string.date) + formatDate(restaurantInspection.getInspectionDate());
        inspectionDateView.setText(message);


        TextView numCriticalView = findViewById(R.id.numCriticalmsg);
        message = getString(R.string.critical_issues);
        message = message + " " + restaurantInspection.getNumCritical();
        numCriticalView.setText(message);

        TextView numNonCriticalView = findViewById(R.id.numNonCriticalmsg);
        message = getString(R.string.non_crit_issuesmessage) + restaurantInspection.getNumNonCritical();
        numNonCriticalView.setText(message);


        TextView hazardResultView = findViewById(R.id.HazardResult);
        hazardResultView.setText(restaurantInspection.getHazardRating());

        ProgressBar progressBar = findViewById(R.id.hazardBarSingle);
        determineHazardLevel(progressBar,restaurantInspection.getHazardRating(),hazardResultView);
        hazardResultView.setText(restaurantInspection.getHazardRating());


    }

    private String formatDate(String unformattedDate) {
        DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String result = "";
        try {
            Date formatDate = format.parse(unformattedDate);
            result = formatDateInspection(formatDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String formatDateInspection(Date formatDate) {
        String result;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formatDate);

        result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)] + " " +
                calendar.get(Calendar.DAY_OF_MONTH) +
                ", " + calendar.get(Calendar.YEAR);

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel, TextView hazardTextView){

        if(hazardLevel.equalsIgnoreCase("LOW")){
            progressBar.setProgress(30);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));
            hazardTextView.setTextColor(Color.rgb(75, 194, 54));

        } else if(hazardLevel.equalsIgnoreCase("MODERATE")){

            progressBar.setProgress(60);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));
            hazardTextView.setTextColor(Color.rgb(245, 158, 66));

        } else if(hazardLevel.equalsIgnoreCase("HIGH")){

            progressBar.setProgress(90);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 66, 66)));
            hazardTextView.setTextColor(Color.rgb(245, 66, 66));
        }
    }

    private void loadViolations() {
        ArrayAdapter<Violation> adapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.inspectionListView);
        listView.setAdapter(adapter);
    }

    private class CustomListAdapter extends ArrayAdapter<Violation>{

        public CustomListAdapter(){
            super(SingleInspectionActivity.this,R.layout.list_violations_layout,restaurantInspection.getViolationsList());
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemview = view;
            if(itemview == null){
                itemview = getLayoutInflater().inflate(R.layout.list_violations_layout,viewGroup,false);
            }
            ImageView imageView = itemview.findViewById(R.id.violationIcon);
            TextView briefMessage_view = itemview.findViewById(R.id.violationDescription);
            TextView severityMessage_view = itemview.findViewById(R.id.violationSeverityTxt);
            ImageView colorBlock_imageview = itemview.findViewById(R.id.hazardColor);


            Violation violation = restaurantInspection.getViolationsList().get(position);
            //TODO use violation data to create listview

            int id_num = violation.getViolation_id();

            if(id_num >= 100 && id_num < 200){
                imageView.setImageResource(R.drawable.permits);
                briefMessage_view.setText(R.string.violation_100_199);
            }else if(id_num >= 200 && id_num < 300){
                imageView.setImageResource(R.drawable.food_safety);
                briefMessage_view.setText(R.string.violation_200_299);
            }else if (id_num >= 300 && id_num < 400){
                imageView.setImageResource(R.drawable.equipment_cleanliness);
                briefMessage_view.setText(R.string.violation_300_399);
            }else if(id_num >= 400 && id_num < 500){
                imageView.setImageResource(R.drawable.wash_hands);
                briefMessage_view.setText(R.string.violation_400_499);
            }else if(id_num >= 500 && id_num < 600){
                imageView.setImageResource(R.drawable.food_safe);
                briefMessage_view.setText(R.string.violation_500_599);
            }

            if(violation.getStatus().equalsIgnoreCase("Critical")){
                colorBlock_imageview.setColorFilter(ContextCompat.getColor(SingleInspectionActivity.this,R.color.colorAccent));

            }else{
                colorBlock_imageview.setColorFilter(ContextCompat.getColor(SingleInspectionActivity.this,R.color.yellow));
            }
            severityMessage_view.setText(violation.getStatus());


            return itemview;
        }

    }
    private void checkEmptyViolations() {
        TextView textView = findViewById(R.id.violation_emptyMessage);
        if(restaurantInspection.getViolationsList().size() == 0){
            textView.setText(R.string.no_violations_message);
            textView.setVisibility(View.VISIBLE);
        }
        else{
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void registerClickCallback() {
        ListView listView = findViewById(R.id.inspectionListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Violation violation = restaurantInspection.getViolationsList().get(position);
                Toast.makeText(SingleInspectionActivity.this,violation.getViolationDump(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //called by Restaurant Activity
    public static Intent makeIntent(Context context, int restaurantIndex, int inspectionIndex) {
        Intent intent = new Intent (context, SingleInspectionActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_INDEX,restaurantIndex);
        intent.putExtra(EXTRA_INSPECTION_INDEX,inspectionIndex);
        return intent;
    }

}

