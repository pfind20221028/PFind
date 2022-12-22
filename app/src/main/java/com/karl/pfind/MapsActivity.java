/*
package com.karl.pfind;

import static com.karl.Pfind.Constants.BluetoothConstants.APP_NAME;
import static com.karl.Pfind.Constants.BluetoothConstants.BLUETOOTH_CONNECT;
import static com.karl.Pfind.Constants.BluetoothConstants.MY_UUID;
import static com.karl.Pfind.Constants.BluetoothConstants.REQUEST_ENABLE_BT;
import static com.karl.Pfind.Constants.BluetoothConstants.STATE_CONNECTED;
import static com.karl.Pfind.Constants.BluetoothConstants.STATE_CONNECTING;
import static com.karl.Pfind.Constants.BluetoothConstants.STATE_CONNECTION_FAILED;
import static com.karl.Pfind.Constants.BluetoothConstants.STATE_LISTENING;
import static com.karl.Pfind.Constants.BluetoothConstants.STATE_MESSAGE_RECEIVED;
import static com.karl.Pfind.Constants.GPSConstants.ACCESS_FINE_LOCATION;
import static com.karl.Pfind.Constants.GPSConstants.ZOOM_LEVEL;

import androidx.fragment.app.FragmentActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karl.pfind.Models.CustomLocation;
import com.karl.pfind.Models.Owner;
import com.karl.pfind.bluno.BlunoLibrary;
import com.karl.pfind.databinding.ActivityMapsBinding;
import com.karl.pfind.Permissions.PermissionChecker;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Context ctx;

    private Button listen, listDevices;
    private ListView listView;
    private TextView uiLogger, status;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice[] btArray;
    BluetoothService bluetoothService;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location location,target;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private MarkerOptions hostMarker,targetMarker;
    private Boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ctx = this;
        findViewByIds();
        setUpBluetooth();
        implementListeners();
        setupGps();
    }

    private void findViewByIds() {
        listen = findViewById(R.id.listen);
        listView = findViewById(R.id.listview);
        uiLogger = findViewById(R.id.msg);
        status = findViewById(R.id.status);
        listDevices = findViewById(R.id.listDevices);
    }

    private void setUpBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            PermissionChecker.checkPermission(ctx, BLUETOOTH_CONNECT);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private void implementListeners() {

        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionChecker.checkPermission(ctx, BLUETOOTH_CONNECT);
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings=new String[bt.size()];
                btArray=new BluetoothDevice[bt.size()];
                int index=0;

                if( bt.size()>0)
                {
                    for(BluetoothDevice device : bt)
                    {
                        btArray[index]= device;
                        strings[index]=device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerClass serverClass=new ServerClass();
                serverClass.start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ClientClass clientClass=new ClientClass(btArray[i]);
                clientClass.start();

                status.setText("Connecting");
            }
        });

    }

    private void setupGps() {
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
                    setLocations(location);
                }
            }
        };
    }

    private void startLocationUpdates() {

        PermissionChecker.checkPermission(ctx, ACCESS_FINE_LOCATION);

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void setLocations(Location loc) {
        //TODO
        //setup GPS permissions

        location = location == null ? new Location("server") : location;

        location.setLatitude(loc.getLatitude());
        location.setLongitude(loc.getLongitude());

        uiLogger.setText(
                uiLogger.getText() + "\n " +
                        "Server (lat): " + loc.getLatitude() + "\n" +
                        "Server (long)" + loc.getLongitude());

        setMarker();
    }

    private void setMarker() {

        if(isMapReady) {

            if(hostMarker == null && location != null) {
                LatLng h = new LatLng(location.getLatitude(),location.getLongitude());
                hostMarker = new MarkerOptions();
                hostMarker.title("hostMarker");
                hostMarker.position(h);
                hostMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_owner_48px));
                mMap.addMarker(hostMarker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(h, ZOOM_LEVEL));
            }

            if(targetMarker == null && target != null) {
                LatLng t = new LatLng(target.getLatitude(),target.getLongitude());
                targetMarker = new MarkerOptions();
                targetMarker.title("targetMarker");
                targetMarker.position(t);
                targetMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.dog_walking_48px));
                mMap.addMarker(targetMarker);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL));
            }

            if(hostMarker != null && location != null) {
                hostMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            if(targetMarker != null && target != null) {
                LatLng t = new LatLng(target.getLatitude(),target.getLongitude());
                targetMarker.position(t);
                CameraUpdateFactory.newLatLngZoom(t, ZOOM_LEVEL);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    */
/**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *//*


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        isMapReady = true;
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    //String tempMsg=new String(readBuff,0,msg.arg1);

                    CustomLocation loc = (CustomLocation) SerializationUtils.deserialize(readBuff);

                    target = target == null ? new Location("client") : target;

                    target.setLatitude(loc.getLatitude());
                    target.setLongitude(loc.getLongitude());

                    Float distanceToTarget = location.distanceTo(target);

                    String tempMsg =
                            uiLogger.getText() + "\n " +
                                    "Client (lat): " + loc.getLatitude() + "\n" +
                                    "Client (long)" + loc.getLongitude() + "\n\n" +
                                    "Distance: " + distanceToTarget + " meters;";

                    uiLogger.setText(tempMsg);

                    setMarker();
                    break;
            }
            return true;
        }
    });

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                PermissionChecker.checkPermission(ctx, BLUETOOTH_CONNECT);
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket=null;

            while (socket==null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);

                    */
/*TODO
                     * Notify nearby users
                     * *//*

                }

                if(socket!=null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    bluetoothService=new BluetoothService(socket);
                    bluetoothService.start();

                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;

            try {
                PermissionChecker.checkPermission(ctx, BLUETOOTH_CONNECT);
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                PermissionChecker.checkPermission(ctx, BLUETOOTH_CONNECT);
                socket.connect();
                Message message=Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                bluetoothService = new BluetoothService(socket);
                bluetoothService.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message message=Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class BluetoothService extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public BluetoothService (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}*/
