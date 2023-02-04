package com.siam11651.bluno_bpm.Services;

public class BluetoothLEServiceWrapper
{
    private BluetoothLEService bluetoothLEService;

    public BluetoothLEServiceWrapper(BluetoothLEService bluetoothLEService)
    {
        SetBluetoothLEService(bluetoothLEService);
    }

    public void SetBluetoothLEService(BluetoothLEService bluetoothLEService)
    {
        this.bluetoothLEService = bluetoothLEService;
    }

    public BluetoothLEService GetBluetoothLEService()
    {
        return bluetoothLEService;
    }
}
