package com.siam11651.bluno_bpm.GattCallbacks;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import java.nio.charset.StandardCharsets;

public class BlUnoGattCallback extends BluetoothGattCallback
{
    private class BluetoothGattCharacteristicHelper
    {
        BluetoothGattCharacteristic characteristic;
        String characteristicValue;

        BluetoothGattCharacteristicHelper(BluetoothGattCharacteristic characteristic, String characteristicValue)
        {
            this.characteristic = characteristic;
            this.characteristicValue = characteristicValue;
        }
    }

    public final static String ACTION_GATT_CONNECTED = "com.siam11651.bluno_bpm.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.siam11651.bluno_bpm.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.siam11651.bluno_bpm.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.siam11651.bluno_bpm.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.siam11651.bluno_bpm.EXTRA_DATA";
    private BluetoothGatt bluetoothGatt;
    private final Context context;

    public BlUnoGattCallback(Context context)
    {
        this.context = context;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
        if(newState == BluetoothProfile.STATE_CONNECTED)
        {
            BroadcastUpdate(ACTION_GATT_CONNECTED);

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

            gatt.discoverServices();
        }
        else if(newState == BluetoothProfile.STATE_DISCONNECTED)
        {
            BroadcastUpdate(ACTION_GATT_DISCONNECTED);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        if(status == BluetoothGatt.GATT_SUCCESS)
        {
            characteristic.setValue("");
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        if(status == BluetoothGatt.GATT_SUCCESS)
        {
            BroadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        BroadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
        if(status == BluetoothGatt.GATT_SUCCESS)
        {
            BroadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        }
    }

    private void BroadcastUpdate(String action)
    {
        Intent intent = new Intent(action);

        context.sendBroadcast(intent);
    }

    public void BroadcastUpdate(String action, BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        Intent intent = new Intent(action);

        byte[] data = bluetoothGattCharacteristic.getValue();

        if(data != null && data.length > 0)
        {
            intent.putExtra(EXTRA_DATA, new String(data));
            context.sendBroadcast(intent);
        }
    }

    public void SetBluetoothGatt(BluetoothGatt bluetoothGatt)
    {
        this.bluetoothGatt = bluetoothGatt;
    }

    public BluetoothGatt GetBluetoothGatt()
    {
        return bluetoothGatt;
    }

    public void ReadCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic)
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

        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    public void WriteCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        String string = new String(bluetoothGattCharacteristic.getValue(), StandardCharsets.ISO_8859_1);

        // characteristicRingBuffer.push(new BluetoothGattCharacteristicHelper(bluetoothGattCharacteristic, string));
        // onCharacteristicWrite(bluetoothGatt, bluetoothGattCharacteristic, WRITE_NEW_CHARACTERISTIC);

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

        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    public void SetCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean enable)
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

        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, enable);
    }
}
