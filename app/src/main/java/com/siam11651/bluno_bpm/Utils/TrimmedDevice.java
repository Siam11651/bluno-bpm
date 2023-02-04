package com.siam11651.bluno_bpm.Utils;

public class TrimmedDevice
{
    private final String name;
    private final String address;

    public TrimmedDevice(String name, String address)
    {
        this.name = name;
        this.address = address;
    }

    public String GetName()
    {
        return name;
    }

    public String GetAddress()
    {
        return address;
    }
}
