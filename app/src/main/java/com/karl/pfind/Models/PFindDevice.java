package com.karl.pfind.Models;

import com.karl.pfind.ui.register.BleDevice;

public class PFindDevice {

    private BleDevice bleDevice;
    private String userID;
    private CustomLocation lastLocation;
    private Boolean isMissing;

    public PFindDevice() {}

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public CustomLocation getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(CustomLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Boolean getMissing() {
        return isMissing;
    }

    public void setMissing(Boolean missing) {
        isMissing = missing;
    }
}
