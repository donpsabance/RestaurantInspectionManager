package com.example.restaurantinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.restaurantinspection.model.InspectionComparator;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantComparator;
import com.example.restaurantinspection.model.RestaurantInspection;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Service.CheckInternet;
import com.example.restaurantinspection.model.Service.Feed;
import com.example.restaurantinspection.model.Service.FileDownloadClient;
import com.example.restaurantinspection.model.Service.Resource;
import com.example.restaurantinspection.model.Service.ServiceGenerator;
import com.example.restaurantinspection.model.Service.Surrey_Data_API;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
    public static final String TAG_CHECK = "MainActivity";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String NEW_RESTAURANTS_FILE_NAME = "New_downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";
    private static final String NEW_INSPECTIONS_FILE_NAME = "New_downloaded_Inspections.csv";
    private RestaurantManager restaurantManager;

    private Button btnStartDownload;
    private Button btnLoadFromStorage;
    private TextView textview_want_downloadMsg;
    private ProgressDialog progressDialog;
    private boolean cancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_download);
        restaurantManager = RestaurantManager.getInstance();
        restaurantManager.setExtraDataLoaded(true);
        setViews();
        registerClickCallback();

        if(CheckInternet.getConnectionType(this)){
            check_For_Updates(ID_RESTAURANTS);
        }else{
            justLoadWhateverInStorage();
        }
        deleteFile(NEW_RESTAURANTS_FILE_NAME);
        deleteFile(NEW_INSPECTIONS_FILE_NAME);
    }

    private void DeleteFile(String FileName)
    {
        File file = new File(FileName);
        if(file.isFile())
        {
            deleteFile(FileName);
            Log.d("Delete","delete "+FileName+"successful");
        }
    }

    private void renameFile(String old,String New)
    {
        File oldFile = new File("/data/data/com.example.restaurantinspection/files/"+old);
        String oldPath = oldFile.getAbsolutePath();
        oldFile = new File(oldPath);
        String newPath = oldPath.replace(old,New);
        File newFile = new File(newPath);
        if(oldFile.renameTo(newFile)) {
            Log.d("RENAME", "yes!!!!!!!!!!!!");
        }
        else{
            Log.d("RENAME", "NOOOOOOOOOO!!!!!!!!!!!!");
        }
    }






    private void justLoadWhateverInStorage() {
        LoadingDialog();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                LoadingDialog();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                file_read_FromDownloadedRestaurants(RESTAURANTS_FILE_NAME);
                file_read_FromDownloadedInspections(INSPECTIONS_FILE_NAME);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                terminateActivity();
            }
        }.execute();
    }

    private void terminateActivity() {
        Intent i = new Intent();
        Log.d("CHECK","SENDING INTENT");
        setResult(RESULT_OK,i);
        finish();
    }

    private void DownloadingDialog(Thread thread)
    {
        progressDialog = new ProgressDialog(RequireDownloadActivity.this);
        progressDialog.setTitle("Please Wait~~");
        progressDialog.setMessage("Downloading");
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                thread.interrupt();
                deleteFile(NEW_RESTAURANTS_FILE_NAME);
                deleteFile(NEW_INSPECTIONS_FILE_NAME);
                cancel(true);

            }
        });
        progressDialog.show();
    }
    private void cancel(boolean bool) {
        if(bool) {
            cancelled = true;
        }else{
            cancelled = false;
        }

    }
    private void LoadingDialog()
    {
        progressDialog = new ProgressDialog(RequireDownloadActivity.this);
        progressDialog.setTitle("Please Wait~~");
        progressDialog.setMessage("Loading~~~~");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

    }
        Handler mHandler= new Handler(){
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == 1){
                mHandler.post(runnable);
            }else{
                justLoadWhateverInStorage();
            }

        }
    };

     Runnable runnable = () -> {
         renameFile(NEW_RESTAURANTS_FILE_NAME,RESTAURANTS_FILE_NAME);
         renameFile(NEW_INSPECTIONS_FILE_NAME,INSPECTIONS_FILE_NAME);
         String date_last_modified = ReadWebTime();
         WriteUserTime(date_last_modified);
         mHandler.sendEmptyMessage(0);

     };
    private void registerClickCallback() {
        btnStartDownload.setOnClickListener(v -> {
            // starts download for restaurants then for inspections
            cancel(false);
            Thread myThread = new Thread();
            DownloadingDialog(myThread);
            new Thread(myThread)
            {
                @Override
                public void run(){
                    super.run();
                    fetchPackages(ID_RESTAURANTS);
                    try {
                        myThread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!cancelled) {
                        progressDialog.dismiss();
                        Message message = new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                }
            }.start();

        });

        btnLoadFromStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justLoadWhateverInStorage();
            }
        });
    }



    private void check_For_Updates(String typeID) {
        Surrey_Data_API surrey_data_api = ServiceGenerator.createService(Surrey_Data_API.class);
        Call<Feed> call = surrey_data_api.getData(typeID);
        // checks whether restaurants or inspections csv requires an update
        ExtractInfo2_checkForUpdate(call, typeID);
    }

    private void ExtractInfo2_checkForUpdate(Call<Feed> Filetype, String type) {
        Filetype.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {

                //extracts the needed information from web response
                ArrayList<Resource> ResourceList = response.body().getResult().getResources();

                String date_last_modified = ResourceList.get(0).getDate_last_modified();

                // show display to download csv files if time difference greater than 20 hours
                if (type.equalsIgnoreCase(ID_RESTAURANTS)) {
                    if (CompareTime_to_mostRecendtlyDownloaded(date_last_modified)) {
                        setVisibilities(View.VISIBLE);
                        return;
                    }
                    check_For_Updates(ID_INSPECTIONS);
                    return;
                } else if (type.equalsIgnoreCase(ID_INSPECTIONS)) {
                    if (CompareTime_to_mostRecendtlyDownloaded(date_last_modified)) {

                        setVisibilities(View.VISIBLE);
                        return;
                    }
                }
                // if it reaches here load whatever is in local storage
                justLoadWhateverInStorage();

            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Toast.makeText(RequireDownloadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
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

                ArrayList<Resource> ResourceList = response.body().getResult().getResources();


                String url = ResourceList.get(0).getUrl();
                String date_last_modified = ResourceList.get(0).getDate_last_modified();
                WriteWebTime(date_last_modified);

                if (type.equalsIgnoreCase(ID_RESTAURANTS)) {
                    downloadFile(url, NEW_RESTAURANTS_FILE_NAME);
                    fetchPackages(ID_INSPECTIONS);
                } else {
                    downloadFile(url, NEW_INSPECTIONS_FILE_NAME);
                }
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG_CHECK, "something went wrong " + t.getMessage());
                Toast.makeText(RequireDownloadActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(String url, String filename) {
        // create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL).build();

        FileDownloadClient fileDownloadClient = retrofit.create(FileDownloadClient.class);
        Call<ResponseBody> call = fileDownloadClient.downloadFileStream(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //write the binary file to the disk
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected Void doInBackground(Void... voids) {
                        writeToFile(response.body(), filename);
                        return null;
                    }
                }.execute();

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

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(filname.equalsIgnoreCase(INSPECTIONS_FILE_NAME)){
                    terminateActivity();
                }
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

            }
            int count = 0;
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

        HashMap<String,Restaurant> hmap = new HashMap<>();
        for(Restaurant r : restaurantManager){
            hmap.put(r.getTrackingNumber(),r);
        }

        FileInputStream fileInputStream = null;
        String line = "";
        try {
            fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            // Step over headers
            reader.readLine();
            while (((line = reader.readLine()) != null) && (!line.equals(",,,,,,"))) {
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
                if(hmap.containsKey(sample.getTrackingNumber())){
                    hmap.get(sample.getTrackingNumber()).getRestaurantInspectionList().add(sample);
                }
            }
        } catch (IOException e) {
            Log.wtf("RESULT", "Error reading data file on line" + line, e);
        }   finally{
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean CompareTime_to_mostRecendtlyDownloaded(String data_last_modified_web)
    {
        String UserLastModifiedTime = ReadUserTime();
        Log.d("Time",UserLastModifiedTime);
        UserLastModifiedTime = TakeDataTime(UserLastModifiedTime);
        String WebLastModifiedTime = TakeDataTime(data_last_modified_web);
        return GetHourDifference(WebLastModifiedTime, UserLastModifiedTime) > 200000;
    }

    private String ReadUserTime(){
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("Time", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        return LastModifiedTimeFile.getString("User_Last_Modified_time",DefaultTime);
    }

    private String ReadWebTime(){
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("Time", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        return LastModifiedTimeFile.getString("Web_Last_Modified_time",DefaultTime);
    }

    private void WriteUserTime(String date){
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("Time", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        SharedPreferences.Editor editor = LastModifiedTimeFile.edit();
        editor.putString("User_Last_Modified_time",date);
        editor.apply();
    }

    private void WriteWebTime(String date){
        SharedPreferences LastModifiedTimeFile = getSharedPreferences("Time", Context.MODE_PRIVATE);
        String DefaultTime = getResources().getString(R.string.default_time);
        SharedPreferences.Editor editor = LastModifiedTimeFile.edit();
        editor.putString("Web_Last_Modified_time",date);
        editor.apply();
    }

    private String TakeDataTime(String date){
        if(date != null)
        {
            date = date.replaceAll("[^0-9]","").trim();
            date = date.substring(0,14);
        }
        return date;
    }

    private double GetHourDifference(String date1, String date2) {
        BigDecimal Date1 = new BigDecimal(date1);
        BigDecimal Date2 = new BigDecimal(date2);
        return (Date1.subtract(Date2).doubleValue()+1.0);
    }

    private void setViews() {
        btnStartDownload = findViewById(R.id.btn_download_from_web);
        btnLoadFromStorage = findViewById(R.id.btn_load_from_storage);
        textview_want_downloadMsg = findViewById(R.id.txt_newDownloadAvailable);
    }
    private void setVisibilities(int visibility){
        btnStartDownload.setVisibility(visibility);
        btnLoadFromStorage.setVisibility(visibility);
        textview_want_downloadMsg.setVisibility(visibility);

    }

    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, RequireDownloadActivity.class);
        return intent;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressDialog.dismiss();
    }

}
