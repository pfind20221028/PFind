package com.karl.pfind.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class CustomLocation implements Serializable, Parcelable {

    private double latitude;
    private double longitude;

    public CustomLocation() {
    }

    public CustomLocation(double latitude,double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected CustomLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<CustomLocation> CREATOR = new Creator<CustomLocation>() {
        @Override
        public CustomLocation createFromParcel(Parcel in) {
            return new CustomLocation(in);
        }

        @Override
        public CustomLocation[] newArray(int size) {
            return new CustomLocation[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
