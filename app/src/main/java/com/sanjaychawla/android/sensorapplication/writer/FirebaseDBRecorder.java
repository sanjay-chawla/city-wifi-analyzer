package com.sanjaychawla.android.sensorapplication.writer;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sanjaychawla.android.sensorapplication.callback.CustomCallback;
import com.sanjaychawla.android.sensorapplication.data.Record;

public class FirebaseDBRecorder {
    private static String TAG = FirebaseDBRecorder.class.getSimpleName();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void write(Record record){
        if(db != null){
            db.collection("records").add(record);
        }
    }

    public static void recogniseWiFi(final Context context, final Record record, final CustomCallback callback){
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
                        if(((Double) routerLatitude) != null && ((Double) routerLongitude) != null){
                            Log.d(TAG, "match found in ID: " + documentSnapshot.getId());
                            float[] result = new float[3];
                            Location.distanceBetween(record.getLatitude(), record.getLongitude(),
                                    routerLatitude, routerLongitude, result);
                            Log.d(TAG, "Distance : " + result[0]);
                            if(result[0] < 150) {
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
            })
        ;
    }
}
