package com.siam11651.bluno_bpm.Utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.siam11651.bluno_bpm.Services.BluetoothLEService;

public class BluetoothConnection
{
    private TrimmedDevice device;
    private static BluetoothConnection singleton = null;
    private BluetoothLEService bluetoothLEService;

    private BluetoothConnection()
    {
        bluetoothLEService = null;
        device = null;
    }

    public static BluetoothConnection GetBluetoothConnection()
    {
        if(singleton == null)
        {
            singleton = new BluetoothConnection();
        }

        return singleton;
    }

    public void SetDevice(TrimmedDevice device)
    {
        this.device = device;
    }

    public TrimmedDevice GetDevice()
    {
        return device;
    }

    public void Connect(Context context)
    {
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }


    }
}
