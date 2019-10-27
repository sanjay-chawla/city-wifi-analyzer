package com.sanjaychawla.android.sensorapplication.callback;

import android.content.Context;

import com.sanjaychawla.android.sensorapplication.data.Record;

public interface CustomCallback {
    void onCallback(Context context, Record record);
}
