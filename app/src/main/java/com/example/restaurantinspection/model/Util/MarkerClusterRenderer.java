package com.example.restaurantinspection.model.Util;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;


// Followed tutorial https://codinginfinite.com/android-google-map-custom-marker-clustering/
public class MarkerClusterRenderer extends DefaultClusterRenderer {

    private static final int Marker_DIMENSION = 48;
    private final IconGenerator iconGenerator;
    private final ImageView markerImageView;


    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager clusterManager) {
        super(context, map, clusterManager);
        iconGenerator = new IconGenerator(context);
        markerImageView = new ImageView(context);
        markerImageView.setLayoutParams(new ViewGroup.LayoutParams(Marker_DIMENSION, Marker_DIMENSION));
        iconGenerator.setContentView(markerImageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.title(item.getTitle()).snippet(item.getSnippet());

        //set icon to match appropriate hazard level

        if (item.getSnippet().endsWith("Low") || item.getSnippet().endsWith("Bajo") || item.getSnippet().endsWith(("低危"))) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        if (item.getSnippet().endsWith(("Moderate")) || item.getSnippet().endsWith("Mediano") || item.getSnippet().endsWith("中等")) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        if (item.getSnippet().endsWith(("High")) || item.getSnippet().endsWith("Alto") || item.getSnippet().endsWith("高危")) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

    }


    @Override
    protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        //associate marker with restaurant
        marker.setTag(clusterItem);
    }

    @Override
    public Marker getMarker(ClusterItem clusterItem) {
        return super.getMarker(clusterItem);
    }
}
