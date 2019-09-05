package com.burakcankocyigit.passbox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG="DeviceListActivity";
    public static String EXTRA_DEVİCE_ADRESS="device_address";
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDeviceArrayAdapter;
    public ListView pairedListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        setResult(Activity.RESULT_CANCELED);
         Button scanButton = findViewById(R.id.button_scan);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
                view.setVisibility(View.GONE);

            }
        }
        );


        ArrayAdapter<String> pairedDevicesArrayAdapter = new
                ArrayAdapter<String>(this, R.layout.device_name);
        mNewDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        ListView newDevicesListView = findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDeviceArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevice=getResources().getText(R.string.none_paired).toString();
            pairedDevicesArrayAdapter.add(noDevice);

        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter!=null){
            mBtAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }
    public void doDiscovery(){
        Log.d(TAG,"doDiscovery()");
        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning for devices...");
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if (mBtAdapter.isDiscovering()){
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }
    private AdapterView.OnItemClickListener mDeviceClickListener= new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            mBtAdapter.cancelDiscovery();
            String info=((TextView) v).getText().toString();
            String address=info.substring(info.length()-17);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVİCE_ADRESS,address);
            setResult(Activity.RESULT_OK,intent);
        //    av.getChildAt(arg2).setBackgroundColor(Color.GREEN);

            finish();

        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED&& device.getName()!=null) {

                    mNewDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());

                }
                // When discovery is finished, change the Activity title
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDeviceArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDeviceArrayAdapter.add(noDevices);
                }
            }
        }
    };

}
