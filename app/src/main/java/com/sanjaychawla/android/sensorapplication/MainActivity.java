package com.sanjaychawla.android.sensorapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.sanjaychawla.android.sensorapplication.event.receiver.LocationUpdatesBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    MapView mapView = null;
    private static final int UPDATE_INTERVAL = 20000;
    private static final String TAG = "MainActivity";
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;
    public static final int SMALLEST_DISPLACEMENT = 20;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded.
                        // Now you can add data or make other map adjustments
                    }
                });

            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        return locationRequest;
    }
}
