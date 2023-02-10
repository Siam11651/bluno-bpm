package com.siam11651.bluno_bpm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.siam11651.bluno_bpm.GattCallbacks.BlUnoGattCallback;
import com.siam11651.bluno_bpm.Services.BluetoothLEService;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.DeviceReaderWriter;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

public class ConnectedActivity extends AppCompatActivity
{
    private Intent bluetoothServiceIntent;
    Vector<Entry> systoleEntries;
    Vector<Entry> diastoleEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        Objects.requireNonNull(getSupportActionBar()).setTitle(BluetoothConnection.GetBluetoothConnection().GetDevice().GetName());

        systoleEntries = new Vector<>();
        diastoleEntries = new Vector<>();
        TextView systoleTextView = findViewById(R.id.systole_text_view);
        TextView diastoleTextView = findViewById(R.id.diastole_text_view);
        bluetoothServiceIntent = new Intent(this, BluetoothLEService.class);

        startForegroundService(bluetoothServiceIntent);

        LineChart chart = findViewById(R.id.chart1);
        final Long[] count = {0L};
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
                    String data = intent.getStringExtra(BlUnoGattCallback.EXTRA_DATA);
                    String[] tokens = data.split(" ");

                    if(tokens[0].equals("b"))
                    {
                        LineData lineData = new LineData();
                        LineDataSet systoleDataSet = new LineDataSet(systoleEntries, "Systole");
                        LineDataSet diastoleDataSet = new LineDataSet(diastoleEntries, "Diastole");

                        systoleDataSet.setValueTextSize(0);
                        systoleDataSet.setDrawCircles(false);
                        diastoleDataSet.setColor(Color.RED);
                        diastoleDataSet.setCircleColor(Color.RED);
                        diastoleDataSet.setValueTextSize(0);
                        diastoleDataSet.setDrawCircles(false);

                        if(systoleEntries.size() > 50)
                        {
                            systoleEntries.remove(0);
                        }

                        if(diastoleEntries.size() > 50)
                        {
                            diastoleEntries.remove(0);
                        }

                        systoleEntries.add(new Entry((float)count[0] / 10, Integer.parseInt(tokens[1])));
                        diastoleEntries.add(new Entry((float)count[0] / 10, Integer.parseInt(tokens[2])));

                        ++count[0];

                        lineData.addDataSet(systoleDataSet);
                        lineData.addDataSet(diastoleDataSet);
                        chart.setData(lineData);
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                        systoleTextView.setText(tokens[1]);
                        diastoleTextView.setText(tokens[2]);
                    }
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
        chart.setBackgroundColor(Color.WHITE);
        chart.setDescription(null);
        chart.setDragEnabled(false);
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