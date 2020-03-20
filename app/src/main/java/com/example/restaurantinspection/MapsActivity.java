package com.example.restaurantinspection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.security.Permission;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String RESTAURANT_INDEX = "com.example.restaurantinspection - restaurant index";
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private GoogleMap mMap;
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();
    private int restaurantIndex;
    final HashMap<Marker, Integer> mHashMap = new HashMap<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        getPermissions();
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

    //Followed video tutorials
    //https://www.youtube.com/watch?v=Vt6H9TOmsuo&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=4
    private void getPermissions(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationPermission = true;
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

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
                    locationPermission = true;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }
            }
        }
    }


    //display selected restaurant's info window
    private void displayInfoWindow() {
        for (Marker m : mHashMap.keySet()) {
            if (restaurantIndex == mHashMap.get(m)) {
                m.showInfoWindow();
            }
        }
    }

}
