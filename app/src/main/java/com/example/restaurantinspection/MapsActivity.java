package com.example.restaurantinspection;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.restaurantinspection.model.Restaurant;
import com.example.restaurantinspection.model.RestaurantManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RestaurantManager restaurantManager = RestaurantManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        for (Restaurant r : restaurantManager
        ) {
            double latitude = Double.parseDouble(r.getLatitude());
            double longitude = Double.parseDouble(r.getLongitude());
            LatLng restaurant = new LatLng(latitude, longitude);
            String restaruantName = r.getName();

            //get most recent inspection
            if (r.getRestaurantInspectionList().size() != 0) {

                //set peg color to hazard level
                String hazardLevel = r.getRestaurantInspectionList().get(0).getHazardRating();
                if (hazardLevel.equalsIgnoreCase("LOW")) {
                    mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                } else if (hazardLevel.equalsIgnoreCase("MODERATE")) {
                    mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(restaurant)
                            .title(restaruantName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(restaurant));
            }


        }
    }

}
