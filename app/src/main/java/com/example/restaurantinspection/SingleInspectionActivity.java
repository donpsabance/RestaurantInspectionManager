package com.example.restaurantinspection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Violation;
import com.example.restaurantinspection.model.ViolationsManager;

import org.w3c.dom.Text;

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


    private ViolationsManager violationsManager = new ViolationsManager();

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

//        buildViolationManager();
        loadViolations();

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

/*    private void buildViolationManager() {
        String [] arr = restaurantInspection.getViolations().split("\\|");
        for(String s : arr){
            Log.d("TAG",s);
            Violation violation = new Violation(s);
            violationsManager.add(violation);
        }
    }*/

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

        TextView inspectionTypeView = findViewById(R.id.inspectionType);
        inspectionTypeView.setText("Inspection type: " + restaurantInspection.getInspectionType());

        TextView inspectionDateView = findViewById(R.id.inspectionDate);
        inspectionDateView.setText("Date: " + formatDate(restaurantInspection.getInspectionDate()));

        TextView hazardResultView = findViewById(R.id.HazardResult);
        hazardResultView.setText(restaurantInspection.getHazardRating());

        TextView numCriticalView = findViewById(R.id.numCriticalmsg);
        numCriticalView.setText("Critical issues: " + restaurantInspection.getNumCritical());

        TextView numNonCriticalView = findViewById(R.id.numNonCriticalmsg);
        numNonCriticalView.setText("Non critical issues: " + restaurantInspection.getNumNonCritical());

        ProgressBar progressBar = findViewById(R.id.hazardBarSingle);
        TextView hazardMessageView = findViewById(R.id.HazardResult);
        determineHazardLevel(progressBar,restaurantInspection.getHazardRating(),hazardMessageView);
        hazardMessageView.setText(restaurantInspection.getHazardRating());


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
        String result = "";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formatDate);

        result = new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH) - 1] + " " +
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


            Violation violation = restaurantInspection.getViolationsList().get(position);
            //TODO use violation data to create listview

            int id_num = violation.getViolation_id();

            if(id_num >= 100 && id_num < 200){
                imageView.setImageResource(R.drawable.food);
                briefMessage_view.setText("No qualified permit or illegal construction.");
            }else if(id_num >= 200 && id_num < 300){
                imageView.setImageResource(R.drawable.food);
                briefMessage_view.setText("Food or cooking methods or food storage is not safe.");
            }else if (id_num >= 300 && id_num < 400){
                imageView.setImageResource(R.drawable.food);
                briefMessage_view.setText("Poor equipment and work environment");
            }else if(id_num >= 400 && id_num < 500){
                imageView.setImageResource(R.drawable.food);
                briefMessage_view.setText("Staff have poor hygiene habits or conditions.");
            }else if(id_num >= 500 && id_num < 600){
                imageView.setImageResource(R.drawable.food);
                briefMessage_view.setText("Insufficient FOODSAFE LEVEL for the operator or watchkeeper.");
            }
            String id_string = Integer.toString(id_num);

            severityMessage_view.setText(violation.getStatus());
            //briefMessage_view.setText(id_string);


            return itemview;
//            return super.getView(position, convertView, parent);
        }
    }

    //called by Restaurant Activity

    public static Intent makeIntent(Context context, int restaurantIndex, int inspectionIndex) {
        Intent intent = new Intent (context, SingleInspectionActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_INDEX,restaurantIndex);
        intent.putExtra(EXTRA_INSPECTION_INDEX,inspectionIndex);
        return intent;
    }

}

