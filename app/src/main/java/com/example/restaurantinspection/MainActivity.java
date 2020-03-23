package com.example.restaurantinspection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.Service.Feed;
import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.Service.Resource;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.restaurantinspection.model.Service.ServiceGenerator;
import com.example.restaurantinspection.model.Service.Surrey_Data_API;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://data.surrey.ca/";
    private static final String ID_RESTAURANTS = "restaurants";
    private static final String ID_INSPECTIONS = "fraser-health-restaurant-inspection-reports";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";
    public static final String TAG = "MainActivity";

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private ArrayAdapter<Restaurant> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readRestaurantData();
//        readDownloadedInspectionData();

        ///////////////////////////////////
        // TODO: testing work on using retrofit
        File file_restaurant = getBaseContext().getFileStreamPath(RESTAURANTS_FILE_NAME);
        File file_inspections = getBaseContext().getFileStreamPath(INSPECTIONS_FILE_NAME);
//        if(file_restaurant.exists() && file_inspections.exists()){
//            Log.d("TEST", "ALREADY EXISITS");
//            loadFileData();
//        }else{
//            checkForUpdates();
//        }
//        loadFileData();
        ///////////////////////////////////
//        startActivity(RequireDownloadActivity.makeIntent(this));

        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }
//        if(CompareTime())
//        {
//            ShowUpdateDialog();
//        }
//        startActivity(new Intent(this, MapsActivity.class));

        startActivity(new Intent(this, MapsActivity.class));

        loadRestaurants();
        registerClickFeedback();
        setupMagicButton();
        setUpMapButton();
        // does the downloading
//        checkForUpdates();
    }
    public boolean CompareTime()
    {
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("user", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        String LastModifiedTime = LastModifiedTimeFile.getString("Last_Modified_time",DefaultTime);
        Log.d("Time",LastModifiedTime);
        String CurrentTime = "2020-03-18T08:18:00.000000";
        if(!LastModifiedTime.equals(CurrentTime))
        {
            SharedPreferences.Editor editor = LastModifiedTimeFile.edit();
            editor.putString("Last_Modified_time",CurrentTime);
            editor.apply();
            LastModifiedTime = LastModifiedTimeFile.getString("Last_Modified_time",DefaultTime);
            Log.d("Time",LastModifiedTime+" has changed");
            return true;
        }
        return false;

    }

    public static class UpdateDialog extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder UpdateDialog = new AlertDialog.Builder(getActivity());
            UpdateDialog.setMessage(R.string.Update).setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Log.d("TAG", "onClick: update the information");
                }
            })
            .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("TAG", "onClick: did nothing");
                }
            });
            return UpdateDialog.create();
        }
    }

    public void ShowUpdateDialog()
    {
        DialogFragment showUpdateDialog = new UpdateDialog();
        showUpdateDialog.show(getSupportFragmentManager(),"update");
    }



    private void setupMagicButton() {
        Button btn = findViewById(R.id.btn_makeDownload);
        btn.setOnClickListener(v -> {
            restaurantManager.getRestaurantList().sort(new RestaurantComparator());
            for (Restaurant restaurant : restaurantManager) {
                Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
                loadRestaurants();
            }
        });
    }

    private void setUpMapButton() {

        //start MapActivity
        FloatingActionButton fab = findViewById(R.id.mapButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsActivity.class));
            }
        });
    }


    private void loadFileData() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                loadFile(RESTAURANTS_FILE_NAME);
                loadFile(INSPECTIONS_FILE_NAME);
                return null;
            }
        }.execute();
    }

    private void loadFile(String filename) {
        FileInputStream fileInputStream = null;


        int lines_read = 0;
        try {
            fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = "";
            lines_read = 0;

            reader.readLine();

            while ((line = reader.readLine()) != null) {
                lines_read++;

                String[] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6]);

                restaurantManager.add(sample);
                Log.d("NEW MANAGER : ", sample.toString());

                Log.d("LOAD", line);
            }
            int count = 0;
            for(Restaurant restaurant: restaurantManager){
                count++;
                Log.d("LISTING", restaurant.toString());
            }
            Log.d("LISTING","final count: "+count);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d("RESULT: ", "finally null, lines read: " + lines_read);
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void checkForUpdates() {
        fetchPackages(ID_RESTAURANTS);
    }

    private void fetchPackages(String typeID) {

        Surrey_Data_API surrey_data_api = ServiceGenerator.createService(Surrey_Data_API.class);
        Call<Feed> call = surrey_data_api.getData(typeID);
        ExtractInfo(call, typeID);

    }

    private void ExtractInfo(Call<Feed> Filetype, String type) {
        Filetype.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                Log.d(TAG, "onResponse: Server Response" + response.toString());
                Log.d(TAG, "onResponse: received information: " + response.body().toString());

                ArrayList<Resource> ResourceList = response.body().getResult().getResources();
                // UI STUFF
                String format = ResourceList.get(0).getFormat();
                String url = ResourceList.get(0).getUrl();
                String date_last_modified = ResourceList.get(0).getDate_last_modified();
                //TODO: DOWNLOAD THE URL DATA IF DATE COMPARISON > 20 HOURS
                if(type.equalsIgnoreCase(ID_RESTAURANTS)){
                    // Todo check time here;
                    if(true){
                        //startActivity(RequireDownloadActivity.makeIntent(MainActivity.this));
                    }
                    fetchPackages(ID_INSPECTIONS);
                } else if (type.equalsIgnoreCase(ID_INSPECTIONS)){
                    // Todo check time here;
                    if(true){
                        startActivity(RequireDownloadActivity.makeIntent(MainActivity.this));
                    }
                }
                // if it reaches here load whatever is in local storage
/*                // END OF UI STUFF
                Log.d(TAG, "I got the url : " + url);
                if (type.equalsIgnoreCase(ID_INSPECTIONS)) {
                    downloadFile(url, INSPECTIONS_FILE_NAME);
                } else {
                    downloadFile(url, RESTAURANTS_FILE_NAME);
                }*/
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, "something went wrong " + t.getMessage());
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }







    private class CustomListAdapter extends ArrayAdapter<Restaurant> {
        public CustomListAdapter() {
            super(MainActivity.this, R.layout.restaurantlistlayout, restaurantManager.getRestaurantList());
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {

            View itemView = view;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurantlistlayout, viewGroup, false);
            }

            Restaurant restaurant = restaurantManager.getRestaurantList().get(position);

            ImageView imageView = itemView.findViewById(R.id.restaurantIcon);
            TextView addressText = itemView.findViewById(R.id.restaurantLocation);
            TextView descriptionText = itemView.findViewById(R.id.restaurantDescription);
            TextView reportText = itemView.findViewById(R.id.restaurantRecentReport);
            ProgressBar hazardRating = itemView.findViewById(R.id.hazardRatingBar);

            imageView.setImageResource(R.drawable.food);
            addressText.setText(restaurant.getAddress());
            descriptionText.setText(restaurant.getName());

            //make sure they have an inspection report available
            if (restaurant.getRestaurantInspectionList().size() > 0) {

                RestaurantInspection restaurantInspection = restaurant.getRestaurantInspectionList().get(0);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date inspectionDate = null;

                try {
                    inspectionDate = simpleDateFormat.parse(restaurantInspection.getInspectionDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int issuesFound = restaurantInspection.getNumCritical() + restaurantInspection.getNumNonCritical();

                determineHazardLevel(hazardRating, restaurantInspection.getHazardRating(), issuesFound);

                String formattedInspectionDate = DateManager.formatDateInspection(inspectionDate);
                String reportMsg = "Most Recent Report: " + formattedInspectionDate + "\n";
                reportMsg += issuesFound + " issues found";

                int icon = determineIcon(restaurant.getName());

                imageView.setImageResource(icon);
                descriptionText.setText(restaurant.getName());
                reportText.setText(reportMsg);

            } else {

                reportText.setText(R.string.noavailablereports);
                hazardRating.setVisibility(View.INVISIBLE);
            }
            return itemView;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void determineHazardLevel(ProgressBar progressBar, String hazardLevel, int totalViolations) {

        if (hazardLevel.equalsIgnoreCase("LOW")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(75, 194, 54)));

        } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 158, 66)));

        } else if (hazardLevel.equalsIgnoreCase("HIGH")) {
            progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(245, 66, 66)));
        }
        progressBar.setMax(100);
        progressBar.setProgress(5 + 10 * totalViolations);
    }

    private int determineIcon(String restaurantName){

        if(restaurantName.toLowerCase().contains("pizza")){
            return R.drawable.pizza;
        } else if (restaurantName.toLowerCase().contains("burger") ||
                    restaurantName.toLowerCase().contains("a&w")){
            return R.drawable.burger;
        } else if(restaurantName.toLowerCase().contains("sushi")){
            return R.drawable.sushi;
        } else if(restaurantName.toLowerCase().contains("subway") ||
                    restaurantName.toLowerCase().contains("sandwich")){
            return R.drawable.sandwich;
        } else if(restaurantName.toLowerCase().contains("coffee") ||
                    restaurantName.toLowerCase().contains("tim hortons") ||
                    restaurantName.toLowerCase().contains("startbucks")){
            return R.drawable.coffee;
        } else if(restaurantName.toLowerCase().contains("chicken")){
            return R.drawable.chicken;
        } else if(restaurantName.toLowerCase().contains("seafood")){
            return R.drawable.lobster;
        } else if(restaurantName.toLowerCase().contains("mcdonalds")){
            return R.drawable.mcdonalds;
        } else if(restaurantName.toLowerCase().contains("taco")){
            return R.drawable.taco;
        } else if(restaurantName.toLowerCase().contains("noodles") ||
                    restaurantName.toLowerCase().contains("ramen") ||
                        restaurantName.toLowerCase().contains("pho")){
            return R.drawable.noodles;
        }

        //default
        return R.drawable.food;

    }

    private void registerClickFeedback() {

        ListView listView = findViewById(R.id.restaurantListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Restaurant restaurant = restaurantManager.getRestaurantList().get(position);
                Toast.makeText(MainActivity.this, "You clicked " + restaurant.getName(), Toast.LENGTH_SHORT).show();

                //start restaurant activity
                Intent intent = RestaurantActivity.makeIntent(MainActivity.this, position);
                startActivity(intent);
            }
        });
    }

    private void readRestaurantData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );

        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                // Split line by ','
                String[] tokens = line.split(",");
                Restaurant sample = new Restaurant(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6]);

                restaurantManager.add(sample);
            }
        } catch (IOException e) {
            Log.wtf(MAIN_ACTIVITY_TAG, "Error reading data file on line" + line, e);
        }
    }

    private void readDownloadedInspectionData() {
        InputStream is = getResources().openRawResource(R.raw.fraserhealthrestaurantinspectionreports);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        String line = "";
        try {
            // Step over headers
            reader.readLine();
            while ((!(line = reader.readLine()).equals(",,,,,,"))||((line = reader.readLine()) != null)) {
                // Split line by ','
                Log.d("TEST", line);
                String[] parts = line.split("\"");
                String[] tokens = parts[0].split(",");
                String var_token5;
                String var_token6 = "Low";
                String ViolationDump;
                if (parts.length==3) {
                    ViolationDump = parts[1].replace(",","!");
                    var_token5 = ViolationDump;
                    var_token6 = parts[2].replace(","," ").trim();
                } else {
                    var_token5 = "No violations";
                }


                RestaurantInspection sample = new RestaurantInspection(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        var_token5, var_token6);
                Log.d("MY_ACTIVITY", sample.getTrackingNumber() + " " + sample.getInspectionDate()+" "+sample.getHazardRating());
                for (Restaurant restaurant : restaurantManager) {
                    if (sample.getTrackingNumber().equalsIgnoreCase(restaurant.getTrackingNumber())) {
                        restaurant.getRestaurantInspectionList().add(sample);
                    }
                }
            }
        } catch (IOException e) {
            Log.wtf(MAIN_ACTIVITY_TAG, "Error reading data file on line" + line, e);
        }
    }

    public void loadRestaurants() {
        arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        arrayAdapter.notifyDataSetChanged();
//        restaurantManager = RestaurantManager.getInstance();;
        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }
    }
}
