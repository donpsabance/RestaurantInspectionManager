package com.example.restaurantinspection.model.Service;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * checks if has internet
 * source : stackoverflow.com/questions/32547006/connectivitymanager-getnetworkinfoint-deprecated
 */
public class CheckInternet {

        public static boolean getConnectionType(Context context){
            boolean result = false; // Returns connection type.
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cm != null) {
                    NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            result = true;
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            result = true;
                        }
                    }
                }
            } else {
                if (cm != null) {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null) {
                        // connected to the internet
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            result = true;
                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                            result = true;
                    }
                }
            }
            return result;
        }
}
