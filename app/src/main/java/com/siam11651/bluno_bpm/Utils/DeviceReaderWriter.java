package com.siam11651.bluno_bpm.Utils;

import android.content.Context;
import android.util.JsonReader;

import com.siam11651.bluno_bpm.ConnectedActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

public class DeviceReaderWriter
{
    public static TrimmedDevice ReadConnectedDevice(Context context) throws IOException, JSONException
    {
        File file = new File(context.getFilesDir(), "active.json");

        file.createNewFile();

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileByteArray = new byte[(int)fileInputStream.getChannel().size()];

        fileInputStream.read(fileByteArray);

        String fileString = new String(fileByteArray);

        JSONObject jsonObject = null;

        try
        {
            jsonObject = new JSONObject(fileString);
        }
        catch(JSONException e)
        {
            return null;
        }

        return new TrimmedDevice(jsonObject.getString("name"), jsonObject.getString("address"));
    }

    public static void WriteConnectedDevice(Context context, TrimmedDevice device) throws IOException, JSONException
    {
        File file = new File(context.getFilesDir(), "active.json");

        file.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(file);

        if(device != null)
        {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("name", device.GetName());
            jsonObject.put("address", device.GetAddress());

            fileOutputStream.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
        }
        else
        {
            fileOutputStream.write("".getBytes(StandardCharsets.UTF_8));
        }
    }

    public static Vector<TrimmedDevice> Read(Context context) throws IOException, JSONException
    {
        File file = new File(context.getFilesDir(), "devices.json");
        Vector<TrimmedDevice> trimmedDeviceVector = new Vector<>();

        file.createNewFile();

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileByteArray = new byte[(int)fileInputStream.getChannel().size()];

        fileInputStream.read(fileByteArray);
        fileInputStream.close();

        String fileString = new String(fileByteArray);
        JSONObject jsonObject = null;

        try
        {
            jsonObject = new JSONObject(fileString);
        }
        catch(JSONException e)
        {
            return trimmedDeviceVector;
        }

        JSONArray devicesJsonArray = jsonObject.getJSONArray("devices");

        for(int i = 0; i < devicesJsonArray.length(); ++i)
        {
            JSONObject trimmedDeviceJsonObject = (JSONObject)devicesJsonArray.get(i);
            TrimmedDevice trimmedDevice = new TrimmedDevice(trimmedDeviceJsonObject.getString("name"), trimmedDeviceJsonObject.getString("address"));

            trimmedDeviceVector.add(trimmedDevice);
        }

        return trimmedDeviceVector;
    }

    public static void Write(Context context, TrimmedDevice device) throws IOException, JSONException
    {
        List<TrimmedDevice> trimmedDeviceList = Read(context);
        boolean found = false;

        for(int i = 0; i < trimmedDeviceList.size(); ++i)
        {
            if(trimmedDeviceList.get(i).GetAddress().equals(device.GetAddress()))
            {
                found = true;

                break;
            }
        }

        if(!found)
        {
            trimmedDeviceList.add(0, device); // add at top

            JSONArray devicesJsonArray = new JSONArray();

            for(int i = 0; i < trimmedDeviceList.size(); ++i)
            {
                JSONObject trimmedDevicJsonObject = new JSONObject();

                trimmedDevicJsonObject.put("name", trimmedDeviceList.get(i).GetName());
                trimmedDevicJsonObject.put("address", trimmedDeviceList.get(i).GetAddress());
                devicesJsonArray.put(trimmedDevicJsonObject);
            }

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("devices", devicesJsonArray);

            String jsonString = jsonObject.toString();
            File file = new File(context.getFilesDir(), "devices.json");

            file.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            fileOutputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.close();
        }
    }
}
