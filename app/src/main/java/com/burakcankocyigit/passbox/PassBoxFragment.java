package com.burakcankocyigit.passbox;

        import android.annotation.SuppressLint;
        import android.annotation.TargetApi;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothSocket;
        import android.content.Context;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.ActionBar;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.fragment.app.Fragment;
        import androidx.fragment.app.FragmentActivity;
        import androidx.fragment.app.FragmentTransaction;

        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.EditorInfo;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.lang.reflect.Array;
        import java.security.Key;


public class PassBoxFragment extends Fragment {

    private static final String Tag="PassBoxFragment";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private ListView mConservationView;
    private EditText mOutEditText;
    private Button mConnectButton;

    private String mConnectedDeviceName=null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapte=null;


    public PassBoxService mPassBoxService=null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mBluetoothAdapte=BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapte==null){

            FragmentActivity activity =getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }



    }


    public void onStart(){
        super.onStart();
        if (!mBluetoothAdapte.isEnabled()){
            Intent enableIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
        else if (mPassBoxService==null){
            setupChat();
          //  mPassBoxService=new PassBoxService(getActivity(),mHandler);
        }


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPassBoxService!=null){
            mPassBoxService.stop();

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mPassBoxService!=null){

            if (mPassBoxService.getState()== PassBoxService.STATE_NONE){
                mPassBoxService.start();

            }
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_pass_box, container, false);
    }

    @Override
    public void onViewCreated( View view, @Nullable Bundle savedInstanceState) {
        mConservationView=view.findViewById(R.id.fragment_listview);
        // mOutEditText =view.findViewById(R.id.edit_text_out);
        mConnectButton=view.findViewById(R.id.button_connect);
    }
    private void setupChat(){
        Log.d(Tag,"setupChat()");
        mPassBoxService=new PassBoxService(getActivity(),mHandler);

    }

    public void ensureDiscoverable(){
        if (mBluetoothAdapte.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }
    private void setStatus(int resId){
        AppCompatActivity activity = (MainActivity) getActivity();
        if (null==activity) {
            return;
        }
        final ActionBar actionBar=activity.getSupportActionBar();
        if (null==actionBar){
            return;
        }
        actionBar.setSubtitle(resId);

    }
    private void setStatus(CharSequence subTitle){
        AppCompatActivity activity= (MainActivity)getActivity();
        if (null==activity){
            return;
        }
        final ActionBar actionBar= activity.getSupportActionBar();
        if (null==actionBar){
            return;
        }
        actionBar.setSubtitle(subTitle);
    }
    @SuppressLint("HandlerLeak")
    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage( Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constans.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case PassBoxService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));


                            break;
                        case PassBoxService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case PassBoxService.STATE_LISTEN:
                        case PassBoxService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;

                case Constans.MESSAGE_WRITE:


                    byte[] writeBf = (byte[]) msg.obj;
                    String writemessage = new String(writeBf);
                    mConversationArrayAdapter.add("Me:" + writemessage);
                    break;
                case Constans.MESSAGE_READ:

                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ": " + readMessage);
                    break;
                case Constans.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constans.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constans.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constans.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

            }

        }
    };




    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                  setupChat();



                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(Tag, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVİCE_ADRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {

        // Get the device MAC address

            String address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVİCE_ADRESS);
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapte.getRemoteDevice(address);
            // Attempt to connect to the device
            mPassBoxService.connect(device, secure);




    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.passbox, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

}



