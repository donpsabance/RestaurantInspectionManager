package com.example.restaurantinspection;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.restaurantinspection.model.DateManager;
import com.example.restaurantinspection.model.retrofitdetails.Feed;
import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.retrofitdetails.Resource;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://data.surrey.ca/";
    private static final String ID_RESTAURANTS = "restaurants";
    private static final String ID_INSPECTIONS = "fraser-health-restaurant-inspection-reports";

    public static final String MAIN_ACTIVITY_TAG = "MyActivity";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///////////////////////////////////
        // TODO: testing work on using retrofit
        // PRE: current data    POST: i want to extract the url from
        checkForUpdates();
        ///////////////////////////////////
        readRestaurantData();
        readInspectionData();

        restaurantManager.getRestaurantList().sort(new RestaurantComparator());
        for (Restaurant restaurant : restaurantManager) {
            Collections.sort(restaurant.getRestaurantInspectionList(), new InspectionComparator());
        }

//        startActivity(new Intent(this, MapsActivity.class));

        loadRestaurants();
        registerClickFeedback();
    }

    private void checkForUpdates() {
        if(true/*place some condition here*/){
            
            fetchPackages(ID_RESTAURANTS);
            //fetchPackages(ID_INSPECTIONS);
        }
    }

    private void fetchPackages(String type) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Surrey_Data_API restaurantDataAPI = retrofit.create(Surrey_Data_API.class);
        Call<Feed> call = restaurantDataAPI.getData(type);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {

                ArrayList<Resource> ResourceList = response.body().getResult().getResources();
                String format = ResourceList.get(0).getFormat();
                String url = ResourceList.get(0).getUrl();
                String date_last_modified = ResourceList.get(0).getDate_last_modified();


                //TODO: DOWNLOAD THE URL DATA
                downloadFile(url);

            }


            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(MAIN_ACTIVITY_TAG, "something went wrong " + t.getMessage());
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void downloadFile(String url) {
        // create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL).build();
        FileDownloadClient fileDownloadClient = retrofit.create(FileDownloadClient.class);

        Call<ResponseBody> call = fileDownloadClient.downloadFile(url);
        Log.d(MAIN_ACTIVITY_TAG,"url = " + url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        //boolean success = writeResponseBodyToDisk(response.body());
                        return null;
                    }
                }.execute();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this,"download didn't work :( " + t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "restaurant.csv");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                Log.d(MAIN_ACTIVITY_TAG, "FILESIZE IS" + fileSize);

                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(MAIN_ACTIVITY_TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
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

                imageView.setImageResource(R.drawable.food);
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

    private void readInspectionData() {
        InputStream is = getResources().openRawResource(R.raw.inspectionsdata);
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
                String var_token6;
                if (tokens.length >= 7 && tokens[6].length() > 0) {
                    var_token6 = tokens[6];
                } else {
                    var_token6 = "No violations";
                }

                RestaurantInspection sample = new RestaurantInspection(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], var_token6);
                Log.d("MY_ACTIVITY", sample.getTrackingNumber() + " " + sample.getInspectionDate());
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

        ArrayAdapter<Restaurant> arrayAdapter = new CustomListAdapter();
        ListView listView = findViewById(R.id.restaurantListView);
        listView.setAdapter(arrayAdapter);
    }
}
