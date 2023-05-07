package com.siam11651.bluno_bpm.Utils;

public class SignalData
{
    private static SignalData singleton = null;
    private String signalValue;
    private boolean recording;

    private SignalData()
    {
        signalValue = "0";
    }

    public static SignalData GetSignalData()
    {
        if(singleton == null)
        {
            singleton = new SignalData();
        }

        return singleton;
    }

    public void SetSignalDataValue(String value)
    {
            signalValue = value;
    }

    public String GetSignalDataValue()
    {
        return signalValue;
    }
}
