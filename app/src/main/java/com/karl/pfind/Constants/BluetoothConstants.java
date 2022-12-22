package com.karl.Pfind.Constants;

import java.util.UUID;

public class BluetoothConstants {

    public static Integer REQUEST_ENABLE_BT = 1;

    public static String BLUETOOTH_CONNECT = "BLUETOOTH_CONNECT";
    public static String BLUETOOTH_SCAN = "BLUETOOTH_SCAN";

    public static final String APP_NAME = "BTChat";
    public static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");

    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;


}
