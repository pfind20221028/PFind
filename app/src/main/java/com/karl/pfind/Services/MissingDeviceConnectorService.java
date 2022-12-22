package com.karl.pfind.Services;

import static com.karl.Pfind.Constants.GPSConstants.ACCESS_FINE_LOCATION;
import static com.karl.Pfind.Constants.GPSConstants.ZOOM_LEVEL;
import static com.karl.pfind.BroadcastReceiver.BroadcastConstants.NOTIFICATION_BROADCAST_ACTION;
import static com.karl.pfind.Constants.FirebaseConstants.ACTION_FOUND;
import static com.karl.pfind.Constants.FirebaseConstants.ACTION_MISSING;
import static com.karl.pfind.Constants.FirebaseConstants.MISSING_DEVICES_TOPIC;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.karl.pfind.Constants.RetrofitConstants;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.Permissions.PermissionChecker;
import com.karl.pfind.bluno.BlunoLibraryService;
import com.karl.pfind.http.Note;
import com.karl.pfind.http.NotificationEndpoint;

import java.util.HashMap;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissingDeviceConnectorService extends BlunoLibraryService {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private BroadcastReceiver notifsReceiver;
    private Boolean isDeviceReady,
                    isSearchingForMissingDevice = false,
                    hasTriggered = false;

    private String missingDeviceMacAddress,action,owner_uid;
    private Location missingDevice,host;
    private RetrofitConstants retrofit;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    public void onCreate() {
        super.onCreate();

        onCreateProcess();
        serialBegin(115200);
        setUpBroadcastReceiver();
        onResumeProcess();
        setUpRetrofit();
        setUpGps();
    }

    private void setUpBroadcastReceiver() {

        notifsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                action = intent.getStringExtra("action");

                if(action.equals(ACTION_MISSING)) {
                    owner_uid = intent.getStringExtra("owner_uid");
                    Bundle b = intent.getBundleExtra("Location");
                    CustomLocation customLocation = b.getParcelable("missingDeviceLocation");

                    missingDeviceMacAddress = intent.getStringExtra("mac_address");

                    missingDevice = missingDevice == null ? new Location("missingDevice") : missingDevice;
                    missingDevice.setLatitude(customLocation.getLatitude());
                    missingDevice.setLongitude(customLocation.getLongitude());

                    if(host == null)
                        return;

                    float distanceDifference = missingDevice.distanceTo(host);
                    //Distance is less than 500 meters
                    //if(distanceDifference < 500)
                        connectToBleDevice(missingDeviceMacAddress);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                notifsReceiver, new IntentFilter(NOTIFICATION_BROADCAST_ACTION));
    }

    private void setUpRetrofit() {

        retrofit = new RetrofitConstants();
    }

    private void setUpGps() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    host = host == null ? new Location("Your location") : location;
                    host.setLatitude(location.getLatitude());
                    host.setLongitude(location.getLongitude());
                }
            }
        };

        PermissionChecker.checkPermission(this, ACCESS_FINE_LOCATION);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,Looper.getMainLooper());
    }

    private void toast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBleServiceReady() {
        isDeviceReady = true;
    }

    @Override
    public void onDeviceDisconnected() {}

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                log.info("Connected");
                toast("Connected! notifying owner.");
                break;
            case isConnecting:
                log.info("Connecting");
                toast("Connecting");
                break;
            case isToScan:
                log.info("Scan");
                break;
            case isScanning:
                log.info("Scanning");
                break;
            case isDisconnecting:
                log.info("Is Disconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {
        log.info(theString);

        notifyOwner(theString);
    }


    private void notifyOwner(String theString) {

        try {
            //TODO
            //Update condition lol
            if(theString.length() == 20 && !hasTriggered) {

                String[] locString = theString.split(",", 2);

                String lat = locString[0].replace("\"", "");
                String lng = locString[1].replace("\"", "");

                Double dlat = Double.parseDouble(lat);
                Double dlng = Double.parseDouble(lng);

                NotificationEndpoint apiService = retrofit
                        .getRetrofit()
                        .create(NotificationEndpoint.class);

                Note modal = new Note();
                modal.setSubject("Alert!!");
                modal.setContent("Your pet was last found at this location.");
                modal.setImage("");
                HashMap<String,String> hm = new HashMap<String, String>() {{
                    put("latitude", lat);
                    put("longitude", lng);
                    put("owner_uid", owner_uid);
                    put("action", ACTION_FOUND);
                }};
                modal.setData(hm);

                Call<String> call = apiService.triggerLostPet("",MISSING_DEVICES_TOPIC,modal);

                log.info("Trigger backend");
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String test = response.body();
                        log.info("Success! \n" + test);
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        String test = call.request().toString();
                        log.info("Error! \n" + test);
                    }
                });

                hasTriggered = true;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        onPauseProcess();
        onStopProcess();
        onDestroyProcess();
        try {
            unregisterReceiver(notifsReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


}
