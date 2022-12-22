package com.karl.pfind.Permissions;

import static com.karl.Pfind.Constants.BluetoothConstants.BLUETOOTH_CONNECT;
import static com.karl.Pfind.Constants.GPSConstants.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;


public class PermissionChecker {

    private static String[] PERMISSIONS_BLUETOOTH = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static String[] PERMISSIONS_GPS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static void checkPermission(Context context, String permission) {

        if (permission.equals(BLUETOOTH_CONNECT)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        (Activity) context,
                        PERMISSIONS_BLUETOOTH,
                        1
                );
            }
        }

        if (permission.equals(ACCESS_FINE_LOCATION)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        (Activity) context,
                        PERMISSIONS_GPS,
                        1
                );
            }
        }


    }
}
