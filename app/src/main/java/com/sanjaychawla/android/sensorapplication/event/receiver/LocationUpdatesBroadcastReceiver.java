package com.sanjaychawla.android.sensorapplication.event.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.sanjaychawla.android.sensorapplication.callback.CustomCallback;
import com.sanjaychawla.android.sensorapplication.data.Record;
import com.sanjaychawla.android.sensorapplication.helper.InternetSpeedHelper;
import com.sanjaychawla.android.sensorapplication.writer.FirebaseDBRecorder;

import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATES = "com.sanjaychawla.android.sensorapplication.LocationUpdatesBroadcastReceiver.PROCESS_UPDATES";
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
                        Record record = new Record();
                        record.setLatitude(location.getLatitude());
                        record.setLongitude(location.getLongitude());
                        getNetworkInformation(context, record);
                    }
                }
            }
        }
    }

    private static void getNetworkInformation(Context context, Record record) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo ();
            record.setSsid(wifiInfo.getSSID().replace("\"",""));
            record.setNetworkType("WIFI");
            record.setWiFiStrength(wifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5));
            Log.d(TAG, "SSID: " + record.getSsid());
            FirebaseDBRecorder.recogniseWiFi(context, record, new InternetSpeedCallback());
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            record.setNetworkType(info.getSubtypeName());
            new InternetSpeedCallback().onCallback(context, record);
        } else {
            record.setNetworkType("UNKNOWN");
            new InternetSpeedCallback().onCallback(context, record);
        }
    }

    private static class InternetSpeedCallback implements CustomCallback {
        @Override
        public void onCallback(Context context, Record record) {
            new InternetSpeedHelper().calculateSpeed(record,
                    context.getExternalFilesDir(null).getAbsolutePath()
            );
        }
    }
}
