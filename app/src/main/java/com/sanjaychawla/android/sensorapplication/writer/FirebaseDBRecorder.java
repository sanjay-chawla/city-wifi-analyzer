package com.sanjaychawla.android.sensorapplication.writer;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.sanjaychawla.android.sensorapplication.callback.CustomCallback;
import com.sanjaychawla.android.sensorapplication.callback.MapChangeOnDataFetchCallback;
import com.sanjaychawla.android.sensorapplication.data.Record;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleBlur;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

public class FirebaseDBRecorder {
    private static String TAG = FirebaseDBRecorder.class.getSimpleName();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void write(Record record){
        if(db != null){
            db.collection("records").add(record);
        }
    }

    public static void recogniseWiFi(final Context context, final Record record, final CustomCallback callback) {
        db.collectionGroup("03292017")
            .whereEqualTo("SSID", record.getSsid())
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    Log.d(TAG, "is from cache: " + queryDocumentSnapshots.getMetadata().isFromCache());
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        double routerLatitude = documentSnapshot.getDouble("LAT");
                        double routerLongitude = documentSnapshot.getDouble("LON");
                        if (((Double) routerLatitude) != null && ((Double) routerLongitude) != null) {
                            Log.d(TAG, "match found in ID: " + documentSnapshot.getId());
                            float[] result = new float[3];
                            Location.distanceBetween(record.getLatitude(), record.getLongitude(),
                                    routerLatitude, routerLongitude, result);
                            Log.d(TAG, "Distance : " + result[0]);
                            if (result[0] < 150) {
                                record.setDistanceFromRouter(result[0]);
                                record.setWifiRecognised(true);
                            }
                            break;
                        } else {
                            record.setDistanceFromRouter(9999999);
                            record.setWifiRecognised(true);
                            break;
                        }
                    }
                    callback.onCallback(context, record);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Failure in read: " + e.getMessage());
                }
            });
    }

    public static void generateGeoJSON(MapboxMap mapboxMap, MapChangeOnDataFetchCallback callback){
        List<Feature> featureList = new ArrayList<>();
        db.collection("records")
                .whereEqualTo("networkType", "WIFI")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG,"response received ");
                    for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                        if (isValuePresent(document, "latitude")
                        && isValuePresent(document, "longitude")
                        && isValuePresent(document, "wiFiStrength")
                        && isValuePresent(document, "networkSpeed")){
                            Double latitude = (Double) document.get("latitude");
                            Double longitude = (Double) document.get("longitude");
                            long strength = (long) document.get("wiFiStrength");
                            double speed = (Double) document.get("networkSpeed");
                            Point point = Point.fromLngLat(longitude, latitude, 1.0);
                            Feature feature = Feature.fromGeometry(point);
                            feature.addNumberProperty("mag", strength);
                            feature.addNumberProperty("internetSpeed", speed);
                            featureList.add(feature);
                        }
                    }
                    GeoJsonSource source = new GeoJsonSource("userRecords",
                                                    FeatureCollection.fromFeatures(featureList),
                                                    new GeoJsonOptions()
                                                            .withCluster(true)
                                                            .withClusterMaxZoom(17)
                                                            .withClusterRadius(10));


                    callback.onDataFetchCallback(mapboxMap, source);
                });

    }
    public static void generateGeoJSONForNetwork(MapboxMap mapboxMap, MapChangeOnDataFetchCallback callback){
        List<Feature> featureList = new ArrayList<>();
        db.collectionGroup("03292017")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d(TAG,"response received ");
                    for (DocumentSnapshot document: querySnapshot.getDocuments()) {
                        if (isValuePresent(document, "LAT")
                        && isValuePresent(document, "LON")
                        && isValuePresent(document, "SSID")){
                            Double latitude = (Double) document.get("LAT");
                            Double longitude = (Double) document.get("LON");
                            String SSID = (String) document.get("SSID");
                            Point point = Point.fromLngLat(longitude, latitude, 1.0);
                            Feature feature = Feature.fromGeometry(point);
                            feature.addStringProperty("SSID", SSID);
                            // feature.addNumberProperty("internetSpeed", speed);
                            featureList.add(feature);
                        }
                    }
                    GeoJsonSource source = new GeoJsonSource("wifiPoints",
                                                    FeatureCollection.fromFeatures(featureList)
                                                    );
                    callback.onDataFetchCallback(mapboxMap, source);
                });
    }

    private static boolean isValuePresent(DocumentSnapshot document, String field){
        return document.get(field) != null;
    }
}
