package com.sanjaychawla.android.sensorapplication.callback;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

public interface MapChangeOnDataFetchCallback {
    void onDataFetchCallback(MapboxMap mapboxMap, GeoJsonSource source);
}
