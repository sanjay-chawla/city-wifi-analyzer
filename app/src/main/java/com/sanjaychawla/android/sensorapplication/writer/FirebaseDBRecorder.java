package com.sanjaychawla.android.sensorapplication.writer;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sanjaychawla.android.sensorapplication.DataObject.Record;

public class FirebaseDBRecorder {
    private static String TAG = FirebaseDBRecorder.class.getSimpleName();
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void write(Record record){
        if(db != null){
            db.collection("records").add(record);
        }
    }
}
