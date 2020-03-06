package com.example.restaurantinspection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.Violation;
import com.example.restaurantinspection.model.ViolationsManager;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SingleInspectionActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "com.example.restaurantinspection: date";
    public static final String EXTRA_NONCRITICAL = "com.example.restaurantinspection: nonCritical";
    public static final String EXTRA_CRITICAL = "com.example.restaurantinspection critical";
    public static final String EXTRA_HZDRATING = "com.example.restaurantinspection: hazardLVL";
    public static final String EXTRA_INSPECTION_TYPE = "com.example.restaurantinspection: inspection_type";
    public static final String EXTRA_VIOLATION_DUMP = "com.example.restaurantinspection: violationDump";

    private ViolationsManager violationsManager = new ViolationsManager();

    private String inspectionDate;
    private String violations;
    private String inspectiontype;
    private String trackingNumber;
    private String hazardLVL;
    private int numNonCritical;
    private int numCritical;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_inspection);
        getFromExtra();
        updateTextUI();

        buildViolationManager();
        loadViolations();

    }

    private void buildViolationManager() {
        String [] arr = violations.split("\\|");
        for(String s : arr){
            Log.d("TAG",s);
            Violation violation = new Violation(s);
            violationsManager.add(violation);
        }
    }

    private void getFromExtra() {
        Intent intent = getIntent();
        inspectionDate = intent.getStringExtra(EXTRA_DATE);
        inspectiontype = intent.getStringExtra(EXTRA_INSPECTION_TYPE);
        hazardLVL = intent.getStringExtra(EXTRA_HZDRATING);
        violations = intent.getStringExtra(EXTRA_VIOLATION_DUMP);
        numNonCritical = intent.getIntExtra(EXTRA_NONCRITICAL,0);
        numCritical = intent.getIntExtra(EXTRA_CRITICAL,0);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateTextUI() {

        TextView inspectionTypeView = findViewById(R.id.inspectionType);
        inspectionTypeView.setText("Inspection type: " + inspectiontype);

        TextView inspectionDateView = findViewById(R.id.inspectionDate);
        inspectionDateView.setText("Date: " + formatDate(inspectionDate));

        TextView hazardResultView = findViewById(R.id.HazardResult);
        hazardResultView.setText(hazardLVL);

        TextView numCriticalView = findViewById(R.id.numCriticalmsg);
        numCriticalView.setText("Critical issues: " + numCritical);

        TextView numNonCriticalView = findViewById(R.id.numNonCriticalmsg);
        numNonCriticalView.setText("Non critical issues: " + numNonCritical);

        ProgressBar progressBar = findViewById(R.id.hazardBarSingle);
        TextView hazardMessageView = findViewById(R.id.HazardResult);
        determineHazardLevel(progressBar,hazardLVL,hazardMessageView);
        hazardMessageView.setText(hazardLVL);


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
            super(SingleInspectionActivity.this,R.layout.list_violations_layout,violationsManager.getViolationList());
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemview = view;
            if(itemview == null){
                itemview = getLayoutInflater().inflate(R.layout.list_violations_layout,viewGroup,false);
            }
            //TODO use violation data to create listview
            ImageView imageView = itemview.findViewById(R.id.violationIcon);
            imageView.setImageResource(R.drawable.food);

            return itemview;
//            return super.getView(position, convertView, parent);
        }
    }

    //called by Restaurant Activity

    public static Intent makeIntent(Context context, RestaurantInspection restaurantInspection) {
        Intent intent = new Intent (context, SingleInspectionActivity.class);
        intent.putExtra(EXTRA_DATE,restaurantInspection.getInspectionDate());
        intent.putExtra(EXTRA_INSPECTION_TYPE,restaurantInspection.getInspectionType());
        intent.putExtra(EXTRA_NONCRITICAL,restaurantInspection.getNumNonCritical());
        intent.putExtra(EXTRA_CRITICAL,restaurantInspection.getNumCritical());
        intent.putExtra(EXTRA_HZDRATING,restaurantInspection.getHazardRating());
        intent.putExtra(EXTRA_VIOLATION_DUMP, restaurantInspection.getViolations());
        return intent;
    }

}

