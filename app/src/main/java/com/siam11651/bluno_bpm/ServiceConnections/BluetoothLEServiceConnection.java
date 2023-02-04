package com.siam11651.bluno_bpm.ServiceConnections;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.siam11651.bluno_bpm.ServiceBinders.BluetoothLEServiceBinder;
import com.siam11651.bluno_bpm.Services.BluetoothLEService;
import com.siam11651.bluno_bpm.Services.BluetoothLEServiceWrapper;
import com.siam11651.bluno_bpm.Utils.TrimmedDevice;

public class BluetoothLEServiceConnection implements ServiceConnection
{
    private final BluetoothLEServiceWrapper bluetoothLEServiceWrapper;
    private final TrimmedDevice device;

    public BluetoothLEServiceConnection(BluetoothLEServiceWrapper bluetoothLEServiceWrapper, TrimmedDevice device)
    {
        this.device = device;
        this.bluetoothLEServiceWrapper = bluetoothLEServiceWrapper;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        BluetoothLEServiceBinder bluetoothLEServiceBinder = (BluetoothLEServiceBinder)service;

        bluetoothLEServiceWrapper.SetBluetoothLEService(bluetoothLEServiceBinder.GetService());
        bluetoothLEServiceWrapper.GetBluetoothLEService().Connect(device);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        bluetoothLEServiceWrapper.SetBluetoothLEService(null);
    }

    public BluetoothLEServiceWrapper GetBluetoothLEServiceWrapper()
    {
        return bluetoothLEServiceWrapper;
    }
}
