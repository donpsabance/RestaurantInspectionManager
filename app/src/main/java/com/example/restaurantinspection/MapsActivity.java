package com.example.restaurantinspection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Service.Feed;
import com.example.restaurantinspection.model.Service.Resource;
import com.example.restaurantinspection.model.Service.ServiceGenerator;
import com.example.restaurantinspection.model.Service.Surrey_Data_API;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String BASE_URL = "http://data.surrey.ca/";
    private static final String ID_RESTAURANTS = "restaurants";
    private static final String ID_INSPECTIONS = "fraser-health-restaurant-inspection-reports";
    private static final String RESTAURANTS_FILE_NAME = "downloaded_Restaurants.csv";
    private static final String INSPECTIONS_FILE_NAME = "downloaded_Inspections.csv";
    private static final String SERVER_TAG = "SERVER UPDATE: ";

    public static final String RESTAURANT_INDEX = "com.example.restaurantinspection - restaurant index";
    private final String LOG_TAG = "RESTAURANT INSPECTION: ";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private int restaurantIndex;

    private GoogleMap mMap;
    private final HashMap<Marker, Integer> mHashMap = new HashMap<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private static final int LOCATION_PERMISSION_CODE = 1234;
    private final float DEFAULT_ZOOM = 12f;
    private final long MIN_TIME = 1000;
    private final float MIN_DISTANCE = 1f;

    private boolean locationPermission = false;

    //called by Restaurant Activity
    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        return intent;
    }

    //get selected restaurant from Restaurant Activity
    private void extractDatafromIntent() {
        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(RESTAURANT_INDEX, Integer.MAX_VALUE);
    }

    private void startLocationUpdates() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE);
        locationRequest.setInterval(MIN_TIME);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    moveMapFocus(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                }
            };
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.myLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //make sure we have permission to do anything with location first
        getPermissions();
        // does the downloading
        checkForUpdates();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(locationPermission){

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){

                return;
            }
            mMap.setMyLocationEnabled(true);
            //update map to user's current location
            getDeviceLocation();
            startLocationUpdates();
        }

        //display pegs showing the location of each restaurant we have data for.
        int i = 0;
        for (final Restaurant r : restaurantManager) {
            double latitude = Double.parseDouble(r.getLatitude());
            double longitude = Double.parseDouble(r.getLongitude());
            final LatLng restaurant = new LatLng(latitude, longitude);
            String restaruantName = r.getName();
            String addr = r.getAddress();

            //get most recent inspection
            if (r.getRestaurantInspectionList().size() != 0) {

                //set peg color to hazard level
                String hazardLevel = r.getRestaurantInspectionList().get(0).getHazardRating();
                if (hazardLevel.equalsIgnoreCase("LOW")) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .snippet(addr + "\n" + "Hazard Rating: " + hazardLevel)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mHashMap.put(marker, i);
                } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .snippet(addr + "\n" + "Hazard Rating: " + hazardLevel)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    mHashMap.put(marker, i);
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .snippet(addr + "\n" + "Hazard Rating: " + hazardLevel)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    mHashMap.put(marker, i);
                }

                //Zoom map for view
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restaurant, 10f));

                //Create customized info window view
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                        TextView restaurantName = view.findViewById(R.id.restName);
                        TextView restaurantAddr = view.findViewById(R.id.restSnippet);

                        restaurantName.setText(marker.getTitle());
                        restaurantAddr.setText(marker.getSnippet());

                        return view;
                    }
                });

                //Start Restaurant Activity upon clicking info window
                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {

                        Toast.makeText(MapsActivity.this, "You clicked " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                        int pos = mHashMap.get(marker);

//                        start restaurant activity
                        Intent intent = RestaurantActivity.makeIntent(MapsActivity.this, pos);
                        startActivity(intent);
                    }
                });
            }
            i++;
        }

        extractDatafromIntent();
        displayInfoWindow();
    }

    public void loadMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    //Followed video tutorials
    //https://www.youtube.com/watch?v=Vt6H9TOmsuo&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=4
    private void getPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                //they already have permissions, load the map
                locationPermission = true;
                loadMap();

                //other wise, lets ask for permission to access their location
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results){

        locationPermission = false;
        switch (requestCode){
            case LOCATION_PERMISSION_CODE: {

                if(results.length > 0){
                    for(int i : results){
                        if(i != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                    }

                    //they accepted permissions, load map
                    locationPermission = true;
                    loadMap();
                }
            }
        }
    }

    private void getDeviceLocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try{

            if(locationPermission){
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();

                            moveMapFocus(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(MapsActivity.this, "ERROR: Could not get device location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch(SecurityException securityException){

            Log.wtf(LOG_TAG, "ERROR: " + securityException.getStackTrace());
        }
    }

    private void moveMapFocus(LatLng latLng, float zoom){

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);

    }

    //display selected restaurant's info window
    private void displayInfoWindow() {
        for (Marker m : mHashMap.keySet()) {
            if (restaurantIndex == mHashMap.get(m)) {
                m.showInfoWindow();
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
                Log.d(SERVER_TAG, "onResponse: Server Response" + response.toString());
                Log.d(SERVER_TAG, "onResponse: received information: " + response.body().toString());

                ArrayList<Resource> ResourceList = response.body().getResult().getResources();
                // UI STUFF
                String format = ResourceList.get(0).getFormat();
                String url = ResourceList.get(0).getUrl();
                String date_last_modified = ResourceList.get(0).getDate_last_modified();
                //TODO: DOWNLOAD THE URL DATA IF DATE COMPARISON > 20 HOURS
                if(type.equalsIgnoreCase(ID_RESTAURANTS)){
                    // Todo check time here;
                    if(true){
                        //startActivity(RequireDownloadActivity.makeIntent(MapsActivity.this));
                    }
                    fetchPackages(ID_INSPECTIONS);
                } else if (type.equalsIgnoreCase(ID_INSPECTIONS)){
                    // Todo check time here;
                    if(true){
                        startActivity(RequireDownloadActivity.makeIntent(MapsActivity.this));
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
                Log.e(SERVER_TAG, "something went wrong " + t.getMessage());
                Toast.makeText(MapsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
