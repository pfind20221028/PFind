package com.karl.pfind.ui.register;

import static com.karl.pfind.ui.login.LoginConstants.LOGIN_SUCCESS;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karl.pfind.MapActivity;
import com.karl.pfind.bluno.BlunoLibraryFragment;
import com.karl.pfind.databinding.FragmentRegisterDeviceBinding;
import com.karl.pfind.ui.login.LoginViewModel;

import java.util.ArrayList;
import java.util.List;

public class RegisterDeviceFragment extends BlunoLibraryFragment {

    private Context ctx;
    //UPDATE THIS IF COPIED
    private FragmentRegisterDeviceBinding binding;
    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentRegisterDeviceBinding.inflate(inflater, container, false);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        ctx = getActivity();

        request(1000, new BlunoLibraryFragment.OnPermissionsResult() {
            @Override
            public void OnSuccess() {
                Toast.makeText(ctx,"Permission request successful",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFail(List<String> noPermissions) {
                Toast.makeText(ctx,"Permission request failed",Toast.LENGTH_SHORT).show();
            }
        });

        onCreateProcess();
        serialBegin(115200);

        return binding.getRoot();
    }

    RecyclerView recyclerView;
    TextView txtBleStatus;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = binding.rvBle;
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        mLeDeviceListAdapter.setOnShareClickedListener(new BleDeviceAdapter.OnShareClickedListener() {
            @Override
            public void ShareClicked(String name,String macAd) {
                //connectToMacAddress(macAd);

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder
                        .setMessage("Do you want to register this device (" + name + ")?.")
                        .setTitle("Register Device")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                loginViewModel
                                        .registerDevice(name,macAd)
                                        .observe(getActivity(), result -> {
                                            if(result != null) {
                                                if(result.getResultMessage().equals(LOGIN_SUCCESS)) {
                                                    Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                                                    redirectToMaps();
                                                } else {
                                                    Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                                            }
                                        });
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
        recyclerView.setAdapter(mLeDeviceListAdapter);

        binding.imgScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonScanOnClickProcess();
            }
        });

        txtBleStatus = binding.txtBleStatus;
    }

    private void testSaveToUser() {
        //Save to user
        loginViewModel
                .registerDevice("Bluno","78:DB:2F:BF:30:E5")
                .observe(getActivity(), result -> {
                    if(result != null) {
                        if(result.getResultMessage().equals(LOGIN_SUCCESS)) {
                            Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                            redirectToMaps();
                        } else {
                            Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ctx,result.getResultMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectToMaps() {
        Intent mapsActivity = new Intent(getActivity(), MapActivity.class);
        startActivity(mapsActivity);
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theconnectionStateEnum) {
        switch (theconnectionStateEnum) {											//Four connection state
            case isConnected:
                txtBleStatus.setText("Device Connected!");
                break;
            case isConnecting:
                txtBleStatus.setText("Connecting");
                break;
            case isToScan:
                //txtBleStatus.setText("Scan");
                break;
            case isScanning:
                txtBleStatus.setText("Scanning for Ble device");
                break;
            case isDisconnecting:
                txtBleStatus.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {

    }
}
