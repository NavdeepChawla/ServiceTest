package com.navdeep.servicetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestLocationPermission();

        Intent intent=new Intent(this,TestService.class);
        startForegroundService(intent);

        String bluetoothAdapter;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter().getAddress();
        TextView textView=findViewById(R.id.textView);
        textView.setText(bluetoothAdapter);


    }

    private void requestLocationPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Permission")
                        .setMessage("Please kindly select the permission for always")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();
            }
        }
        else
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }

    private void requestSMSPermission()
    {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 1002);
        }
    }

    private void requestStoragePermission()
    {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1003);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1001:if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                      {
                          requestLocationPermission();
                      }
                      else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                      {
                          requestSMSPermission();
                      }
                      break;
            case 1002:if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                      {
                            requestSMSPermission();
                      }
                      else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                      {
                          requestStoragePermission();
                      }
                      break;
            case 1003:if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                      {
                          requestStoragePermission();
                      }
                      break;
        }
    }
}