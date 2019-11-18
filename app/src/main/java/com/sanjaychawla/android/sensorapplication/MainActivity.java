package com.sanjaychawla.android.sensorapplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.layers.TransitionOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.sanjaychawla.android.sensorapplication.event.receiver.LocationUpdatesBroadcastReceiver;
import com.sanjaychawla.android.sensorapplication.writer.FirebaseDBRecorder;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private MapboxMap mapboxMap;
    private MapView mapView;
    FloatingActionButton myLocationButton;
    private static final int UPDATE_INTERVAL = 20000;
    private static final String TAG = "MainActivity";
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5;
    public static final int SMALLEST_DISPLACEMENT = 20;
    private FusedLocationProviderClient fusedLocationClient;
    private CircleLayer unclustered;
    private CircleLayer circles;
    private SymbolLayer networkMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
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

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        FirebaseDBRecorder.generateGeoJSON(mapboxMap, (mapboxMap1, source) -> {
            mapboxMap1.setStyle(Style.MAPBOX_STREETS,
                    style -> {
                        enableLocationComponent(style);
                        style.addSource(source);
                        enableMapHostspot(style, Property.NONE);
                    });
        });
        FirebaseDBRecorder.generateGeoJSONForNetwork(mapboxMap, (mapboxMap1, source) -> {
            mapboxMap1.getStyle(
                    style -> {
                        style.addSource(source);
                        enableNetworkDetails(style, Property.NONE);
                    });
        });

        MaterialButton button1 = findViewById(R.id.hotspots);
        button1.setOnClickListener(v -> {
            mapboxMap.getStyle(style -> {
                style.setTransition(new TransitionOptions(500, 500, true));
                showMapHotspots(style);
                hideNetworkDetails(style);
            });
        });

        MaterialButton button2 = findViewById(R.id.networks);
        button2.setOnClickListener(v -> {
                mapboxMap.getStyle(style -> {
                    showNetworkDetails(style);
                    hideMapHotspots(style);
                });
        });

        MaterialButton button3 = findViewById(R.id.none);
        button3.setOnClickListener(v -> {
            mapboxMap.getStyle(style -> {
                style.setTransition(new TransitionOptions(500, 500, true));
                hideNetworkDetails(style);
                hideMapHotspots(style);
                MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
                toggleGroup.clearChecked();
            });
        });

    }

    private void enableNetworkDetails(Style style, String visibility) {
        style.addImage("markerimage", BitmapFactory.decodeResource(getResources(),
                R.drawable.mapbox_marker_icon_default));
        networkMarkers = new SymbolLayer("markerLayer", "wifiPoints");
        networkMarkers.setProperties(PropertyFactory.iconImage("markerimage"),
                PropertyFactory.visibility(visibility));
        style.addLayer(networkMarkers);

    }

    private void hideMapHotspots(Style style){
        unclustered.setProperties(PropertyFactory.visibility(Property.NONE));
        for (int i = 0; i < 3; i++) {
            Layer l = style.getLayer("cluster-1-" + i);
            if(l != null)
                    l.setProperties(PropertyFactory.visibility(Property.NONE));
        }
    }
    private void showMapHotspots(Style style){
        unclustered.setProperties(PropertyFactory.visibility(Property.VISIBLE));
        // style.addLayerBelow(unclustered, "buildings");
        for (int i = 0; i < 3; i++) {
            Layer l = style.getLayer("cluster-1-" + i);
            if(l != null)
                    l.setProperties(PropertyFactory.visibility(Property.VISIBLE));
        }
    }

    private void hideNetworkDetails(Style style){
        networkMarkers.setProperties(PropertyFactory.visibility(Property.NONE));
    }

    private void showNetworkDetails(Style style){
        networkMarkers.setProperties(PropertyFactory.visibility(Property.VISIBLE));
    }

    private void enableMapHostspot(Style style, String visibility) {
        final int[][] layers = new int[][]{
                new int[]{150, Color.parseColor("#E55E5E")},
                new int[]{20, Color.parseColor("#F9886C")},
                new int[]{0, Color.parseColor("#FBB03B")}
        };
        unclustered = new CircleLayer("unclustered-points-1", "userRecords");
        unclustered.setProperties(
                circleColor(Color.parseColor("#FBB03B")),
                circleRadius(20f),
                circleBlur(1f),
                PropertyFactory.visibility(visibility));
        unclustered.setFilter(Expression.neq(get("cluster"), literal(true)));
        style.addLayerBelow(unclustered, "buildings");

        for (int i = 0; i < layers.length; i++) {
            circles = new CircleLayer("cluster-1-" + i, "userRecords");
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(70f),
                    circleBlur(1f),
                    PropertyFactory.visibility(visibility)
            );
            Expression pointCount = toNumber(get("point_count"));
            circles.setFilter(
                    i == 0
                            ? Expression.gte(pointCount, literal(layers[i][0])) :
                            Expression.all(
                                    Expression.gte(pointCount, literal(layers[i][0])),
                                    Expression.lt(pointCount, literal(layers[i - 1][0]))
                            )
            );
            style.addLayerBelow(circles, "buildings");
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            final LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions
                            .builder(this, loadedMapStyle)
                            .build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            locationComponent.addOnCameraTrackingChangedListener(new OnCameraTrackingChangedListener() {
                @Override
                public void onCameraTrackingDismissed() {
                    myLocationButton.show();
                }

                @Override
                public void onCameraTrackingChanged(int currentMode) {
                }
            });

            myLocationButton = findViewById(R.id.locationFAB);
            myLocationButton.setOnClickListener(v -> {
                // Toggle GPS position updates
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation()))
                        .zoom(14) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000, new MapboxMap.CancelableCallback() {
                    @Override
                    public void onCancel() {

                    }
                    @Override
                    public void onFinish() {
                        mapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING);
                        myLocationButton.hide();
                    }
                });
            });

            locationComponent.getLocationComponentOptions().trackingInitialMoveThreshold();
            locationComponent.getLocationComponentOptions().trackingMultiFingerMoveThreshold();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.onPause();
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

}
