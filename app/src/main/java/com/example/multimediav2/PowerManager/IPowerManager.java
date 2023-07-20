package com.example.multimediav2.PowerManager;


import android.content.Context;

import java.util.List;

import Modules.OSTime;

public interface IPowerManager {
    boolean IsOpen();
    //void SetTime(String _open, String _close, String _repeat);
    void SetTime(List<OSTime> osTimes);
    void StartListen();
    void StopListen();
    void ShutDown();
    void Open();
    void Reboot();
    void setSystemTime(Context context);
    void Install(String path);
    void StatusBar();
    String GetName();
}
