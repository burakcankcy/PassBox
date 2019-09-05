package com.burakcankocyigit.passbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.burakcankocyigit.passbox.common.common.logger.Log;
import com.burakcankocyigit.passbox.common.common.logger.LogFragment;
import com.burakcankocyigit.passbox.common.common.logger.LogWrapper;
import com.burakcankocyigit.passbox.common.common.logger.MessageOnlyLofFilter;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private boolean mLogShown;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION=99;



    //int request_code_for_enabling_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
     /*   Thread serverThread = new ServerBTConnection(bluetoothAdapter);
        serverThread.run();
        Thread clientThread= new ClientBtConnection(bluetoothDevice);
        clientThread.run();*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.some_layout);
        if (savedInstanceState==null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            PassBoxFragment fragment = new PassBoxFragment();
            transaction.replace(R.id.sample_content_fragment,fragment);
            transaction.commit();

        }
        checkLocationPermission();
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate( R.menu.main,menu);
    return true;
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? "Hide Log" : "Show Log");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown= !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void initializeLogging(){
        {
            // Wraps Android's native log framework.
            LogWrapper logWrapper = new LogWrapper();
            // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
            Log.setLogNode(logWrapper);

            // Filter strips out everything except the message text.
            MessageOnlyLofFilter msgFilter = new MessageOnlyLofFilter();
            logWrapper.setNext(msgFilter);

            // On screen logging via a fragment with a TextView.
            LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.log_fragment);
            msgFilter.setNext(logFragment.getLogView());

            Log.i(TAG, "Ready");
        }
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this).setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            }
            else {ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);

            }
            return false;

        }else {return true;}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode){
        case MY_PERMISSIONS_REQUEST_LOCATION:{
            if (grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                }
            }
                else {
                Toast.makeText(this, "Bluetooth Bağlantısı İçin Konum Servislerini Açınız", Toast.LENGTH_SHORT).show();

                }

                return;

                }

            }
        }
    }














