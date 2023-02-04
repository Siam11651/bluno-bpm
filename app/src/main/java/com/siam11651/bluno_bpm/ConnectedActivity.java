package com.siam11651.bluno_bpm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.siam11651.bluno_bpm.GattCallbacks.BlUnoGattCallback;
import com.siam11651.bluno_bpm.ServiceConnections.BluetoothLEServiceConnection;
import com.siam11651.bluno_bpm.Services.BluetoothLEService;
import com.siam11651.bluno_bpm.Services.BluetoothLEServiceWrapper;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.DeviceReaderWriter;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectedActivity extends AppCompatActivity
{
    private final String password = "AT+PASSWOR=DFRobot\r\n";
    private final String baudRateBuffer = "AT+UART=115200\r\n";
    public static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private BluetoothGattCharacteristic sCharacteristic, modelNumberCharactersitic, serialPortCharacteristic, commandCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics;
    private BluetoothLEServiceWrapper bluetoothLEServiceWrapper;
    private BluetoothLEServiceConnection bluetoothLEServiceConnection;

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
            // rescan
        }
        else
        {
            sCharacteristic = modelNumberCharactersitic;
            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().SetCharacteristicNotification(sCharacteristic, true);
            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().ReadCharacteristic(sCharacteristic);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        getSupportActionBar().setTitle(BluetoothConnection.GetBluetoothConnection().GetDevice().GetName());

        TextView systoleTextView = findViewById(R.id.systole_text_view);
        TextView diastoleTextView = findViewById(R.id.diastole_text_view);
        BluetoothConnection bluetoothConnection = BluetoothConnection.GetBluetoothConnection();
        bluetoothLEServiceWrapper = new BluetoothLEServiceWrapper(null);
        bluetoothLEServiceConnection = new BluetoothLEServiceConnection(bluetoothLEServiceWrapper, bluetoothConnection.GetDevice());
        Intent bluetoothServiceIntent = new Intent(this, BluetoothLEService.class);

        bindService(bluetoothServiceIntent, bluetoothLEServiceConnection, Context.BIND_AUTO_CREATE);

        BroadcastReceiver bleBroadcastReciever = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction().equals(BlUnoGattCallback.ACTION_GATT_CONNECTED))
                {
                    Log.println(Log.INFO, "connection", "connected");
                }
                else if(intent.getAction().equals(BlUnoGattCallback.ACTION_GATT_SERVICES_DISCOVERED))
                {
                    DisplayGattServices(bluetoothLEServiceWrapper.GetBluetoothLEService().GetSupportedGattServices());
                }
                else if(intent.getAction().equals(BlUnoGattCallback.ACTION_DATA_AVAILABLE))
                {
                    if(sCharacteristic == modelNumberCharactersitic)
                    {
                        if(intent.getStringExtra(BlUnoGattCallback.EXTRA_DATA).toUpperCase().startsWith("DF BLUNO"))
                        {
                            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().SetCharacteristicNotification(sCharacteristic, false);
                            sCharacteristic = commandCharacteristic;
                            sCharacteristic.setValue(password);
                            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().WriteCharacteristic(sCharacteristic);
                            sCharacteristic.setValue(baudRateBuffer);
                            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().WriteCharacteristic(sCharacteristic);
                            sCharacteristic = serialPortCharacteristic;
                            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().SetCharacteristicNotification(sCharacteristic, true);
                            bluetoothLEServiceWrapper.GetBluetoothLEService().GetBluetoothGattCallback().ReadCharacteristic(sCharacteristic);
                        }
                    }
                    else if(sCharacteristic == serialPortCharacteristic)
                    {
                        String data = intent.getStringExtra(BlUnoGattCallback.EXTRA_DATA);
                        String[] tokens = data.split(" ");

                        if(tokens[0].equals("b"))
                        {
                            systoleTextView.setText(tokens[1]);
                            diastoleTextView.setText(tokens[2]);
                        }
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BlUnoGattCallback.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlUnoGattCallback.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BlUnoGattCallback.ACTION_DATA_AVAILABLE);
        registerReceiver(bleBroadcastReciever, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.connection_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.disconnect_menu_item)
        {
            try
            {
                DeviceReaderWriter.WriteConnectedDevice(this, null);
            }
            catch(IOException | JSONException e)
            {
                throw new RuntimeException(e);
            }

            bluetoothLEServiceWrapper.GetBluetoothLEService().Disconnect();
            unbindService(bluetoothLEServiceConnection);

            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finishAffinity();
    }
}