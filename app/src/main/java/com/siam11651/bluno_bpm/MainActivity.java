package com.siam11651.bluno_bpm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.siam11651.bluno_bpm.ServiceBinders.BluetoothLEServiceBinder;
import com.siam11651.bluno_bpm.ServiceConnections.BluetoothLEServiceConnection;
import com.siam11651.bluno_bpm.Services.BluetoothLEService;
import com.siam11651.bluno_bpm.Utils.BluetoothConnection;
import com.siam11651.bluno_bpm.Utils.DeviceReaderWriter;
import com.siam11651.bluno_bpm.Utils.TrimmedDevice;

import org.json.JSONException;

import java.io.IOException;
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
            @Override
            public void onClick(View v)
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
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                        {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                        }

                        bluetoothAdapter.cancelDiscovery();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();

                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                }

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

                            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                            {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                            }

                            TrimmedDevice trimmedDevice = new TrimmedDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress());
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
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

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

        for(int i = 0; i < trimmedDeviceVector.size(); ++i)
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