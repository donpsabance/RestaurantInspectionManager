package com.example.restaurantinspection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
    public boolean onOptionsItemSelected(MenuItem item) {
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
        String message = "";

        String inspectionType = restaurantInspection.getInspectionType();
        TextView inspectionTypeView = findViewById(R.id.inspectionType);

        if(inspectionType.equalsIgnoreCase("Routine")) {
            message = getString(R.string.inspection_type) + " " + getString(R.string.routine);
        }
        if(inspectionType.equalsIgnoreCase("Follow-Up")){
            message = getString(R.string.inspection_type) + " " + getString(R.string.follow_up);
        }


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



        ProgressBar progressBar = findViewById(R.id.hazardBarSingle);
        determineHazardLevel(progressBar, restaurantInspection.getHazardRating(), hazardResultView);
        String hazardRating = restaurantInspection.getHazardRating();
        if (hazardRating.equalsIgnoreCase("LOW")){
            hazardResultView.setText(R.string.low);
        }
        if (hazardRating.equalsIgnoreCase("MODERATE")){
            hazardResultView.setText(R.string.moderate);
        }
        if (hazardRating.equalsIgnoreCase("HIGH")){
            hazardResultView.setText(R.string.high);
        }


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
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel, TextView hazardTextView) {

        if (hazardLevel.equalsIgnoreCase("LOW")) {
            progressBar.setProgress(30);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));
            hazardTextView.setTextColor(Color.rgb(75, 194, 54));

        } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {

            progressBar.setProgress(60);
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));
            hazardTextView.setTextColor(Color.rgb(245, 158, 66));

        } else if (hazardLevel.equalsIgnoreCase("HIGH")) {

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

    private class CustomListAdapter extends ArrayAdapter<Violation> {

        public CustomListAdapter() {
            super(SingleInspectionActivity.this, R.layout.list_violations_layout, restaurantInspection.getViolationsList());
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemview = view;
            if (itemview == null) {
                itemview = getLayoutInflater().inflate(R.layout.list_violations_layout, viewGroup, false);
            }
            ImageView imageView = itemview.findViewById(R.id.violationIcon);
            TextView briefMessage_view = itemview.findViewById(R.id.violationDescription);
            TextView severityMessage_view = itemview.findViewById(R.id.violationSeverityTxt);
            ImageView colorBlock_imageview = itemview.findViewById(R.id.hazardColor);


            Violation violation = restaurantInspection.getViolationsList().get(position);
            //TODO use violation data to create listview

            int id_num = violation.getViolation_id();

            if (id_num >= 100 && id_num < 200) {
                imageView.setImageResource(R.drawable.permits);
                briefMessage_view.setText(R.string.violation_100_199);
            } else if (id_num >= 200 && id_num < 300) {
                imageView.setImageResource(R.drawable.food_safety);
                briefMessage_view.setText(R.string.violation_200_299);
            } else if (id_num >= 300 && id_num < 400) {
                imageView.setImageResource(R.drawable.equipment_cleanliness);
                briefMessage_view.setText(R.string.violation_300_399);
            } else if (id_num >= 400 && id_num < 500) {
                imageView.setImageResource(R.drawable.wash_hands);
                briefMessage_view.setText(R.string.violation_400_499);
            } else if (id_num >= 500 && id_num < 600) {
                imageView.setImageResource(R.drawable.food_safe);
                briefMessage_view.setText(R.string.violation_500_599);
            }

            if (violation.getStatus().equalsIgnoreCase("Critical")) {
                colorBlock_imageview.setColorFilter(ContextCompat.getColor(SingleInspectionActivity.this, R.color.colorAccent));
                severityMessage_view.setText(R.string.critical);

            } else {
                colorBlock_imageview.setColorFilter(ContextCompat.getColor(SingleInspectionActivity.this, R.color.yellow));
                severityMessage_view.setText(R.string.not_critical);
            }

            return itemview;
        }

    }

    private void checkEmptyViolations() {
        TextView textView = findViewById(R.id.violation_emptyMessage);
        if (restaurantInspection.getViolationsList().size() == 0) {
            textView.setText(R.string.no_violations_message);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void registerClickCallback() {
        ListView listView = findViewById(R.id.inspectionListView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Violation violation = restaurantInspection.getViolationsList().get(position);
            switch(violation.getViolation_id()){
                case 101:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V101, Toast.LENGTH_SHORT).show();
                    break;
                case 102:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V102, Toast.LENGTH_SHORT).show();
                    break;
                case 103:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V103, Toast.LENGTH_SHORT).show();
                    break;
                case 104:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V104, Toast.LENGTH_SHORT).show();
                    break;
                case 201:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V201, Toast.LENGTH_SHORT).show();
                    break;
                case 202:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V202, Toast.LENGTH_SHORT).show();
                    break;
                case 203:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V203, Toast.LENGTH_SHORT).show();
                    break;
                case 204:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V204, Toast.LENGTH_SHORT).show();
                    break;
                case 205:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V205, Toast.LENGTH_SHORT).show();
                    break;
                case 206:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V206, Toast.LENGTH_SHORT).show();
                    break;
                case 208:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V208, Toast.LENGTH_SHORT).show();
                    break;
                case 209:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V209, Toast.LENGTH_SHORT).show();
                    break;
                case 210:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V210, Toast.LENGTH_SHORT).show();
                    break;
                case 211:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V211, Toast.LENGTH_SHORT).show();
                    break;
                case 212:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V212, Toast.LENGTH_SHORT).show();
                    break;
                case 301:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V301, Toast.LENGTH_SHORT).show();
                    break;
                case 302:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V302, Toast.LENGTH_SHORT).show();
                    break;
                case 303:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V303, Toast.LENGTH_SHORT).show();
                    break;
                case 304:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V304, Toast.LENGTH_SHORT).show();
                    break;
                case 305:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V305, Toast.LENGTH_SHORT).show();
                    break;
                case 306:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V306, Toast.LENGTH_SHORT).show();
                    break;
                case 307:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V307, Toast.LENGTH_SHORT).show();
                    break;
                case 308:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V308, Toast.LENGTH_SHORT).show();
                    break;
                case 309:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V309, Toast.LENGTH_SHORT).show();
                    break;
                case 310:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V310, Toast.LENGTH_SHORT).show();
                    break;
                case 311:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V311, Toast.LENGTH_SHORT).show();
                    break;
                case 312:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V312, Toast.LENGTH_SHORT).show();
                    break;
                case 313:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V313, Toast.LENGTH_SHORT).show();
                    break;
                case 314:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V314, Toast.LENGTH_SHORT).show();
                    break;
                case 315:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V315, Toast.LENGTH_SHORT).show();
                    break;
                case 401:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V401, Toast.LENGTH_SHORT).show();
                    break;
                case 402:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V402, Toast.LENGTH_SHORT).show();
                    break;
                case 403:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V403, Toast.LENGTH_SHORT).show();
                    break;
                case 404:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V404, Toast.LENGTH_SHORT).show();
                    break;
                case 501:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V501, Toast.LENGTH_SHORT).show();
                    break;
                case 502:
                    Toast.makeText(SingleInspectionActivity.this,R.string.V502, Toast.LENGTH_SHORT).show();
                    break;
            }

            //Toast.makeText(SingleInspectionActivity.this, violation.getViolationDump(), Toast.LENGTH_SHORT).show();
        });
    }

    //called by Restaurant Activity
    public static Intent makeIntent(Context context, int restaurantIndex, int inspectionIndex) {
        Intent intent = new Intent(context, SingleInspectionActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_INDEX, restaurantIndex);
        intent.putExtra(EXTRA_INSPECTION_INDEX, inspectionIndex);
        return intent;
    }

}

