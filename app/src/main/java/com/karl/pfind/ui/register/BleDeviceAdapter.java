package com.karl.pfind.ui.register;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.karl.pfind.R;
import com.karl.pfind.bluno.BlunoLibrary;

import java.util.ArrayList;
import java.util.List;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {

    OnShareClickedListener mCallback;
    private List<BluetoothDevice> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    //private ItemClickListener mClickListener;

    public BleDeviceAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void add(BluetoothDevice b) {
        mData.add(b);
    }

    public void clear() {
        mData.clear();
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public BleDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.listitem_ble_devices, parent, false);
        return new BleDeviceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice ble = mData.get(position);
        holder.device_name.setText(ble.getName() == null ? "Unknown Device" : ble.getName());
        holder.device_address.setText(ble.getAddress());
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return (mData.size());
    }

    public Boolean macAddressExists(String mad) {
        boolean b = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            b = mData.stream().anyMatch(d -> d.getAddress().equals(mad));
        }
        return b;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView device_name;
        TextView device_address;

        ViewHolder(View itemView) {
            super(itemView);
            device_name = itemView.findViewById(R.id.device_name);
            device_address = itemView.findViewById(R.id.device_address);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            TextView s = itemView.findViewById(R.id.device_name);
            TextView s2 = itemView.findViewById(R.id.device_address);
            mCallback.ShareClicked((String) s.getText(),(String) s2.getText());
        }
    }

    public void setOnShareClickedListener(OnShareClickedListener mCallback) {
        this.mCallback = mCallback;
    }

    public interface OnShareClickedListener {
        public void ShareClicked(String deviceName,String url);
    }


}