package com.navdeep.servicetest;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestService extends Service {

    private static final String TAG = "";
    private ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

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
        BluetoothAdapter mBluetoothAdapter;
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
                //Firebase Auth
                //final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //final String number =user.getPhoneNumber();
                final String number ="+919911008666";


                //Database Reference
                FirebaseFirestore firestore=FirebaseFirestore.getInstance();
                Map<String,Object> Data=new HashMap<>();

                //TimeStamp
                Date currentTimeobj = Calendar.getInstance().getTime();
                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentTimeobj);
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentTimeobj);
                String currentDateandTime = currentDate+" at "+currentTime;
                long dateInsecs = (currentTimeobj.getTime())/1000;
                Data.put("TimeStamps",currentDateandTime);

                //Location
                List<Double> exactLocation=new ArrayList<>();
                double latitude=0.0;
                double longitude=0.0;
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO
                    }
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location!=null)
                    {
                        latitude=location.getLatitude();
                        longitude=location.getLongitude();
                    }
                    else{
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location!=null)
                        {
                            latitude=location.getLatitude();
                            longitude=location.getLongitude();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                exactLocation.add(latitude);
                exactLocation.add(longitude);
                Data.put("Location",exactLocation);

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
                    if(mBTDevices.size()!=0)
                    {
                        List<String> macAddress=new ArrayList<>();
                        for(int i=0;i<mBTDevices.size();i++)
                        {
                            String tempMacAddress=mBTDevices.get(i).getAddress();
                            macAddress.add(tempMacAddress);
                        }
                        Data.put("MacAddress",macAddress);
                    }
                    firestore.collection("Profile").document(number).collection("TimeStamps").document("" + dateInsecs).set(Data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
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
