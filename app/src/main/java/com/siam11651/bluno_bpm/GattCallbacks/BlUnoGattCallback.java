package com.siam11651.bluno_bpm.GattCallbacks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.siam11651.bluno_bpm.MainActivity;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BlUnoGattCallback extends BluetoothGattCallback
{
    private final String password = "AT+PASSWOR=DFRobot\r\n";
    private final String baudRateBuffer = "AT+UART=115200\r\n";
    public static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic sCharacteristic, modelNumberCharactersitic, serialPortCharacteristic, commandCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics;
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

    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
        if(newState == BluetoothProfile.STATE_CONNECTED)
        {
            BroadcastUpdate(ACTION_GATT_CONNECTED);
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
            if(sCharacteristic == modelNumberCharactersitic)
            {
                SetSerialCharacteristic();
            }
            else
            {
                BroadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        if(sCharacteristic == modelNumberCharactersitic)
        {
            SetSerialCharacteristic();
        }
        else
        {
            BroadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
        if(status == BluetoothGatt.GATT_SUCCESS)
        {
            DisplayGattServices(gatt.getServices());
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

    @SuppressLint("MissingPermission")
    public void ReadCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    @SuppressLint("MissingPermission")
    public void WriteCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic)
    {
        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    @SuppressLint("MissingPermission")
    public void SetCharacteristicNotification(BluetoothGattCharacteristic bluetoothGattCharacteristic, boolean enable)
    {
        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, enable);
    }

    private void SetSerialCharacteristic()
    {
        SetCharacteristicNotification(sCharacteristic, false);

        sCharacteristic = commandCharacteristic;

        sCharacteristic.setValue(password);
        WriteCharacteristic(sCharacteristic);
        sCharacteristic.setValue(baudRateBuffer);
        WriteCharacteristic(sCharacteristic);

        sCharacteristic = serialPortCharacteristic;

        SetCharacteristicNotification(sCharacteristic, true);
        ReadCharacteristic(sCharacteristic);
    }

    @SuppressLint("MissingPermission")
    private void DisplayGattServices(List<BluetoothGattService> bluetoothGattServices)
    {
        if(bluetoothGattServices == null)
        {
            return;
        }

        String uuid = null;
        modelNumberCharactersitic = null;
        serialPortCharacteristic = null;
        commandCharacteristic = null;
        gattCharacteristics = new ArrayList<>();

        for(BluetoothGattService bluetoothGattService : bluetoothGattServices)
        {
            uuid = bluetoothGattService.getUuid().toString();
            List<BluetoothGattCharacteristic> tempBluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<>();

            for(BluetoothGattCharacteristic bluetoothGattCharacteristic : tempBluetoothGattCharacteristics)
            {
                charas.add(bluetoothGattCharacteristic);

                uuid = bluetoothGattCharacteristic.getUuid().toString();

                if(uuid.equals(ModelNumberStringUUID))
                {
                    modelNumberCharactersitic = bluetoothGattCharacteristic;
                }
                else if(uuid.equals(SerialPortUUID))
                {
                    serialPortCharacteristic = bluetoothGattCharacteristic;
                }
                else if(uuid.equals(CommandUUID))
                {
                    commandCharacteristic = bluetoothGattCharacteristic;
                }
            }

            gattCharacteristics.add(charas);
        }

        if(modelNumberCharactersitic == null || serialPortCharacteristic == null || commandCharacteristic == null)
        {
            BroadcastUpdate("INVALID_DEVICE");
        }
        else
        {
            sCharacteristic = modelNumberCharactersitic;
            SetCharacteristicNotification(sCharacteristic, true);
            ReadCharacteristic(sCharacteristic);
            BroadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
        }
    }
}
