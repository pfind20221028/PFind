package com.karl.pfind;

import static com.karl.Pfind.Constants.GPSConstants.ACCESS_FINE_LOCATION;
import static com.karl.Pfind.Constants.GPSConstants.ZOOM_LEVEL;
import static com.karl.pfind.BroadcastReceiver.BroadcastConstants.NOTIFICATION_BROADCAST_ACTION;
import static com.karl.pfind.Constants.FirebaseConstants.ACTION_FOUND;
import static com.karl.pfind.Constants.FirebaseConstants.ACTION_MISSING;
import static com.karl.pfind.Constants.FirebaseConstants.MISSING_DEVICES_TOPIC;
import static com.karl.pfind.ui.login.LoginConstants.SUCCESS;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karl.pfind.Constants.RetrofitConstants;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.Models.PFindDevice;
import com.karl.pfind.Permissions.PermissionChecker;
import com.karl.pfind.Services.MissingDeviceConnectorService;
import com.karl.pfind.bluno.BlunoLibrary;
import com.karl.pfind.bluno.TestBleService;
import com.karl.pfind.http.Note;
import com.karl.pfind.http.NotificationEndpoint;
import com.karl.pfind.ui.login.LoginViewModel;
import com.karl.pfind.ui.register.BleDevice;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends BlunoLibrary implements OnMapReadyCallback {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private Context ctx;

    private CardView buttonScan,cv_missing_pet_info,buttonMissing;
    private ImageView imgLogout;
    private TextView ui_logger,txt_ble_status,tv_missing_pet_message,
                        tv_account_info,txt_acc_email,txt_acc_role;

    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker hostMarker,targetMarker,missingDeviceMarker;
    private Location host,target,missingDevice;
    private Boolean isMapReady = false,
                    panToUserOnce = false,
                    isDeviceConnected = false;

    private LoginViewModel loginViewModel;
    private String deviceMacAddress,missingDeviceMacAddress;

    private RetrofitConstants retrofit;

    private BroadcastReceiver notifsReceiver;

    private FirebaseUser user;

    private ProgressDialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ctx = this;
        showLoadingScreen(true);
        initViewModels();
        retrieveUserDeviceMacAddress();
        setupBle();
        setupGps();
        initViews();
        initListeners();
        setUpMaps();
        retrieveFirebaseToken();
        setUpRetrofit();
        //setUpBroadcastReceiver();
        setUpMissingPetFoundReceiver();
        setUpAccountInfo();
        showLoadingScreen(false);
    }

    private void showLoadingScreen(Boolean b) {
        if(b) {
            dialog = ProgressDialog.show(this, "Loading",
                    "Loading. Please wait...", true);
            if(!dialog.isShowing())
                dialog.show();
        } else {
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }

    private void initViewModels() {
        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);
    }

    private void retrieveUserDeviceMacAddress() {

        loginViewModel
                .findDeviceByUser()
                .observe(this, result -> {
                    if(result != null) {
                        if(result.getResultMessage().equals(SUCCESS)) {
                            try {
                                PFindDevice d = (PFindDevice) result.getResult();
                                BleDevice bd = d.getBleDevice();

                                deviceMacAddress = bd.getMacAddress();
                                //txt_acc_role.setText("Pet owner");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            log.info(result.getResultMessage());
                            buttonScan.setVisibility(View.GONE);
                            buttonMissing.setVisibility(View.GONE);
                            //txt_acc_role.setText("Normal user");
                        }
                    } else {
                        log.info(result.getResultMessage());
                    }
                });
    }

    private void setupBle() {
        dialog.setMessage("Setting up Bluetooth. Please wait...");

        request(1000, new OnPermissionsResult() {
            @Override
            public void OnSuccess() {
                toast("Permission request successful");
            }

            @Override
            public void OnFail(List<String> noPermissions) {
                toast("Permission request failed");
            }
        });

        onCreateProcess();														//onCreate Process by BlunoLibrary

        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200
    }

    private void setupGps() {
        dialog.setMessage("Retrieving your location. Please wait...");

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

                    setMarkers();

                    if(!panToUserOnce) {
                        panCameraToUserLocation();
                        panToUserOnce = true;
                    }
                }
            }
        };
    }

    private void panCameraToUserLocation() {
        if(host == null) {
            toast("Please wait before a user finds the device");
            return;
        }
        LatLng t = new LatLng(host.getLatitude(),host.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));
    }

    private void initViews() {
        buttonScan = (CardView) findViewById(R.id.cv_scan);					//initial the button for scanning the BLE device
        cv_missing_pet_info = (CardView) findViewById(R.id.cv_missing_pet_info);					//initial the button for scanning the BLE device
        buttonMissing = (CardView) findViewById(R.id.buttonMissing);					//initial the button for scanning the BLE device
        ui_logger = (  TextView) findViewById(R.id.ui_logger);
        txt_ble_status = (TextView) findViewById(R.id.txt_ble_status);
        tv_account_info = (TextView) findViewById(R.id.tv_account_info);
        txt_acc_email = (TextView) findViewById(R.id.txt_acc_email);
        txt_acc_role = (TextView) findViewById(R.id.txt_acc_role);
        tv_missing_pet_message = (TextView) findViewById(R.id.tv_missing_pet_message);
        imgLogout = (ImageView) findViewById(R.id.img_logout);
    }

    private void initListeners() {

        buttonScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //buttonScanOnClickProcess();
                connectToBleDevice(deviceMacAddress);
            }
        });

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder
                        .setMessage("Do you want to logout?")
                        .setTitle("Logout")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loginViewModel.signOut();
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        buttonMissing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder
                        .setMessage("Your pet device was disconnected to your phone. Is your pet lost?")
                        .setTitle("Alert")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                triggerNotificationsApi();
                                toast("Notification success!");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                buttonMissing.setVisibility(View.GONE);
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        cv_missing_pet_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panCameraToMissingDeviceLocation();
            }
        });
    }

    private void panCameraToMissingDeviceLocation() {
        if(missingDevice == null) {
            toast("Please wait before a user finds the device");
            return;
        }
        LatLng t = new LatLng(missingDevice.getLatitude(),missingDevice.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));
    }

    private void setUpMaps() {
        dialog.setMessage("Setting up maps. Please wait...");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pet_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void retrieveFirebaseToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {

                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            log.info("Fetching FCM registration token failed" + task.getException());
                            return;
                        }

                        String token = task.getResult();

                        log.info(token);
                    }

                });

        FirebaseMessaging.getInstance().subscribeToTopic(MISSING_DEVICES_TOPIC)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        log.info(msg);
                    }
                });
    }

    private void setUpRetrofit() {

        retrofit = new RetrofitConstants();
    }

    private void setUpBroadcastReceiver() {

        notifsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String macAddress = intent.getStringExtra("mac_address");
                Bundle b = intent.getBundleExtra("Location");
                CustomLocation customLocation = b.getParcelable("missingDeviceLocation");

                missingDeviceMacAddress = macAddress;

                missingDevice = missingDevice == null ? new Location("missingDevice") : missingDevice;
                missingDevice.setLatitude(customLocation.getLatitude());
                missingDevice.setLongitude(customLocation.getLongitude());

                checkDistanceFromMissingDevice();
                setMarkers();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                notifsReceiver, new IntentFilter(NOTIFICATION_BROADCAST_ACTION));
    }

    private void setUpMissingPetFoundReceiver() {

        notifsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getStringExtra("action");
                String owner = intent.getStringExtra("owner_uid");
                if(action.equals(ACTION_FOUND)
                        //&& owner.equals(user.getUid())
                ) {

                    String macAddress = intent.getStringExtra("mac_address");
                    Bundle b = intent.getBundleExtra("Location");
                    CustomLocation customLocation = b.getParcelable("missingDeviceLocation");

                    missingDeviceMacAddress = macAddress;

                    missingDevice = missingDevice == null ? new Location("missingDevice") : missingDevice;
                    missingDevice.setLatitude(customLocation.getLatitude());
                    missingDevice.setLongitude(customLocation.getLongitude());

                    checkDistanceFromMissingDevice();
                    setMarkers();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                notifsReceiver, new IntentFilter(NOTIFICATION_BROADCAST_ACTION));
    }

    private void setUpAccountInfo() {
        dialog.setMessage("Retrieving user info. Please wait...");

        user = loginViewModel.getUserSession();
        txt_acc_email.setText(user.getEmail());
    }

    @Override
    protected void onPause() {
        super.onPause();

        onPauseProcess();														//onPause Process by BlunoLibrary
        onStopProcess();
        onDestroyProcess();
        startBackgroundService();
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, MissingDeviceConnectorService.class);
        //Intent serviceIntent = new Intent(this, TestBleService.class);
        startService(serviceIntent);
    }

    protected void onResume(){
        super.onResume();

        onResumeProcess();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        PermissionChecker.checkPermission(ctx, ACCESS_FINE_LOCATION);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onStop() {
        super.onStop();

        onStopProcess();														//onStop Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
        try {
            unregisterReceiver(notifsReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBleServiceReady() {
        connectToBleDevice(deviceMacAddress);
    }

    public void onDeviceDisconnected() {
        buttonMissing.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                isDeviceConnected = true;
                txt_ble_status.setText("Connected");
                break;
            case isConnecting:
                txt_ble_status.setText("Connecting");
                break;
            case isToScan:
                if(isDeviceConnected)
                    onDeviceDisconnected();
                txt_ble_status.setText("Scan");
                break;
            case isScanning:
                txt_ble_status.setText("Scanning");
                break;
            case isDisconnecting:
                txt_ble_status.setText("Is Disconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called

        try {
            if(theString.equals("INVALID LOCATION")) {

                return;
            }

            if(theString.length() == 20) {

                //if(isSearchingForMissingDevice) {
                //    missingDevice = missingDevice == null ? new Location("Missing Device") : missingDevice;
                //} else {
                    target = target == null ? new Location("Your pet") : target;
                //}

                String[] locString = theString.split(",", 2);

                String lat = locString[0].replace("\"", "");
                String lng = locString[1].replace("\"", "");

                Double dlat = Double.parseDouble(lat);
                Double dlng = Double.parseDouble(lng);

                //if(isSearchingForMissingDevice) {
                //    missingDevice.setLatitude(dlat);
                //    missingDevice.setLongitude(dlng);
                //} else {
                    target.setLatitude(dlat);
                    target.setLongitude(dlng);
                //}

                ui_logger.setText(ui_logger.getText() + theString + "\n");
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(dlat,dlng),
                                ZOOM_LEVEL)
                );
                setMarkers();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {

        mMap = gMap;
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // TODO
                // setMarkers();
            }
        });
        isMapReady = true;
    }

    private void toast(String s) {
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }

    private void checkDistanceFromMissingDevice() {

        if(host == null)
            toast("Could not retrieve host device location.");

        cv_missing_pet_info.setVisibility(View.VISIBLE);
        //tv_missing_pet_message.setText("A missing pet is near your location. (" + host.distanceTo(missingDevice)+ " meters)");
        //connectToBleDevice(missingDeviceMacAddress);
    }

    private void setMarkers() {

        BitmapDescriptor targetIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        BitmapDescriptor missingIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        BitmapDescriptor hostIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        if(isMapReady) {

            if(target != null) {
                if(targetMarker != null)
                    targetMarker.remove();
                LatLng t = new LatLng(target.getLatitude(),target.getLongitude());
                MarkerOptions tmo = new MarkerOptions();
                tmo.title("Your Pet");
                tmo.position(t);
                //tmo.icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_walking_48px));
                tmo.icon(targetIcon);
                tmo.draggable(false);
                targetMarker = mMap.addMarker(tmo);
                /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));*/
            }

            if(missingDevice != null) {
                if(missingDeviceMarker != null)
                    missingDeviceMarker.remove();
                LatLng t = new LatLng(missingDevice.getLatitude(),missingDevice.getLongitude());
                MarkerOptions tmo = new MarkerOptions();
                tmo.title("Missing pet found!");
                tmo.position(t);
                tmo.draggable(false);
                tmo.icon(missingIcon);
                missingDeviceMarker = mMap.addMarker(tmo);
                /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));*/
            }

            if(host != null) {
                if(hostMarker != null)
                    hostMarker.remove();
                LatLng t = new LatLng(host.getLatitude(),host.getLongitude());
                MarkerOptions tmo = new MarkerOptions();
                tmo.title("Your location");
                tmo.position(t);
                tmo.draggable(false);
                tmo.icon(hostIcon);
                //tmo.icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_owner_48px));
                hostMarker = mMap.addMarker(tmo);
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));
            }

        }
    }

    private void triggerNotificationsApi() {

        //TODO
        if(target == null) {
            toast("Pet's last location is not set.");
            return;
        }

        NotificationEndpoint apiService = retrofit
                .getRetrofit()
                .create(NotificationEndpoint.class);

        Note modal = new Note();
        //modal.setSubject("Alert!!");
        //modal.setContent("A missing dog is near you.");
        //modal.setImage("");
        HashMap<String,String> hm = new HashMap<String, String>() {{
            put("latitude", String.valueOf(target.getLatitude()));
            put("longitude", String.valueOf(target.getLongitude()));

            /*Test
            put("latitude", "14.5825552");
            put("longitude", "120.9824323");
            */
            put("mac_address", deviceMacAddress);
            put("owner_uid", loginViewModel.getUserSession().getUid());
            put("action", ACTION_MISSING);
        }};
        modal.setData(hm);

        Call<String> call = apiService.triggerLostPet("",MISSING_DEVICES_TOPIC,modal);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String test = response.body();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String test = call.request().toString();
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder
                .setMessage("Close the app?")
                .setTitle("Exit")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
