package com.karl.pfind.Utils;

import android.bluetooth.BluetoothAdapter;

import com.google.android.datatransport.runtime.dagger.Component;

public class BluetoothUtils {

    private static BluetoothUtils single_instance = null;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public static BluetoothUtils getInstance()
    {
        if (single_instance == null)
            single_instance = new BluetoothUtils();

        return single_instance;
    }

    public boolean isBluetoothOpen() {
        if (mBluetoothAdapter == null)
            return false;

        if (!mBluetoothAdapter.isEnabled())
            return false;

        return true;
    }
}
