package com.sanjaychawla.android.sensorapplication.event.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.sanjaychawla.android.sensorapplication.helper.InternetSpeedHelper;
import com.sanjaychawla.android.sensorapplication.writer.CSVRecorder;

import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES =
            "com.sanjaychawla.android.sensorapplication.LocationUpdatesBroadcastReceiver.PROCESS_UPDATES";
    private static final String TAG = "LUBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    for (Location location : locations) {
                        String networkType = getNetworkInformation(context);
                        double connectionSpeed = new InternetSpeedHelper().calculateSpeed();
                        Log.d(TAG, "here you go: latitude: " + location.getLatitude() + " network type: " + networkType);
                        CSVRecorder.write(context.getExternalFilesDir(null).getAbsolutePath(), location.getLatitude(), location.getLongitude(), networkType, connectionSpeed);
                    }
                }
            }
        }
    }

    private String getNetworkInformation(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info.getType() == ConnectivityManager.TYPE_WIFI){
            return "WIFI";
        } else if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            return info.getSubtypeName();
        } else {
            return "UNKNOWN";
        }
    }
}
