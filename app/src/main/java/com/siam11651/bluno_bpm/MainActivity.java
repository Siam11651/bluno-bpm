package com.siam11651.bluno_bpm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.DeviceReaderWriter;
import com.siam11651.bluno_bpm.Utils.TrimmedDevice;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;
import java.util.Vector;

public class MainActivity extends AppCompatActivity
{
    private void ChangeActivity(TrimmedDevice trimmedDevice)
    {
        Intent intent = new Intent(MainActivity.this, ConnectedActivity.class);
        BluetoothConnection bluetoothConnection = BluetoothConnection.GetBluetoothConnection();

        try
        {
            DeviceReaderWriter.WriteConnectedDevice(this, trimmedDevice);
        }
        catch(IOException | JSONException e)
        {
            // handle later
        }

        bluetoothConnection.SetDevice(trimmedDevice);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Recently Connected Devices");

        Button scanNewDeviceButton = findViewById(R.id.scan_new_device_button);

        scanNewDeviceButton.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v)
            {
                LocationManager locationManager = (LocationManager)MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

                if(locationManager.isLocationEnabled())
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

                    alertDialogBuilder.setTitle("Scanned Devices");

                    LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                    TextView emptyTextView = new TextView(MainActivity.this);

                    emptyTextView.setText("Wow! Such empty");
                    emptyTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    emptyTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    linearLayout.addView(emptyTextView);
                    alertDialogBuilder.setView(linearLayout);
                    alertDialogBuilder.setCancelable(false);

                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    alertDialogBuilder.setNegativeButton("Close", new DialogInterface.OnClickListener()
                    {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            bluetoothAdapter.cancelDiscovery();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();

                    alertDialog.show();
                    bluetoothAdapter.startDiscovery();

                    Vector<TrimmedDevice> trimmedDeviceVector = new Vector<>();

                    BroadcastReceiver bluetoothFoundBoradcastReciever = new BroadcastReceiver()
                    {
                        @Override
                        public void onReceive(Context context, Intent intent)
                        {
                            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND))
                            {
                                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                                @SuppressLint("MissingPermission") TrimmedDevice trimmedDevice = new TrimmedDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                                boolean exists = false;

                                for(int i = 0; i < trimmedDeviceVector.size(); ++i)
                                {
                                    if(trimmedDeviceVector.get(i).GetAddress().equals(trimmedDevice.GetAddress()))
                                    {
                                        exists = true;

                                        break;
                                    }
                                }

                                if(!exists)
                                {
                                    if(trimmedDeviceVector.size() == 0)
                                    {
                                        linearLayout.removeAllViews();
                                    }

                                    trimmedDeviceVector.add(trimmedDevice);

                                    CardView scannedDeviceCardView = (CardView)getLayoutInflater().inflate(R.layout.sample_scanned_device, linearLayout, false);
                                    TextView nameTextView = scannedDeviceCardView.findViewById(R.id.scanned_device_name_text_view);
                                    TextView addressTextView = scannedDeviceCardView.findViewById(R.id.scanned_device_address_text_view);

                                    scannedDeviceCardView.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            try
                                            {
                                                DeviceReaderWriter.Write(MainActivity.this, trimmedDevice);
                                            }
                                            catch(IOException | JSONException e)
                                            {
                                                throw new RuntimeException(e);
                                            }

                                            ChangeActivity(trimmedDevice);
                                            alertDialog.dismiss();
                                        }
                                    });

                                    nameTextView.setText(trimmedDevice.GetName());
                                    addressTextView.setText(trimmedDevice.GetAddress());
                                    linearLayout.addView(scannedDeviceCardView);
                                }
                            }
                        }
                    };

                    registerReceiver(bluetoothFoundBoradcastReciever, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                }
                else
                {
                    Toast toast = new Toast(MainActivity.this);

                    toast.setText("Location service required to scan nearby devices");
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.show();

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                    startActivity(intent);
                }
            }
        });
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        Vector<String> permissionsNeeded = new Vector<>();
        boolean requestNeeded = false;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

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

        TrimmedDevice connectedDevice = null;

        try
        {
            connectedDevice = DeviceReaderWriter.ReadConnectedDevice(this);
        }
        catch(IOException | JSONException e)
        {
            throw new RuntimeException(e);
        }

        if(connectedDevice != null)
        {
            ChangeActivity(connectedDevice);
        }

        Vector<TrimmedDevice> trimmedDeviceVector = null;

        try
        {
            trimmedDeviceVector = DeviceReaderWriter.Read(this);
        }
        catch(IOException | JSONException e)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                Toast toast = new Toast(this);

                toast.setText("Cannot read files to run app");
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.addCallback(new Toast.Callback()
                {
                    @Override
                    public void onToastHidden()
                    {
                        super.onToastHidden();
                        finish();
                    }
                });
                toast.show();
            }
            else
            {
                finish();
            }
        }

        LinearLayout linearLayout = findViewById(R.id.recent_devices_list_view);

        linearLayout.removeAllViews();

        for(int i = 0; i < Objects.requireNonNull(trimmedDeviceVector).size(); ++i)
        {
            CardView scannedDeviceView = (CardView)getLayoutInflater().inflate(R.layout.sample_scanned_device, linearLayout, false);
            TextView nameTextView = scannedDeviceView.findViewById(R.id.scanned_device_name_text_view);
            TextView addressTextView = scannedDeviceView.findViewById(R.id.scanned_device_address_text_view);

            int finalI = i;
            Vector<TrimmedDevice> finalTrimmedDeviceVector = trimmedDeviceVector;

            scannedDeviceView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ChangeActivity(finalTrimmedDeviceVector.get(finalI));
                }
            });
            nameTextView.setText(trimmedDeviceVector.get(i).GetName());
            addressTextView.setText(trimmedDeviceVector.get(i).GetAddress());
            linearLayout.addView(scannedDeviceView);
        }
    }
}