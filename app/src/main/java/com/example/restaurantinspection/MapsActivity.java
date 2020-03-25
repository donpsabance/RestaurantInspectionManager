package com.example.restaurantinspection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Restaurant clickRestaurant;

    private GoogleMap mMap;
    private HashMap<Restaurant, Integer> mHashMap = new HashMap<>();
    private ClusterManager<Restaurant> mClusterManager;
    private List<Restaurant> restaurants = new ArrayList<>();
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
            }


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
        // load extra data from disk
        checkToUpdateData();
    }

    private void checkToUpdateData() {
        if (!restaurantManager.isExtraDataLoaded()) {
            startActivity(RequireDownloadActivity.makeIntent(this));
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setUpMap();
    }

    private void setUpMap() {
        if (locationPermission) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            //update map to user's current location
            getDeviceLocation();
            startLocationUpdates();
        }

        setUpClusterManager();
        setUpInfoWindows();

    }

    private void setUpInfoWindows() {
        //Create customized info window view
        mClusterManager.getMarkerCollection().setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
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
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Restaurant restaurant = (Restaurant) marker.getTag();
                int pos = mHashMap.get(restaurant);

                //start restaurant activity
                Intent intent = RestaurantActivity.makeIntent(MapsActivity.this, pos);
                startActivity(intent);
            }
        });
    }

    private void setUpClusterManager() {
        //Initalize cluster manager
        mClusterManager = new ClusterManager(this, mMap);
        mClusterManager.setRenderer(new MarkerClusterRenderer(this, mMap, mClusterManager));

        //set cluster manager
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        //Add restaurants (cluster item) to restaurants
        List<Restaurant> items = getRestaurants();
        mClusterManager.addItems(items);
        mClusterManager.cluster();
    }


    private List<Restaurant> getRestaurants() {

        int resIndex = 0;
        for (Restaurant r : restaurantManager
        ) {
            if (r.getRestaurantInspectionList().size() != 0) {
                double lat = Double.parseDouble(r.getLatitude());
                double lon = Double.parseDouble(r.getLongitude());
                LatLng restaurantPos = new LatLng(lat, lon);
                String restaurantTitle = r.getName();

                String restHazard = r.getRestaurantInspectionList().get(0).getHazardRating();
                String restaurantSnippet = r.getAddress() + "\n" + "Hazard Rating: " + restHazard;

                Restaurant restaurantItem = new Restaurant(restaurantPos, restaurantTitle, restaurantSnippet);
                restaurants.add(restaurantItem);

                mHashMap.put(restaurantItem, resIndex);
            }

            resIndex++;
        }
        return restaurants;
    }

    //Followed video tutorials
    //https://www.youtube.com/watch?v=Vt6H9TOmsuo&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=4
    private void getPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                //they already have permissions, load the map
                locationPermission = true;
                loadMap();

                //other wise, lets ask for permission to access their location
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {

        locationPermission = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {

                if (results.length > 0) {
                    for (int i : results) {
                        if (i != PackageManager.PERMISSION_GRANTED) {
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

    private void loadMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getDeviceLocation() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {

            if (locationPermission) {
                final Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            moveMapFocus(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(MapsActivity.this, "ERROR: Could not get device location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException securityException) {

            Log.wtf(LOG_TAG, "ERROR: " + securityException.getStackTrace());
        }
    }

    private void moveMapFocus(LatLng latLng, float zoom) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);

    }
}
