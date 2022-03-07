package com.example.util;
import android.os.Parcel;
import android.os.Parcelable;

public interface ProgressListener {
    void onProgress(long currentBytes, long contentLength, boolean done);
}