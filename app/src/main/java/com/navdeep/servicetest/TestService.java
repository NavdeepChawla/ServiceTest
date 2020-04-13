package com.navdeep.servicetest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static java.util.Locale.US;


public class TestService extends Service {

    private static final String TAG = "";
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    BluetoothAdapter mBluetoothAdapter;

    //Broadcast Receiver
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");
            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBTDevices.contains(device)) {
                    mBTDevices.add(device);
                }
            }
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //For Notification of Service.
        String channelID= UUID.randomUUID().toString();
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID, "ServiceTest", NotificationManager.IMPORTANCE_NONE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
        Notification notification =new NotificationCompat.Builder(getApplicationContext(),channelID).setContentTitle("Test").build();
        //To keep the service alive
        startForeground(startId,notification);

        //Bluetooth Adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }

        //For Location
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        //Countdown Timer
        final CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                //TimeStamp
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", US);
                String time = simpleDateFormat.format(new Date());

                //Database Reference
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child(time);

                //Fetch Location
                String latitude="";
                String longitude="";
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO
                    }
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    assert location != null;
                    latitude=String.valueOf(location.getLatitude());
                    longitude=String.valueOf(location.getLongitude());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //Disconnecting Bluetooth Adapter
                try{
                    unregisterReceiver(mBroadcastReceiver3);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //Pushing Data on Firebase
                try{
                    String temp=Integer.toString(mBTDevices.size());
                    Toast.makeText(getApplicationContext(),temp,Toast.LENGTH_SHORT).show();
                    databaseReference.child("Location").child("Latittude").setValue(latitude);
                    databaseReference.child("Location").child("Longitude").setValue(longitude);
                    if(mBTDevices.size()!=0)
                    {
                        for(int i=0;i<mBTDevices.size();i++)
                        {
                            String macAddress=mBTDevices.get(i).getAddress();
                            Toast.makeText(getApplicationContext(),macAddress,Toast.LENGTH_SHORT).show();
                            databaseReference.child("MAC").child("Address-"+ i).setValue(macAddress);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //Restarting the service
                Intent restartService = new Intent(getApplicationContext(),TestService.class);
                startForegroundService(restartService);
            }
        };
        countDownTimer.start();
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try
        {
            unregisterReceiver(mBroadcastReceiver3);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Intent restartService = new Intent(getApplicationContext(),TestService.class);
        startForegroundService(restartService);
        super.onDestroy();
    }
}
