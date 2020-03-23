package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Service.FileDownloadClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
    public static final String TAG = "MainActivity";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";
    private RestaurantManager restaurantManager;
    private String given_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_download);
         restaurantManager = RestaurantManager.getInstance();
        Intent intent = getIntent();
        given_url = intent.getStringExtra("RESTAURANT EXTRA");
        registerClickCallback();
    }

    private void registerClickCallback() {
        Button btn = findViewById(R.id.btn_load);
        btn.setOnClickListener(v -> {
            downloadFile(given_url,RESTAURANTS_FILE_NAME);
        });
    }


    private void loadFileData() {
        Log.d("LOG","Begin loading...");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                loadFile(RESTAURANTS_FILE_NAME);
//                loadFile(INSPECTIONS_FILE_NAME);
                return null;
            }
        }.execute();
    }

    private void loadFile(String filename) {
        FileInputStream fileInputStream = null;
        HashMap<String,Restaurant> hmap = new HashMap<>();
        for(Restaurant restaurant : restaurantManager){
            hmap.put(restaurant.getTrackingNumber(),restaurant);
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
                if(tokens.length == 8)
                {
                    //Log.d("Restaurant","I am here!!!!!!!!!!!!!!!!!");
                    tokens[1]=tokens[1]+", "+tokens[2];
                    for(int i = 2;i<7;i++)
                    {
                        tokens[i]=tokens[i+1];
                    }
                }
                Restaurant sample = new Restaurant(tokens[0], tokens[1],
                        tokens[2], tokens[3], tokens[4],
                        tokens[5], tokens[6]);

                if(!hmap.containsKey(tokens[0])){
                    restaurantManager.add(sample);
                }

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
            loadFileData();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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


    public static Intent makeIntent(Context context,String url) {
        Intent intent = new Intent (context, RequireDownloadActivity.class);
        intent.putExtra("RESTAURANT EXTRA",url);
        return intent;
    }
}
