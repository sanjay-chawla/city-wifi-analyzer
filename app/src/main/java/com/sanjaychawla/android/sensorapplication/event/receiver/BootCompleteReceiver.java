package com.sanjaychawla.android.sensorapplication.event.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final int UPDATE_INTERVAL = 20000;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;
    private FusedLocationProviderClient fusedLocationClient;
    private Context context;

    public void onReceive(Context context, Intent intent ) {
        if( intent.getAction() == null || !intent.getAction().equals( "android.intent.action.BOOT_COMPLETED" ) ) return;
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        startLocationUpdates();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(createLocationRequest(),
                getPendingIntent());
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(30000);
        locationRequest.setMaxWaitTime(MAX_WAIT_TIME);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
