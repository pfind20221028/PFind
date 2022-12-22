package com.karl.pfind.Services;

import static com.karl.pfind.BroadcastReceiver.BroadcastConstants.NOTIFICATION_BROADCAST_ACTION;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.ui.services.RegisterDeviceService;

import java.util.logging.Logger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private RegisterDeviceService registerDeviceService;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        log.info("From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            log.info("Message data payload: " + remoteMessage.getData());

//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.dog_walking_48px)
//                    .setContentTitle("Test")
//                    .setContentText("Test")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//            builder.build();

            String action = remoteMessage.getData().get("action");

            CustomLocation missingDeviceLocation = new CustomLocation();
            missingDeviceLocation.setLatitude(Double.parseDouble(remoteMessage.getData().get("latitude")));
            missingDeviceLocation.setLongitude(Double.parseDouble(remoteMessage.getData().get("longitude")));
            String mcad = remoteMessage.getData().get("mac_address");
            String owner = remoteMessage.getData().get("owner_uid");
            broadcastReceivedMissingDeviceLocation(mcad,missingDeviceLocation,owner,action);
        }
    }

    private void broadcastReceivedMissingDeviceLocation(String mac_address, CustomLocation missingDeviceLocation, String owner, String action) {
        Intent intent = new Intent(NOTIFICATION_BROADCAST_ACTION);
        // You can also include some extra data.
        intent.putExtra("owner_uid", owner);
        intent.putExtra("action", action);
        intent.putExtra("mac_address", mac_address);
        Bundle b = new Bundle();
        b.putParcelable("missingDeviceLocation", missingDeviceLocation);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

}
