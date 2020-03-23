package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Service.Feed;
import com.example.restaurantinspection.model.Service.FileDownloadClient;
import com.example.restaurantinspection.model.Service.Resource;
import com.example.restaurantinspection.model.Service.ServiceGenerator;
import com.example.restaurantinspection.model.Service.Surrey_Data_API;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RequireDownloadActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://data.surrey.ca/";
    private static final String ID_RESTAURANTS = "restaurants";
    private static final String ID_INSPECTIONS = "fraser-health-restaurant-inspection-reports";
    public static final String FLOOFLOO = "MainActivity";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";
    private RestaurantManager restaurantManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_download);
        restaurantManager = RestaurantManager.getInstance();
        registerClickCallback();
    }

    private void registerClickCallback() {
        Button btn = findViewById(R.id.btn_load);
        btn.setOnClickListener(v -> {
            fetchPackages(ID_RESTAURANTS);
        });
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
                Log.d(FLOOFLOO, "onResponse: Server Response" + response.toString());
                Log.d(FLOOFLOO, "onResponse: received information: " + response.body().toString());

                ArrayList<Resource> ResourceList = response.body().getResult().getResources();
                // UI STUFF
                String format = ResourceList.get(0).getFormat();
                String url = ResourceList.get(0).getUrl();
                String date_last_modified = ResourceList.get(0).getDate_last_modified();

                // END OF UI STUFF
                //TODO: DOWNLOAD THE URL DATA IF DATE COMPARISON > 20 HOURS
                Log.d(FLOOFLOO, "I got the url : " + url);
                if (type.equalsIgnoreCase(ID_RESTAURANTS)) {
                    downloadFile(url, RESTAURANTS_FILE_NAME);
                    fetchPackages(ID_INSPECTIONS);
                } else {
                    downloadFile(url, INSPECTIONS_FILE_NAME);
                }
                // url_txt.setText(url);
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(FLOOFLOO, "something went wrong " + t.getMessage());
                Toast.makeText(RequireDownloadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(String url, String filename) {
        // create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL).build();

        FileDownloadClient fileDownloadClient = retrofit.create(FileDownloadClient.class);
        Call<ResponseBody> call = fileDownloadClient.downloadFile(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //write the binary file to the disk
                writeToFile(response.body(), filename);
                Toast.makeText(RequireDownloadActivity.this, "success! :)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RequireDownloadActivity.this, "Failed :(", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private boolean writeToFile(ResponseBody body, String filename) {

        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = openFileOutput(filename, MODE_PRIVATE);

            byte[] fileReader = new byte[4096];

            inputStream = body.byteStream();

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }
                fileOutputStream.write(fileReader, 0, read);
            }
            Toast.makeText(this, "Wrote to " + getFilesDir() + "/" + filename, Toast.LENGTH_LONG).show();
            fileOutputStream.flush();
            startLoading(filename);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private void startLoading(String filname) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                loadFile(filname);
                return null;
            }
        }.execute();
    }


    private void loadFile(String filename) {
        if (filename.equalsIgnoreCase(RESTAURANTS_FILE_NAME)) {
            file_read_FromDownloadedRestaurants(filename);
        } else if (filename.equalsIgnoreCase(INSPECTIONS_FILE_NAME)) {
            file_read_FromDownloadedInspections(filename);
        }
    }

    private void file_read_FromDownloadedRestaurants(String filename) {
        FileInputStream fileInputStream = null;
        HashMap<String, Restaurant> hmap = new HashMap<>();
        for (Restaurant restaurant : restaurantManager) {
            hmap.put(restaurant.getTrackingNumber(), restaurant);
        }

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
                if(tokens.length == 8){
                    tokens[1] = tokens[1]+", "+tokens[2];
                    for (int i = 2; i < 7; i++)
                    {
                        tokens[i]=tokens[i+1];
                    }
                }
                Restaurant sample = new Restaurant(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6]);

                if (!hmap.containsKey(tokens[0])) {
                    restaurantManager.add(sample);
                }

                Log.d("NEW MANAGER : ", sample.toString());

                Log.d("LOAD", line);
            }
            int count = 0;
            for (Restaurant restaurant : restaurantManager) {
                count++;
                Log.d("LISTING", restaurant.toString());
            }
            Log.d("LISTING", "final count: " + count);


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

    private void file_read_FromDownloadedInspections(String filename) {
        FileInputStream fileInputStream = null;

        String line = "";
        try {
            fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            // Step over headers
            reader.readLine();
            while ((!(line = reader.readLine()).equals(",,,,,,")) || ((line = reader.readLine()) != null)) {
                // Split line by ','
                Log.d("TEST", line);
                String[] parts = line.split("\"");
                String[] tokens = parts[0].split(",");
                String var_token5;
                String var_token6 = "Low";
                String ViolationDump;
                if (parts.length == 3) {
                    ViolationDump = parts[1].replace(",", "!");
                    var_token5 = ViolationDump;
                    var_token6 = parts[2].replace(",", " ").trim();
                } else {
                    var_token5 = "No violations";
                }

                RestaurantInspection sample = new RestaurantInspection(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        var_token5, var_token6);

                Log.d("NEW MANAGER", sample.getTrackingNumber() + " " + sample.getInspectionDate() + " " + sample.getHazardRating());
                for (Restaurant restaurant : restaurantManager) {
                    if (sample.getTrackingNumber().equalsIgnoreCase(restaurant.getTrackingNumber())) {
                        restaurant.getRestaurantInspectionList().add(sample);
                    }
                }
            }
        } catch (IOException e) {
            Log.wtf("RESULT", "Error reading data file on line" + line, e);
        }
    }


    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, RequireDownloadActivity.class);
        return intent;
    }
}
