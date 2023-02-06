package com.siam11651.bluno_bpm.Services;

import android.Manifest;
import android.app.IntentService;
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

import com.siam11651.bluno_bpm.GattCallbacks.BlUnoGattCallback;
import com.siam11651.bluno_bpm.ServiceBinders.BluetoothLEServiceBinder;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.TrimmedDevice;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BluetoothLEService extends Service
{
    private final Binder binder;
    private BluetoothGatt bluetoothGatt;
    private BlUnoGattCallback bluetoothGattCallback;

    public BluetoothLEService()
    {
        binder = new BluetoothLEServiceBinder(this);
        bluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public void Connect(TrimmedDevice device)
    {
        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.GetAddress());
        bluetoothGattCallback = new BlUnoGattCallback(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        bluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);

        bluetoothGattCallback.SetBluetoothGatt(bluetoothGatt);
    }

    public void Disconnect()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Connect(BluetoothConnection.GetBluetoothConnection().GetDevice());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
        return (BlUnoGattCallback)bluetoothGattCallback;
    }
}
