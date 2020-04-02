package com.example.restaurantinspection;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.restaurantinspection.model.QueryPreferences;
import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;
import com.example.restaurantinspection.model.Util.MarkerClusterRenderer;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String RESTAURANT_INDEX = "com.example.restaurantinspection - restaurant index";
    private final String LOG_TAG = "RESTAURANT INSPECTION: ";
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private int restaurantIndex;

    private GoogleMap mMap;
    private HashMap<Restaurant, Integer> mHashMap = new HashMap<>();
    private ClusterManager<Restaurant> mClusterManager;
    private MarkerClusterRenderer mRenderer;
    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Restaurant> searchResult = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private EditText mSearchText;

    private static final int LOADING_DATA_RESULT_CODE = 100;
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

    private void extractDatafromIntent() {
        restaurantIndex = getIntent().getIntExtra(RESTAURANT_INDEX, -1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOADING_DATA_RESULT_CODE && resultCode == RESULT_OK) {
            getPermissions();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //make sure we have permission to do anything with location first
        getPermissions();
        setUpSearchBar();
        List<String> favourite_list = readFavouriteList();
        compare_date(favourite_list);
        setFabSettingsButton();
    }

/*    private void setUpSearchBar() {
        mSearchText = findViewById(R.id.input_search);
        mSearchText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN ||
                    event.getAction() == KeyEvent.KEYCODE_ENTER) {
                //TODO: execute method for searching

            }
            return false;
        });

    }*/

        private void setUpSearchBar() {
        SearchView searchView = findViewById(R.id.searchmap);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(MapsActivity.this, query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(MapsActivity.this);
        //TODO: execute search method - below is placeholder search method to test marker behavior
        if (!query.isEmpty()) {

            mClusterManager.clearItems();

            for (Restaurant r : mHashMap.keySet()
            ) {
                if (r.getTitle().toUpperCase().contains(query.toUpperCase())) {
                    mClusterManager.addItem(r);
                }
            }

            mClusterManager.cluster();
        }
    }

    // allows additional settings to be shown/hidden
    private void setFabSettingsButton() {
        final ConstraintLayout extraSettingsLayout = findViewById(R.id.mapSettings_constraintLayout);
        FloatingActionButton fab = findViewById(R.id.settingsPopUpFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(extraSettingsLayout.getVisibility() == View.INVISIBLE){
                    extraSettingsLayout.setVisibility(View.VISIBLE);
                }else{
                    extraSettingsLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void moveMapFocus(LatLng latLng, float zoom) {

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);

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

        //only display marker for selected restaurant
        updateClusters();

        //on tap display all restaurants
        updateMapOnClick();
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

    //re-cluster all restaurants
    private void updateMapOnClick() {
        mMap.setOnMapClickListener(latLng -> {

            if (mClusterManager.getAlgorithm().getItems().size() == 1) {

                setUpClusterManager();
                setUpInfoWindows();
            }
        });
    }

    private void updateClusters() {
        extractDatafromIntent();

        //if there's extracted data
        if (restaurantIndex != -1) {

            //remove all restaurants
            mClusterManager.clearItems();
            for (Restaurant r : mHashMap.keySet()) {
                if (mHashMap.get(r) == restaurantIndex) {

                    //only add the specific restaurant
                    mClusterManager.addItem(r);
                }
            }
            //re-cluster map
            mClusterManager.cluster();
        }
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
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(marker -> {
            Restaurant restaurant = (Restaurant) marker.getTag();
            int pos = mHashMap.get(restaurant);

            //start restaurant activity
            Intent intent = RestaurantActivity.makeIntent(MapsActivity.this, pos);
            startActivity(intent);
        });
    }

    private void setUpClusterManager() {
        //Initalize cluster manager
        mClusterManager = new ClusterManager(this, mMap);
        mRenderer = new MarkerClusterRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(mRenderer);

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
        for (Restaurant r : restaurantManager) {
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

    public void compare_date(List<String> list){
        List<Restaurant> favourite_list = new ArrayList<>();
        List<String> new_favourite_list = new ArrayList<>();
        for(String a : list){
            String[] arr = a.split("\\+");
            for(Restaurant restaurants : restaurantManager.getRestaurantList()){
                if(restaurants.getTrackingNumber().equals(arr[0])){
                    if(!restaurants.getRestaurantInspectionList().get(0).getInspectionDate().equals(arr[1])){
                        arr[1] = restaurants.getRestaurantInspectionList().get(0).getInspectionDate();
                        favourite_list.add(restaurants);
                        new_favourite_list.add(arr[0]+"+"+arr[1]);
                        Log.d("MAP",favourite_list.toString());
                    }
                }
            }
        }
        if(favourite_list.size()!=0){
            show_dialog(favourite_list);
            saveList(new_favourite_list);
        }
    }

    public void show_dialog(List<Restaurant> list){
        List<String> show = new ArrayList<>();
        for(Restaurant restaurants : list){
            String name = restaurants.getName();
            String date = restaurants.getRestaurantInspectionList().get(0).getInspectionDate();
            String hazard_level = restaurants.getRestaurantInspectionList().get(0).getHazardRating();
            String restaurants_show = name + "  " + date + "  " +hazard_level+"\n";
            show.add(restaurants_show);
        }
        AlertDialog.Builder new_update = new AlertDialog.Builder(this);
        new_update.setTitle("Newest Update for your favourite");
        new_update.setMessage(show.toString().replace("[","").
                replace("]","").
                replace(",","").trim());
        new_update.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        new_update.show();
    }

    public List<String> readFavouriteList() {
        List<String> list = new ArrayList<>();
        SharedPreferences sp1 = getSharedPreferences("favourite_list", Context.MODE_PRIVATE);
        String favourite_jsonStr = sp1.getString("Favourite_list","");
        if(!favourite_jsonStr.equals("")){
            Gson gson = new Gson();
            list = gson.fromJson(favourite_jsonStr,new TypeToken<List<String>>(){}.getType());
        }
        return list;
    }
    public void saveList(List<String> favourite_list){
        SharedPreferences sp = this.getSharedPreferences("favourite_list", Context.MODE_PRIVATE);
        Gson user_gson = new Gson();
        String favourite_jsonStr = user_gson.toJson(favourite_list);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Favourite_list",favourite_jsonStr);
        editor.apply();
    }
}
