package com.siam11651.bluno_bpm.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.siam11651.bluno_bpm.ConnectedActivity;
import com.siam11651.bluno_bpm.GattCallbacks.BlUnoGattCallback;
import com.siam11651.bluno_bpm.R;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.TrimmedDevice;

import java.util.List;

public class BluetoothLEService extends Service
{
    private boolean connected;
    private BluetoothGatt bluetoothGatt;
    private BlUnoGattCallback bluetoothGattCallback;

    public BluetoothLEService()
    {
        bluetoothGatt = null;
        connected = false;
    }

    @SuppressLint("MissingPermission")
    public void Connect(TrimmedDevice device)
    {
        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.GetAddress());
        bluetoothGattCallback = new BlUnoGattCallback(this);
        bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);

        bluetoothGattCallback.SetBluetoothGatt(bluetoothGatt);

        connected = true;
    }

    @SuppressLint("MissingPermission")
    public void Disconnect()
    {
        connected = false;

        if(bluetoothGatt == null)
        {
            return;
        }

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(!connected)
        {
            NotificationChannel notificationChannel = new NotificationChannel("BPM Channel", "BPM Forground Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notificationBuilder = new Notification.Builder(this, "BPM Channel");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, ConnectedActivity.class), PendingIntent.FLAG_IMMUTABLE);

            notificationBuilder.setContentTitle("BPM Data Incoming");
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
            notificationBuilder.setContentIntent(pendingIntent);
            startForeground(1, notificationBuilder.build());

            Connect(BluetoothConnection.GetBluetoothConnection().GetDevice());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public List<BluetoothGattService> GetSupportedGattServices()
    {
        if(bluetoothGatt == null)
        {
            return null;
        }

        return bluetoothGatt.getServices();
    }

    public BlUnoGattCallback GetBluetoothGattCallback()
    {
        return bluetoothGattCallback;
    }
}
