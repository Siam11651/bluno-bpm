package com.siam11651.bluno_bpm;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.siam11651.bluno_bpm.GattCallbacks.BlUnoGattCallback;
import com.siam11651.bluno_bpm.Services.BluetoothLEService;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.DataUpdateTimerTask;
import com.siam11651.bluno_bpm.Utils.DeviceReaderWriter;
import com.siam11651.bluno_bpm.Utils.SignalData;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Timer;
import java.util.Vector;

import kotlin.Pair;

public class ConnectedActivity extends AppCompatActivity
{
    Timer dataUpdateTimer;
    DataUpdateTimerTask dataUpdateTimerTask;
    private Intent bluetoothServiceIntent;
    private StringBuffer csvStringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        Objects.requireNonNull(getSupportActionBar()).setTitle(BluetoothConnection.GetBluetoothConnection().GetDevice().GetName());

        TextView systoleTextView = findViewById(R.id.signal_text_view);
        bluetoothServiceIntent = new Intent(this, BluetoothLEService.class);

        startForegroundService(bluetoothServiceIntent);

        LineChart chart = findViewById(R.id.chart1);
        LineData lineData = new LineData();
        Vector<Entry> systoleEntries = new Vector<>();
        LineDataSet systoleDataSet = new LineDataSet(systoleEntries, "Signal");

        systoleDataSet.setValueTextSize(0);
        systoleDataSet.setDrawCircles(false);
        lineData.addDataSet(systoleDataSet);
        chart.setData(lineData);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDescription(null);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        dataUpdateTimerTask = new DataUpdateTimerTask(this, systoleEntries, lineData, systoleDataSet, chart, systoleTextView);
        dataUpdateTimer = new Timer();

        dataUpdateTimer.scheduleAtFixedRate(dataUpdateTimerTask, 0, 100);

        BroadcastReceiver bleBroadcastReciever = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction().equals(BlUnoGattCallback.ACTION_GATT_CONNECTED))
                {
                    Log.println(Log.INFO, "connection", "connected");
                }
                else if(intent.getAction().equals(BlUnoGattCallback.ACTION_DATA_AVAILABLE))
                {
                    String data = intent.getStringExtra(BlUnoGattCallback.EXTRA_DATA).trim();
                    SignalData signalData = SignalData.GetSignalData();

                    signalData.SetSignalDataValue(data);
                }
                else if(intent.getAction().equals("INVALID_DEVICE"))
                {
                    Toast toast = new Toast(ConnectedActivity.this);

                    toast.setText("Invalid Device");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();

                    try
                    {
                        DeviceReaderWriter.WriteConnectedDevice(ConnectedActivity.this, null);
                    }
                    catch(IOException | JSONException e)
                    {
                        throw new RuntimeException(e);
                    }

                    stopService(bluetoothServiceIntent);

                    Intent intent1 = new Intent(ConnectedActivity.this, MainActivity.class);

                    startActivity(intent1);
                    finish();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BlUnoGattCallback.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BlUnoGattCallback.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BlUnoGattCallback.ACTION_DATA_AVAILABLE);
        intentFilter.addAction("INVALID_DEVICE");
        registerReceiver(bleBroadcastReciever, intentFilter);

        Button recordButton = findViewById(R.id.record_button);

        recordButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Button viewButton = (Button)v;

                if(dataUpdateTimerTask.IsRecording())
                {
                    viewButton.setText("Record");

                    csvStringBuffer = new StringBuffer();
                    Vector<Pair<Float, Float>> dataRecord = dataUpdateTimerTask.GetSignalValueTimeRecord();

                    for(int i = 0; i < dataRecord.size(); ++i)
                    {
                        csvStringBuffer.append(dataRecord.get(i).getFirst()).append(",").append(dataRecord.get(i).getSecond()).append("\n");
                    }

                    Intent createDocumentIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                    createDocumentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    createDocumentIntent.setType("text/csv");
                    startActivityForResult(createDocumentIntent, 123);
                }
                else
                {
                    viewButton.setText("Save");
                }

                dataUpdateTimerTask.ToggleRecording();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        Vector<String> permissionsNeeded = new Vector<>();
        boolean requestNeeded = false;

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);

            requestNeeded = true;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_DENIED)
            {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADVERTISE);

                requestNeeded = true;
            }

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
            {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);

                requestNeeded = true;
            }

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
            {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);

                requestNeeded = true;
            }
        }

        String[] permissionsNeededArray = new String[permissionsNeeded.size()];

        permissionsNeeded.toArray(permissionsNeededArray);

        if(requestNeeded)
        {
            requestPermissions(permissionsNeededArray, 200);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 123 && resultCode == Activity.RESULT_OK)
        {
            Uri path = data.getData();
            String csvString = new String(csvStringBuffer);

            try
            {
                OutputStream csvOutputStream = getContentResolver().openOutputStream(path);

                csvOutputStream.write(csvString.getBytes());
                csvOutputStream.close();
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }

            dataUpdateTimerTask.ToggleRecording();
        }
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

            stopService(bluetoothServiceIntent);

            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
            finish();
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