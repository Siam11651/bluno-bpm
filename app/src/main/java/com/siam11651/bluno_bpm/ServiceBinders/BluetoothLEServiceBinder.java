package com.siam11651.bluno_bpm.ServiceBinders;

import android.os.Binder;

import com.siam11651.bluno_bpm.Services.BluetoothLEService;

public class BluetoothLEServiceBinder extends Binder
{
    private final BluetoothLEService bluetoothLEService;

    public BluetoothLEServiceBinder(BluetoothLEService bluetoothLEService)
    {
        this.bluetoothLEService = bluetoothLEService;
    }

    public BluetoothLEService GetService()
    {
        return bluetoothLEService;
    }
}
