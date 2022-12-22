package com.karl.pfind.ui.services;

import static com.karl.pfind.Constants.FirebaseConstants.DB_REF_BLE_DEVICES;
import static com.karl.pfind.Constants.FirebaseConstants.FIREBASE_INSTANCE;
import static com.karl.pfind.ui.login.LoginConstants.FAILED;
import static com.karl.pfind.ui.login.LoginConstants.LOGIN_FAILED;
import static com.karl.pfind.ui.login.LoginConstants.SUCCESS;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.Models.Owner;
import com.karl.pfind.Models.PFindDevice;
import com.karl.pfind.ui.register.BleDevice;

public class RegisterDeviceService {

    private FirebaseDatabase database = FirebaseDatabase.getInstance(FIREBASE_INSTANCE);
    private DatabaseReference bleDevices = database.getReference(DB_REF_BLE_DEVICES);

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public PFindDevice registerDevice(String deviceName, String macAddress, WebServiceCallbackX webServiceCallback) {

        PFindDevice device = new PFindDevice();
        BleDevice ble = new BleDevice();
        ble.setDeviceName(deviceName);
        ble.setMacAddress(macAddress);
        device.setBleDevice(ble);
        device.setUserID(mAuth.getCurrentUser().getUid());
        device.setMissing(false);
        device.setLastLocation(new CustomLocation(27.2046,77.4977));

        bleDevices.push().setValue(device);

        // TODO
        // Use callback
        return device;
    }

    public void retrieveUserDevice(WebServiceCallbackX callback) {

        Query query = bleDevices.orderByChild("userID")
                .equalTo(mAuth.getCurrentUser().getUid());

        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    callback.onWebServiceError(task.getException().toString());
                }
                else {
                    if (isEmpty(task.getResult().getChildren())) {
                        callback.onWebServiceError(FAILED);
                    } else {
                        for (DataSnapshot childSnapshot: task.getResult().getChildren()) {

                            PFindDevice d = childSnapshot.getValue (PFindDevice.class);
                            callback.onWebServiceSuccess(SUCCESS,d);
                        }
                    }
                }
            }
        });
    }

    private boolean isEmpty(Iterable<DataSnapshot> children) {
        int size = 0;
        for(DataSnapshot value : children) {
            size++;
        }
        return size == 0;
    }

}
